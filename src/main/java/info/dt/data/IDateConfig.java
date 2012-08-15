package info.dt.data;

import org.joda.time.ReadableInterval;

public interface IDateConfig {
  TimeSheet getTimeSheet(ReadableInterval interval);
}
