package info.dt.srv;

import static org.junit.Assert.assertEquals;
import info.dt.data.TimeSheet;

import java.io.File;

import org.joda.time.DateTime;
import org.joda.time.Interval;
import org.joda.time.ReadableInterval;
import org.junit.Test;

public class YamlDateConfigWriterTest {

  @Test
  public void testWrite() throws Exception {
    File file = File.createTempFile("dt-temp", ".yaml");
    YamlDateConfigWriter writer = new YamlDateConfigWriter(file);
    DateTime now = DateTime.now();
    ReadableInterval interval = new Interval(now, now.plusDays(2));
    TimeSheet timeSheet = new DemoDateConfig().getTimeSheet(interval);
    writer.writer(timeSheet);
    YamlDateConfig config = new YamlDateConfig(file.toURI().toURL());
    TimeSheet timeSheet2 = config.getTimeSheet(interval);
    assertEquals(timeSheet.toString(), timeSheet2.toString());

  }

}
