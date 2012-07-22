package info.dt.report;

import static com.google.common.collect.ImmutableList.of;
import static org.junit.Assert.assertEquals;
import info.dt.data.ITimeSheetPosition;
import info.dt.data.TimeSheetPosition;

import java.util.List;

import org.joda.time.DateTime;
import org.joda.time.Duration;
import org.junit.Test;

import com.google.common.collect.ImmutableList;

public class TicketFilterBuilderTest {

  @Test
  public void testConcatDescription() {

    TicketFilterBuilder tfb = new TicketFilterBuilder(null);

    assertTFB(tfb, of("Important (.changes)", "discussion"), "Important (.changes); discussion" //
    );
    assertTFB(tfb, of("Important (.changes)", "discussion", "magic and dangerous but unbelievable", "things") //
        , "Important (.changes); magic and dangerous but unbelievable ; things" //
    );
    assertTFB(
        tfb,
        of("FIXME", "Important (.changes); discussion",
            "Important (.changes); magic and dangerous but unbelievable ; things",
            "Importat hanges; maic things;other things"), "Importat hanges; maic things;other things" //
    );
  }

  @Test
  public void testConcatDescriptionWithOtherSeparator() {
    TicketFilterBuilder tfb = new TicketFilterBuilder(null).setSeparator('#');

    assertTFB(tfb, of("Important changes", "discussion") //
        , "Important changes# discussion");
    assertTFB(tfb, of("FIXME", "Important changes# discussion", "Importat hanges# maic things") //
        , "Importat hanges# maic things");
    assertTFB(tfb,
        of("FIXME", "Important changes# discussion", "Important changes# maic things", "Importat hanges# maic things") //
        , "Important changes# maic things");
  }

  @Test
  public void testConcatDescriptionNo() {

    TicketFilterBuilder tfb = new TicketFilterBuilder(null);

    assertTFB(tfb, of("NonImportant changes discussion") //
        , "NonImportant changes discussion");
  }

  @Test
  public void testConcatDescriptionLength() {

    TicketFilterBuilder tfb = new TicketFilterBuilder(null);
    assertTFB(tfb, of("b", "NonImportant changes discussion") //
        , "b;NonImportant changes discussion");

    assertTFB(tfb, of("b", "NonImportant changes discussion") //
        , "b;NonImportant changes discussion");

    assertTFB(tfb, of("b", "NonImportant changes discussion") //
        , "b;NonImportant changes discussion; ");
  }

  @Test
  public void testConcatDescriptionOptional() {

    TicketFilterBuilder tfb = new TicketFilterBuilder(null);
    assertTFB(tfb, of("NonImportant changes discussion") //
        , "NonImportant changes discussion");

    assertTFB(tfb, of("NonImportant changes discussion", "test") //
        , "NonImportant changes discussion; test");
  }

  @Test
  public void testConcatDescriptionLengthVari() {

    TicketFilterBuilder tfb = new TicketFilterBuilder(null);

    assertTFB(tfb, of("a", "Important changes discussion") //
        , "a;Important changes discussion");

    assertTFB(tfb, of("a", "Important changes discussion", "Non") //
        , "a;Important changes discussion;Non");

    assertTFB(tfb, of("a", "Important changes discussion", "Non") //
        , "a;Important changes discussion;Non");

  }

  @Test
  public void testConcatDescriptionOnly() {

    TicketFilterBuilder tfb = new TicketFilterBuilder(null);

    assertTFB(tfb, of("NonImportant changes discussion") //
        , "NonImportant changes discussion");

    assertTFB(tfb, of("NonImportant changes discussion") //
        , "NonImportant changes discussion");
  }

  @Test
  public void testConcatDescriptionSingle() {

    TicketFilterBuilder tfb = new TicketFilterBuilder(null);

    assertTFB(tfb, of("NonImportant changes", "discussion") //
        , "NonImportant changes ; discussion");
  }

  protected void assertTFB(TicketFilterBuilder tfb, List<String> expected, String comment) {
    DateTime begin = DateTime.now();
    Duration duration = Duration.ZERO;
    Iterable<String> path = ImmutableList.of("001", "abc");
    ITimeSheetPosition pos1 = new TimeSheetPosition(begin, comment, duration, path);
    assertEquals(expected.toString(), tfb.concatDescription(pos1).toString());
  }

}
