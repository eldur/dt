package info.dt.report;

import info.dt.data.TimeSheet;

import java.util.List;

public interface IReportView {

  String getLabel();

  String getInfo();

  String getId();

  String getNext();

  String getPrevious();

  List<IReportPosition> toReportPositions(TimeSheet timeSheet);

}