package info.dt.srv;

import info.dt.data.IDateConfig;
import info.dt.data.TimeSheet;
import info.dt.data.TimeSheetPosition;
import info.dt.data.TimeSheetPosition.Status;

import java.io.IOException;
import java.net.URL;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.inject.Named;

import org.joda.time.DateTime;
import org.joda.time.Duration;
import org.joda.time.ReadableInterval;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.yaml.snakeyaml.Yaml;

import com.google.common.base.Charsets;
import com.google.common.base.Splitter;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import com.google.common.io.Resources;

public class YamlDateConfig implements IDateConfig {

  private Iterable<URL> resources;
  private static final DateTimeFormatter dateFormat = DateTimeFormat.forPattern("yyyy-M");

  public YamlDateConfig(@Nullable @Named("yaml.url") URL resource) {
    this(ImmutableList.of(resource));
  }

  @Inject
  public YamlDateConfig(@Nullable @Named("yaml.urls") Iterable<URL> resources) {
    this.resources = resources;

  }

  public TimeSheet getTimeSheet(ReadableInterval interval) {
    DateTime start = interval.getStart();
    Multimap<String, TimeSheetPosition> positions = ArrayListMultimap.create();
    for (URL url : resources) {
      List<TimeSheetPosition> poss = parseYaml(url);
      for (TimeSheetPosition pos : poss) {
        DateTime begin = pos.getBegin();
        String key = begin.toString(dateFormat);

        positions.put(key, pos);
      }
    }
    List<TimeSheetPosition> list = Lists.newArrayList(positions.get(start.getYear() + "-" + start.getMonthOfYear()));
    return new TimeSheet(list, start.getYear(), start.getMonthOfYear(), -1d);
  }

  private List<TimeSheetPosition> parseYaml(URL url) {
    List<TimeSheetPosition> result = Lists.newArrayList();
    Yaml y = new Yaml();
    DateTimeFormatter shortFormat = DateTimeFormat.forPattern("HHmm");
    DateTimeFormatter beginFormat = DateTimeFormat.forPattern("yyyy-M-d HH:mm");
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
                description = (String) o.getValue();
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

    } catch (IOException e) {
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
        throw new IllegalStateException();
    }
  }
}
