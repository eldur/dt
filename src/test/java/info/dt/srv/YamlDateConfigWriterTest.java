package info.dt.srv;

import static org.junit.Assert.assertEquals;
import info.dt.data.TimeSheet;

import org.joda.time.DateTime;
import org.joda.time.Interval;
import org.joda.time.ReadableInterval;
import org.junit.Test;

import com.google.common.base.Objects;
import com.google.common.io.Resources;

public class YamlDateConfigWriterTest {

  @Test
  public void testWrite() {

    DateTime now = DateTime.now();
    ReadableInterval interval = new Interval(now, now.plusDays(2));
    TimeSheet timeSheet = new DemoDateConfig().getTimeSheet(interval);

    YamlDateConfig config = new YamlDateConfig(Resources.getResource("2012-09-27.yaml.yml"));
    TimeSheet timeSheet2 = config.getTimeSheet(interval);
    assertTimeSheetEquals(timeSheet, timeSheet2);

  }

  private void assertTimeSheetEquals(TimeSheet timeSheet, TimeSheet timeSheet2) {
    assertEquals(format(timeSheet), format(timeSheet2));

  }

  private String format(TimeSheet timeSheet) {
    return Objects.toStringHelper(timeSheet) //
        .addValue(timeSheet.getPositions()) //
        .toString()//
    ;
  }

}
