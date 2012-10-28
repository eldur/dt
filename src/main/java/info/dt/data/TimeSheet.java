package info.dt.data;

import java.io.Serializable;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import lombok.Data;

import com.google.common.base.Objects;

@Data
public class TimeSheet implements Iterable<TimeSheetPosition>, Serializable {
  private static final long serialVersionUID = 9187423390878942842L;

  private final List<TimeSheetPosition> positions;
  private final int year;
  private final int month;

  private int hashCode;

  public TimeSheet(List<TimeSheetPosition> positions, int year, int month) {
    this.positions = positions;
    this.year = year;
    this.month = month;
    hashCode = Objects.hashCode(positions, year, month);
  }

  public Iterator<TimeSheetPosition> iterator() {
    return positions.iterator();
  }

  public List<TimeSheetPosition> getPositions() {
    return Collections.unmodifiableList(positions);
  }

  @Override
  public int hashCode() {
    return hashCode;
  }

}
