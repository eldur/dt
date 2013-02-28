package info.dt.report;

import static org.junit.Assert.assertEquals;

import org.joda.time.DateTime;
import org.joda.time.ReadableInterval;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.DateTimeFormatterBuilder;
import org.junit.Test;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;

public class DemoReportTest {

  @Test
  public void testGetCurrentInterval() {
    DemoReport dr = getReport(DateTime.parse("2012-06-14"));
    ReadableInterval currentPeriod = dr.getCurrentInterval();

    assertEquals("2012-12-01", formatter().print(currentPeriod.getStart()));
    assertEquals("2012-12-31", formatter().print(currentPeriod.getEnd()));

  }

  @Test
  public void testGetCurrentIntervalVari() {
    DemoReport dr = getReport(DateTime.parse("2012-07-1"));
    ReadableInterval currentPeriod = dr.getCurrentInterval();

    assertEquals("2012-12-01", formatter().print(currentPeriod.getStart()));
    assertEquals("2012-12-31", formatter().print(currentPeriod.getEnd()));

  }

  protected DateTimeFormatter formatter() {
    return new DateTimeFormatterBuilder()//
        .appendYear(4, 4) //
        .appendLiteral("-") //
        .appendMonthOfYear(2) //
        .appendLiteral("-") //
        .appendDayOfMonth(2) //
        .toFormatter();
  }

  protected DemoReport getReport(final DateTime instance) {
    Injector injector = Guice.createInjector(new AbstractModule() {

      @Override
      protected void configure() {
        bind(DateTime.class).toInstance(instance);

      }
    });
    DemoReport dr = injector.getInstance(DemoReport.class);
    return dr;
  }
}
