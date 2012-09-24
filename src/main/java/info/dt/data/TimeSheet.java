package info.dt.data;

import java.io.Serializable;
import java.util.Iterator;
import java.util.List;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode
public class TimeSheet implements Iterable<TimeSheetPosition>, Serializable {
  private static final long serialVersionUID = 9187423390878942842L;

  private final List<TimeSheetPosition> positions;
  private final int year;
  private final int month;

  public Iterator<TimeSheetPosition> iterator() {
    return positions.iterator();
  }
}
