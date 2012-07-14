package info.dt.report;

import info.dt.data.TimeSheet;

import java.util.List;

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
    List<IReportPosition> filtered = new TicketFilterBuilder(timeSheet).getResult();

    return filtered;
  }

}
