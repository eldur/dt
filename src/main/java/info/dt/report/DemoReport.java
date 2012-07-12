package info.dt.report;

import info.dt.data.TimeSheet;
import info.dt.data.TimeSheetPosition;

import java.util.List;

import org.joda.time.DateTime;
import org.joda.time.Duration;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;

public class DemoReport implements IReportView {

  public String getLabel() {
    return "Demo Report";
  }

  public String getInfo() {
    return "info";
  }

  public String getId() {
    return "default";
  }

  public String getNext() {
    // TODO Auto-generated method stub
    return "";
  }

  public String getPrevious() {
    // TODO Auto-generated method stub
    return "";
  }

  public List<IReportPosition> toReportPositions(TimeSheet timeSheet) {
    List<IReportPosition> list = Lists.newArrayList();
    List<TimeSheetPosition> filtered = new TicketFilterBuilder(timeSheet).getResult();

    for (TimeSheetPosition pos : filtered) {
      list.add(new DemoReportPosition(pos.getBegin(), pos.getComment(), pos.getDuration(), pos.getPath()));
    }

    return list;
  }

  private static class DemoReportPosition extends TimeSheetPosition implements IReportPosition {

    public DemoReportPosition(DateTime begin, String comment, Duration duration, Iterable<String> path) {
      super(begin, comment, duration, path);
    }

    public String getLabel() {
      String join = Joiner.on("-").join(getPath());
      int length = getId().length();
      if (join.length() > length) {
        return join.substring(length + 1);
      } else {
        return "";
      }
    }

  }

}
