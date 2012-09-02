package info.dt.report;

import info.dt.data.ITimeSheetPosition;
import info.dt.data.TimeSheetPosition.Status;

import java.util.List;
import java.util.Map;

import org.joda.time.Duration;

public interface IReportPosition extends ITimeSheetPosition {

  String getTitle();

  Map<List<String>, Duration> getPathes();

  int getDurationPercentage();

  Status getStatus();

  List<String> getCommentLines();

}
