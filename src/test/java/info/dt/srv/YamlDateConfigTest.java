package info.dt.srv;

import static org.junit.Assert.assertEquals;
import info.dt.data.IDateConfig;
import info.dt.data.TimeSheet;
import info.dt.data.TimeSheetPosition;

import java.util.List;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.junit.Test;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.io.Resources;

public class YamlDateConfigTest {

  @Test
  public void testRead() {

    TimeSheetPosition p1 = createPos("2012-06-01 10:30", 30, "phonecall", "Fruit Dealer 1");
    TimeSheetPosition p2 = createPos("2012-06-01 11:00", 60, "sell apple", "Fruit Dealer 2");
    TimeSheetPosition p3 = createPos("2012-06-02 10:30", 30, "check stock market", "stocks", "misc");

    List<TimeSheetPosition> positions = ImmutableList.of(p1, p2, p3);
    int year = 2012;
    int month = 6;
    double requiredHours = -1;
    TimeSheet expected = new TimeSheet(positions, year, month, requiredHours);
    IDateConfig dc = new YamlDateConfig(ImmutableList.of(Resources.getResource("test.yaml")));
    TimeSheet timeSheet = dc.getTimeSheet(year, month);
    assertEquals(fmt(expected), fmt(timeSheet));

  }

  private String fmt(TimeSheet tSheet) {
    return tSheet.toString().replace("(", "\n(");
  }

  private TimeSheetPosition createPos(String beginStr, long minutes, String comment, String... labels) {
    DateTimeFormatter fmt = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm");

    DateTime begin = DateTime.parse(beginStr, fmt);
    TimeSheetPosition p1 = new TimeSheetPosition(begin, comment, minutes, Lists.newArrayList(labels));
    return p1;
  }
}
