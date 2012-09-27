package info.dt.srv;

import info.dt.data.TimeSheet;
import info.dt.data.TimeSheetPosition;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.annotation.Nullable;

import org.joda.time.DateTime;
import org.joda.time.Duration;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.DateTimeFormatterBuilder;
import org.yaml.snakeyaml.Yaml;

import com.google.common.base.Charsets;
import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Iterables;
import com.google.common.collect.Multimap;
import com.mycila.inject.internal.guava.collect.Maps;
import com.mycila.inject.internal.guava.io.Files;

public class YamlDateConfigWriter {

  private Yaml y = new Yaml();
  private File file;

  public YamlDateConfigWriter(File file) {
    this.file = file;

  }

  public void writer(TimeSheet timeSheet) {

    Function<TimeSheetPosition, Map<String, String>> function //
    = new Function<TimeSheetPosition, Map<String, String>>() {

      @Nullable
      public Map<String, String> apply(@Nullable TimeSheetPosition arg0) {
        Map<String, String> result = Maps.newHashMap();
        result.put("t", formatTime(arg0.getBegin(), arg0.getDuration()));
        result.put("l", formatPath(arg0.getPath()));
        result.put("d", arg0.getComment());
        return result;
      }

      private String formatPath(List<String> path) {
        return Joiner.on("+").join(path);
      }

      private String formatTime(DateTime begin, Duration duration) {
        DateTimeFormatter formatter = new DateTimeFormatterBuilder() //
            .appendYear(4, 4) //
            .appendLiteral("-") //
            .appendMonthOfYear(2) //
            .appendLiteral("-") //
            .appendDayOfMonth(2) //
            .toFormatter();

        DateTimeFormatterBuilder shortFormatter = new DateTimeFormatterBuilder() //
            .appendHourOfDay(2) //
            .appendMinuteOfHour(2) //
        ;
        String beginStr = formatter.print(begin);

        String durationStr = shortFormatter.toFormatter().print(begin) + "-"
            + shortFormatter.toFormatter().print(begin.plus(duration));
        return beginStr + " " + durationStr;
      }
    };
    Iterable<Map<String, String>> transform = Iterables.transform(timeSheet, function);

    Multimap<String, Map<String, String>> map = ArrayListMultimap.create();
    Pattern pattern = Pattern.compile("([0-9]{4}-[0-9]{1,2}-[0-9]{1,2}) ([0-9-]+)");
    for (Map<String, String> innerMap : transform) {
      String timeDuration = innerMap.get("t");
      Matcher matcher = pattern.matcher(timeDuration);
      if (matcher.matches()) {
        innerMap.put("t", matcher.group(2));
        map.put(matcher.group(1), innerMap);
      } else {
        throw new IllegalArgumentException(timeDuration);
      }
    }

    String dump = y.dump(map.asMap());

    try {
      Files.write(dump, file, Charsets.UTF_8);
    } catch (IOException e) {
      throw new IllegalArgumentException(e);
    }
  }
}
