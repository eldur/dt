package info.dt.report;

import info.dt.data.TimeSheetPosition;

import org.joda.time.DateTime;
import org.joda.time.Duration;

import com.google.common.base.Joiner;

class ReportPosition extends TimeSheetPosition implements IReportPosition {

  private String title;

  public ReportPosition(DateTime begin, String title, String comment, Duration duration, Iterable<String> path) {
    super(begin, comment, duration, path);
    this.title = title;
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

  public String getTitle() {
    return title;
  }

}