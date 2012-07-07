package info.dt.data;

import java.util.List;

import lombok.Data;

import org.joda.time.DateTime;
import org.joda.time.Duration;

import com.google.common.collect.ImmutableList;

@Data
public class TimeSheetPosition {

  private final List<String> labels;
  private final String comment;
  private final Duration duration;
  private final DateTime begin;

  public TimeSheetPosition(DateTime begin, String label, String comment,
      long minutes) {
    this(begin, label, comment, Duration.standardMinutes(minutes));
  }

  public TimeSheetPosition(DateTime begin, String comment, long minutes,
      Iterable<String> labels) {
    labels.getClass();
    comment.getClass();
    this.labels = ImmutableList.copyOf(labels);
    if (this.labels.size() <= 0) {
      throw new IllegalStateException("no labels found");
    }
    this.comment = comment;
    this.duration = Duration.standardMinutes(minutes);
    this.begin = begin;
  }

  public TimeSheetPosition(DateTime begin, String label, String comment,
      Duration duration) {
    label.getClass();
    comment.getClass();
    duration.getClass();
    this.labels = ImmutableList.of(label);
    this.comment = comment;
    this.duration = duration;
    this.begin = begin;
  }

  public String getMainLabel() {
    return labels.get(0);
  }
  
  public String getTitle() {
    return "Title";
  }

}
