package info.dt.srv;

import static org.junit.Assert.assertEquals;
import info.dt.data.IDateConfig;
import info.dt.data.Status;
import info.dt.data.TimeSheet;
import info.dt.data.TimeSheetPosition;

import java.util.List;

import org.joda.time.DateTime;
import org.joda.time.Interval;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.junit.Test;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.io.Resources;

public class YamlDateConfigTest {

  @Test
  public void testRead() {

    TimeSheetPosition p1 = createPos("2012-06-01 10:30", 45, "phonecall", "Food Dealer 1", "xy");
    TimeSheetPosition p2 = createPos("2012-06-01 11:30", 30, "sell an apple", "Fruit Dealer 2");
    TimeSheetPosition p3 = createPos("2012-06-02 10:30", 30, "check stock market", "001", "stock",
        "xy");
    TimeSheetPosition p4 = createPos("2012-06-02 11:00", 60, "check stock market; meeting", "001",
        "meeting", "xy");

    List<TimeSheetPosition> positions = ImmutableList.of(p1, p2, p3, p4);
    int year = 2012;
    int month = 6;
    Interval interval = new Interval(DateTime.parse(year + "-" + month + "-01") //
        , DateTime.parse(year + "-" + month + "-30"));
    TimeSheet expected = new TimeSheet(positions, year, month);
    IDateConfig dc = new YamlDateConfig(ImmutableList.of(Resources.getResource("test.yaml")));
    TimeSheet timeSheet = dc.getTimeSheet(interval);
    assertEquals(fmt(expected), fmt(timeSheet));

  }

  private String fmt(TimeSheet tSheet) {
    return tSheet.toString().replace("(", "\n(");
  }

  private TimeSheetPosition createPos(String beginStr, long minutes, String comment,
      String... labels) {
    DateTimeFormatter fmt = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm");

    DateTime begin = DateTime.parse(beginStr, fmt);
    TimeSheetPosition p1 = new TimeSheetPosition(begin, comment, minutes,
        Lists.newArrayList(labels), Status.NONE);
    return p1;
  }
}
