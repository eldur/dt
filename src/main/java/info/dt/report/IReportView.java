package info.dt.report;

import info.dt.data.TimeSheet;

import java.util.List;

import org.joda.time.Interval;

public interface IReportView {

  String getLabel();

  String getInfo();

  String getId();

  String getNext();

  String getPrevious();

  List<IReportPosition> toReportPositions(TimeSheet timeSheet);

  Interval getCurrentInterval();

}