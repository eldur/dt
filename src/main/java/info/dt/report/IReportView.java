package info.dt.report;

import info.dt.data.TimeSheet;

import java.util.List;

import org.joda.time.ReadableInterval;

public interface IReportView {

  String getLabel();

  String getInfo();

  String getId();

  List<IReportPosition> toReportPositions(TimeSheet timeSheet);

  /**
   * @deprecated interval controls moved to client
   */
  @Deprecated
  ReadableInterval getCurrentInterval();

}
