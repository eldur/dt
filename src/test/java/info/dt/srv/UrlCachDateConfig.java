package info.dt.srv;

import info.dt.data.IDateConfig;
import info.dt.data.TimeSheet;
import info.dt.data.TimeSheetPosition;

import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import javax.annotation.Nullable;

import lombok.extern.slf4j.Slf4j;

import org.joda.time.DateTime;
import org.joda.time.ReadableInterval;

import com.google.common.base.Predicate;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListenableFutureTask;

@Slf4j
public abstract class UrlCachDateConfig implements IDateConfig {

  private final Iterable<URL> resources;

  public UrlCachDateConfig(Iterable<URL> resources) {
    this.resources = resources;
  }

  public abstract List<TimeSheetPosition> parse(URL url);

  private final ExecutorService executor = Executors.newSingleThreadExecutor();
  private final LoadingCache<ReadableInterval, List<TimeSheetPosition>> cache //
  = CacheBuilder.newBuilder().refreshAfterWrite(2, TimeUnit.SECONDS)
      .build(new CacheLoader<ReadableInterval, List<TimeSheetPosition>>() {

        private final Map<String, Long> fileChanged = Maps.newConcurrentMap();
        private final List<URL> toReload = Lists.newArrayList();

        @Override
        public List<TimeSheetPosition> load(ReadableInterval key)
            throws Exception {
          toReload.clear();
          toReload.addAll(Lists.newArrayList(getAllResourceURLs()));
          List<TimeSheetPosition> positions = innerLoad(key);
          return positions;
        }

        private List<TimeSheetPosition> innerLoad(final ReadableInterval key) {
          List<TimeSheetPosition> positions = Lists.newArrayList();
          for (URL url : toReload) {
            log.info("reload {}", url.toExternalForm());
            List<TimeSheetPosition> poss = parse(url);
            for (TimeSheetPosition pos : poss) {
              positions.add(pos);
            }
          }

          Predicate<TimeSheetPosition> predicate = new Predicate<TimeSheetPosition>() {

            public boolean apply(@Nullable TimeSheetPosition input) {

              return key.contains(input.getBegin());
            }
          };

          return Lists.newArrayList(Iterables.filter(positions, predicate));
        }

        @Override
        public ListenableFuture<List<TimeSheetPosition>> reload(
            final ReadableInterval key, List<TimeSheetPosition> prevGraph) {
          if (noRefreshNeeded()) {
            return Futures.immediateFuture(prevGraph);
          } else {
            ListenableFutureTask<List<TimeSheetPosition>> task = ListenableFutureTask
                .create(new Callable<List<TimeSheetPosition>>() {
                  public List<TimeSheetPosition> call() {
                    return innerLoad(key);
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

  public TimeSheet getTimeSheet(ReadableInterval interval) {
    DateTime start = interval.getStart();
    List<TimeSheetPosition> positions = ImmutableList.of();
    try {

      positions = cache.get(interval);

    } catch (ExecutionException e) {
      throw new IllegalStateException(e);
    }

    return new TimeSheet(positions, start.getYear(), start.getMonthOfYear());

  }

}
