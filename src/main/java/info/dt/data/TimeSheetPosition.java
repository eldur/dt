package info.dt.data;

import java.util.List;

import lombok.Data;

import org.joda.time.DateTime;
import org.joda.time.Duration;

import com.google.common.collect.ImmutableList;

@Data
public class TimeSheetPosition implements ITimeSheetPosition {

  private final List<String> path;
  private final String comment;
  private final Duration duration;
  private final DateTime begin;

  public TimeSheetPosition(DateTime begin, String label, String comment,
      long minutes) {
    this(begin, label, comment, Duration.standardMinutes(minutes));
  }

  public TimeSheetPosition(DateTime begin, String comment, long minutes,
      Iterable<String> path) {
    this(begin, comment,  Duration.standardMinutes(minutes), path);
  }
  
  public TimeSheetPosition(DateTime begin, String comment, Duration duration,
      Iterable<String> path) {
    path.getClass();
    comment.getClass();
    this.path = ImmutableList.copyOf(path);
    if (this.path.size() <= 0) {
      throw new IllegalStateException("the first path element is required");
    }
    this.comment = comment;
    this.duration = duration;
    this.begin = begin;
  }

  public TimeSheetPosition(DateTime begin, String id, String comment,
      Duration duration) {
    id.getClass();
    comment.getClass();
    duration.getClass();
    this.path = ImmutableList.of(id);
    this.comment = comment;
    this.duration = duration;
    this.begin = begin;
  }
  /**
   * 
   * @return first path element
   */
  public String getId() {
    return path.get(0);
  }
   
  public String getTitle() {
    return " a very long title description is sometimes a regular casee, because it's a story title";
  }

}
