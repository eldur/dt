package info.dt.data;

import java.io.Serializable;
import java.util.Iterator;
import java.util.List;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode
public class TimeSheet implements Iterable<TimeSheetPosition>, Serializable {
  private static final long serialVersionUID = 6390487023653001503L;

  private final List<TimeSheetPosition> positions;
  private final int year;
  private final int month;
  private final double requiredHours;

  public Iterator<TimeSheetPosition> iterator() {
    return positions.iterator();
  }
}
