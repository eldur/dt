package info.dt.srv;

import info.dt.data.IDateConfig;
import info.dt.data.TimeSheet;
import info.dt.data.TimeSheetPosition;
import info.dt.data.TimeSheetPosition.Status;

import java.util.List;

import javax.inject.Singleton;

import org.joda.time.DateTime;
import org.joda.time.Duration;
import org.joda.time.ReadableInterval;

import com.google.common.base.Splitter;
import com.google.common.collect.Lists;

@Singleton
public class DemoDateConfig implements IDateConfig {
  DateTime now = DateTime.now();

  public TimeSheet getTimeSheet(ReadableInterval interval) {
    List<TimeSheetPosition> positions = Lists.newArrayList();
    add(positions, now, 15, "#id-a,v,φ,Ω,∇", "Do It;a");
    add(positions, now, 75, "#id-b,v,ε,Φ,∇", "Do It;b");
    add(positions, now, 60, "#id-c,o,ψ,Θ,∇", "Do It");
    add(positions, now, 45, "#id-f,w,ζ,Ξ,∇", "Do It");
    add(positions, now, 15, "#id-a,o,ξ,Σ,∇", "Do It;c");
    add(positions, now, 15, "#id-a,v,δ,Γ,∇", "Do It;d");
    add(positions, now, 15, "#id-a,o,β,Σ,∇", "Do It;e");

    TimeSheet ts = new TimeSheet(positions, 0, 0);
    return ts;
  }

  private void add(List<TimeSheetPosition> positions, DateTime now, int minutes, String pathStr, String comment) {
    Iterable<String> path = Splitter.on(",").split(pathStr);
    positions.add(new TimeSheetPosition(now, comment, Duration.standardMinutes(minutes), path, Status.NONE));
  }
}
