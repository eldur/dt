package info.dt.data;

import java.util.List;

import lombok.Data;

import org.joda.time.DateTime;
import org.joda.time.Duration;

import com.google.common.base.Joiner;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;

@Data
public class TimeSheetPosition implements ITimeSheetPosition {

  private final List<String> path;
  private final String comment;
  private final Duration duration;
  private final DateTime begin;
  private final Status status;

  public TimeSheetPosition(DateTime begin, String label, String comment, long minutes, Status status) {
    this(begin, label, comment, Duration.standardMinutes(minutes), status);
  }

  public TimeSheetPosition(DateTime begin, String comment, long minutes//
      , Iterable<String> path, Status status) {
    this(begin, comment, Duration.standardMinutes(minutes), path, status);
  }

  public TimeSheetPosition(DateTime begin, String comment, Duration duration,
      Iterable<String> path, Status status) {
    path.getClass();
    comment.getClass();
    this.path = ImmutableList.copyOf(path);

    if (this.path.size() <= 0) {
      throw new IllegalStateException("the first path element is required");
    }
    for (String pathElement : path) {
      if (Strings.isNullOrEmpty(pathElement)) {
        throw new IllegalStateException("empty elements are not allowed: "
            + Joiner.on("|").skipNulls().join(path));
      }
    }

    this.comment = comment;
    this.duration = duration;
    this.begin = begin;
    this.status = status;
  }

  public TimeSheetPosition(DateTime begin, String id, String comment, Duration duration,
      Status status) {
    id.getClass();
    comment.getClass();
    duration.getClass();
    this.path = ImmutableList.of(id);
    this.comment = comment;
    this.duration = duration;
    this.begin = begin;
    this.status = status;
  }

  /**
   * 
   * @return first path element
   */
  public String getId() {
    return path.get(0);
  }

}
