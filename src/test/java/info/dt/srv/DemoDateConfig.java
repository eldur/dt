package info.dt.srv;

import info.dt.data.IDateConfig;
import info.dt.data.Status;
import info.dt.data.TimeSheet;
import info.dt.data.TimeSheetPosition;

import java.util.List;

import javax.annotation.Nullable;
import javax.inject.Singleton;

import org.joda.time.DateTime;
import org.joda.time.Duration;
import org.joda.time.ReadableInterval;

import com.google.common.base.Predicate;
import com.google.common.base.Splitter;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

@Singleton
public class DemoDateConfig implements IDateConfig {

  private final DateTime now;

  public DemoDateConfig() {
    DateTime now2 = DateTime.now();
    long millis = now2.getMillis();
    long modQuarterHour = millis % 900000;
    millis = millis - modQuarterHour;
    now = new DateTime(millis);
  }

  public TimeSheet getTimeSheet(final ReadableInterval interval) {
    List<TimeSheetPosition> positions = Lists.newArrayList();
    add(positions, now, 15, "#id-a,v,φ,Ω,∇", "Do It;a");
    add(positions, now, 75, "#id-b,v,ε,Φ,∇", "Do It;b");
    add(positions, now, 60, "#id-c,o,ψ,Θ,∇", "Do It");
    add(positions, now, 45, "#id-f,w,ζ,Ξ,∇", "Do It");
    add(positions, now, 15, "#id-a,o,ξ,Σ,∇", "Do It;c");
    add(positions, now, 15, "#id-a,v,δ,Γ,∇", "Do It;d");
    add(positions, now, 15, "#id-a,o,β,Σ,∇", "Do It;e");

    Predicate<TimeSheetPosition> predicate = new Predicate<TimeSheetPosition>() {

      public boolean apply(@Nullable TimeSheetPosition input) {

        return interval.contains(input.getBegin());
      }
    };

    return new TimeSheet(Lists.newArrayList(Iterables.filter(positions, predicate)), 0, 0);
  }

  private void add(List<TimeSheetPosition> positions, DateTime now, int minutes, String pathStr,
      String comment) {
    Iterable<String> path = Splitter.on(",").split(pathStr);
    positions.add(new TimeSheetPosition(now, comment, Duration.standardMinutes(minutes), path,
        Status.NONE));
  }
}
