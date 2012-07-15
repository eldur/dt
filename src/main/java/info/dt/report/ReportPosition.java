package info.dt.report;

import info.dt.data.TimeSheetPosition;

import java.util.List;
import java.util.Map;

import org.joda.time.DateTime;
import org.joda.time.Duration;

class ReportPosition extends TimeSheetPosition implements IReportPosition {

  private String title;
  private Map<List<String>, Duration> pathes;
  private Duration reportDuration;

  /**
   * @return the pathes
   */
  public Map<List<String>, Duration> getPathes() {
    return pathes;
  }

  public ReportPosition(DateTime begin, String title, String comment //
      , Duration duration, List<String> path, Map<List<String>, Duration> pathes, Duration reportDuration) {
    super(begin, comment, duration, path);
    this.title = title;
    this.pathes = pathes;
    this.reportDuration = reportDuration;
  }

  public String getTitle() {
    return title;
  }

  public int getDurationPercentage() {
    return (int) (100 * getDuration().getMillis() / reportDuration.getMillis());
  }
}