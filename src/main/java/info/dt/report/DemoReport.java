package info.dt.report;

import info.dt.data.TimeSheet;

import java.util.List;

import javax.annotation.Nullable;
import javax.inject.Inject;

import org.joda.time.DateTime;
import org.joda.time.Interval;

public class DemoReport implements IReportView {

  /**
   * 
   * @param parse
   *          initial date
   */
  @Inject
  @Nullable
  private DateTime now;

  public String getLabel() {
    return "Demo Report";
  }

  public String getInfo() {
    return "info";
  }

  public String getId() {
    return "default";
  }

  public List<IReportPosition> toReportPositions(TimeSheet timeSheet) {
    List<IReportPosition> filtered = new TicketFilterBuilder(timeSheet).getResult();

    return filtered;
  }

  public Interval getCurrentInterval() {
    if (now == null) {
      now = DateTime.now();
    }

    return new Interval(DateTime.parse(now.getYear() + "-" + now.getMonthOfYear() + "-01") //
        , DateTime.parse(now.getYear() + "-" + now.getMonthOfYear() + "-"
            + now.dayOfMonth().withMaximumValue().getDayOfMonth()));
  }
}
