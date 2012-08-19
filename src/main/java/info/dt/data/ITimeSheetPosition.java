package info.dt.data;

import info.dt.data.TimeSheetPosition.Status;

import java.util.List;

import org.joda.time.DateTime;
import org.joda.time.Duration;

public interface ITimeSheetPosition {

  String getId();

  Duration getDuration();

  List<String> getPath();

  DateTime getBegin();

  String getComment();

  Status getStatus();

}
