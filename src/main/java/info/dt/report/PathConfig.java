package info.dt.report;

import org.joda.time.DateTime;
import org.joda.time.Duration;
import org.joda.time.Interval;

public class PathConfig {

  public PathConfig() {
    set("∇", interval(2012, 01), hours(100)) //
        .add(".Σ.", hours(100)) //
    ;
    set("∇", interval(2012, 02), hours(100)) //
        .add(".#id-a", 01, 05, hours(1)) //
        .add(".#id-b", 01, 05, hours(1)) //
    ;

  }

  private PathConfig add(String string, Duration hours) {
    // TODO Auto-generated method stub
    return this;
  }

  private PathConfig set(String pathNode, Interval interval, Duration duration) {
    // TODO check overlaping intervals
    // TODO check duration <= interval duration
    return this;
  }

  private PathConfig add(String path, int beginDay, int endDay, Duration duration) {
    // TODO check path
    return this;
  }

  private Interval interval(int year, int month) {
    DateTime begin = DateTime.parse(year + "-" + month + "-01");
    DateTime end = begin.plusDays(begin.dayOfMonth().withMaximumValue().getDayOfMonth());
    new Interval(begin, end);
    return null;
  }

  private Duration hours(int hours) {
    return Duration.standardHours(hours);
  }

}
