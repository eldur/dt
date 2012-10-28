package info.dt.srv;

import info.dt.data.IDateConfig;
import info.dt.data.TimeSheet;
import info.dt.data.TimeSheetPosition;
import info.dt.data.TimeSheetPosition.Status;

import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import lombok.extern.slf4j.Slf4j;

import org.joda.time.DateTime;
import org.joda.time.Duration;
import org.joda.time.ReadableInterval;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.yaml.snakeyaml.Yaml;

import com.google.common.base.Charsets;
import com.google.common.base.Predicate;
import com.google.common.base.Splitter;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.io.Resources;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListenableFutureTask;

@Slf4j
@Singleton
public class YamlDateConfig implements IDateConfig {

  private Iterable<URL> resources;

  static DateTimeFormatter shortFormat = DateTimeFormat.forPattern("HHmm");
  static DateTimeFormatter beginFormat = DateTimeFormat.forPattern("yyyy-M-d HH:mm");

  @Inject
  public YamlDateConfig(@Named("yaml.url") URL resource) {
    this(ImmutableList.of(resource));
  }

  public YamlDateConfig(@Named("yaml.urls") Iterable<URL> resources) {
    this.resources = resources;
  }

  private final ExecutorService executor = Executors.newSingleThreadExecutor();
  private final LoadingCache<ReadableInterval, List<TimeSheetPosition>> cache //
  = CacheBuilder.newBuilder().refreshAfterWrite(2, TimeUnit.SECONDS)
      .build(new CacheLoader<ReadableInterval, List<TimeSheetPosition>>() {

        private Map<String, Long> fileChanged = Maps.newConcurrentMap();
        private List<URL> toReload = Lists.newArrayList();

        @Override
        public List<TimeSheetPosition> load(ReadableInterval key) throws Exception {
          toReload.clear();
          toReload.addAll(Lists.newArrayList(getAllResourceURLs()));
          List<TimeSheetPosition> positions = reload();
          return positions;
        }

        private List<TimeSheetPosition> reload() {
          List<TimeSheetPosition> positions = Lists.newArrayList();
          for (URL url : toReload) {
            log.info("reload {}", url.toExternalForm());
            List<TimeSheetPosition> poss = parseYaml(url);
            for (TimeSheetPosition pos : poss) {
              positions.add(pos);
            }
          }
          return positions;
        }

        @Override
        public ListenableFuture<List<TimeSheetPosition>> reload(final ReadableInterval key,
            List<TimeSheetPosition> prevGraph) {
          if (noRefreshNeeded()) {
            return Futures.immediateFuture(prevGraph);
          } else {
            ListenableFutureTask<List<TimeSheetPosition>> task = ListenableFutureTask
                .create(new Callable<List<TimeSheetPosition>>() {
                  public List<TimeSheetPosition> call() {
                    return reload();
                  }
                });
            executor.execute(task);
            return task;
          }
        }

        private Iterable<URL> getAllResourceURLs() {
          return resources;
        }

        private boolean noRefreshNeeded() {
          toReload.clear();
          boolean refresh = true;
          for (URL url : getAllResourceURLs()) {
            try {
              File file = new File(url.toURI());
              Long changeDate = fileChanged.get(file.getAbsolutePath());
              Long valueOf = Long.valueOf(file.lastModified());
              if (changeDate == null || !valueOf.equals(changeDate)) {
                log.info("file change detected / {}", file.getName());
                refresh = false;
                toReload.add(url);
              }
              fileChanged.put(file.getAbsolutePath(), valueOf);
            } catch (URISyntaxException e) {
              throw new IllegalArgumentException(e);
            }
          }
          return refresh;
        }
      });

  private List<TimeSheetPosition> parseYaml(URL url) {
    List<TimeSheetPosition> result = Lists.newArrayList();
    Yaml y = new Yaml();

    try {
      Object load = y.load(Resources.toString(url, Charsets.UTF_8));

      if (load instanceof Map) {
        Map<Date, List<Map<String, ?>>> map = (Map<Date, List<Map<String, ?>>>) load;
        for (Entry<Date, List<Map<String, ?>>> entry : map.entrySet()) {
          DateTime day = new DateTime(entry.getKey());
          for (Map<String, ?> pos : entry.getValue()) {
            // pos
            DateTime begin = null;
            List<String> path = null;
            String description = null;
            Duration duration = null;
            Status status = Status.NONE;
            for (Entry<String, ?> o : pos.entrySet()) {
              String key = o.getKey();
              if ("d".equals(key)) {
                Object value = o.getValue();
                if (value instanceof String) {
                  description = (String) value;
                } else if (value instanceof byte[]) {
                  description = new String((byte[]) value);
                } else {
                  throw new IllegalArgumentException(value.getClass().getCanonicalName());
                }

                if (description.contains("TODO")) {
                  status = Status.TODO;
                }
                if (description.contains("FIXME")) {
                  status = Status.FIXME;
                }
              } else if ("t".equals(key)) {
                final String value = (String) o.getValue();
                String beginStr = value.replaceAll("-[0-9]{1,4}$", "");
                DateTime beginHour = DateTime.parse(fillTimeStr(beginStr), shortFormat);

                String newBegin = day.getYear() + "-" + day.getMonthOfYear() + "-" + day.getDayOfMonth() + " "
                    + beginHour.getHourOfDay() + ":" + beginHour.getMinuteOfHour();
                begin = DateTime.parse(newBegin, beginFormat);

                String endStr = value.replaceAll("^[0-9]{1,4}-", "");
                DateTime endHour = DateTime.parse(fillTimeStr(endStr), shortFormat);

                DateTime diff = endHour.minus(beginHour.getMillis());

                duration = Duration.millis(diff.getMillis());
              } else if ("l".equals(key)) {
                Object value = o.getValue();
                if (value instanceof String) {
                  String pathString = (String) value;
                  Iterable<String> iterable = Splitter.on("+").split(pathString);
                  path = Lists.newLinkedList(iterable);
                } else {
                  throw new IllegalStateException("unknown type for l");
                }
              } else {

                throw new IllegalStateException("unknown field " + key);
              }
            }
            TimeSheetPosition timeSheetPosition;
            if (path != null) {
              timeSheetPosition = new TimeSheetPosition(begin, description, duration.getStandardMinutes(), path, status);
            } else {
              throw new IllegalStateException("no label was set");
            }
            result.add(timeSheetPosition);
          }
        }
      } else {
        throw new IllegalStateException("data have to be a map");
      }

    } catch (Exception e) {
      throw new IllegalStateException(e);
    }
    return result;
  }

  private String fillTimeStr(String timeStr) {
    int length = timeStr.length();
    switch (length) {
      case 1:
        return timeStr + "000";
      case 2:
        return timeStr + "00";
      case 4:
        return timeStr;
      default:
        throw new IllegalStateException(timeStr);
    }
  }

  public TimeSheet getTimeSheet(ReadableInterval interval) {
    DateTime start = interval.getStart();
    List<TimeSheetPosition> positions = ImmutableList.of();
    try {

      positions = cache.get(interval);

    } catch (ExecutionException e) {
      throw new IllegalStateException(e);
    }
    Predicate<TimeSheetPosition> predicate = new Predicate<TimeSheetPosition>() {

      public boolean apply(@Nullable TimeSheetPosition input) {
        // TODO Auto-generated method stub
        return false;
      }
    };

    return new TimeSheet(positions, start.getYear(), start.getMonthOfYear());

  }
}
