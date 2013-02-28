package info.dt.srv;

import info.dt.data.IDateConfig;
import info.dt.data.Status;
import info.dt.data.TimeSheetPosition;

import java.net.URL;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import lombok.extern.slf4j.Slf4j;

import org.joda.time.DateTime;
import org.joda.time.Duration;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.yaml.snakeyaml.Yaml;

import com.google.common.base.Charsets;
import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.io.Resources;

@Slf4j
@Singleton
public class YamlDateConfig extends UrlCachDateConfig implements IDateConfig {

  static DateTimeFormatter shortFormat = DateTimeFormat.forPattern("HHmm");
  static DateTimeFormatter beginFormat = DateTimeFormat.forPattern("yyyy-M-d HH:mm");

  @Inject
  public YamlDateConfig(@Named("yaml.url") URL resource) {
    super(ImmutableList.of(resource));
  }

  public YamlDateConfig(Iterable<URL> resources) {
    super(resources);
  }

  @Override
  public List<TimeSheetPosition> parse(URL url) {
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

                String newBegin = day.getYear() + "-" + day.getMonthOfYear() + "-"
                    + day.getDayOfMonth() + " " + beginHour.getHourOfDay() + ":"
                    + beginHour.getMinuteOfHour();
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
              timeSheetPosition = new TimeSheetPosition(begin, description,
                  duration.getStandardMinutes(), path, status);
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

}
