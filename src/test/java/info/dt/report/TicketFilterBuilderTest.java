package info.dt.report;

import static org.junit.Assert.assertEquals;
import info.dt.data.TimeSheetPosition;

import org.joda.time.DateTime;
import org.joda.time.Duration;
import org.junit.Test;

import com.google.common.collect.ImmutableList;

public class TicketFilterBuilderTest {

  @Test
  public void testConcatDescription() {

    TicketFilterBuilder tfb = new TicketFilterBuilder(null).setSeparator('#');

    assertTFB(tfb, "Important changes; discussion" //
        , "Important changes; discussion");
    assertTFB(tfb, "Important changes; maic things" //
        , "Important changes;\ndiscussion\nmaic things");
    assertTFB(tfb, "Importat hanges; maic things" //
        , "Important changes;\ndiscussion\nmaic things\nImportat hanges; maic things");

    tfb = new TicketFilterBuilder(null);

    assertTFB(tfb, "Important changes; discussion" //
        , "Important changes\ndiscussion");
    assertTFB(tfb, "Important changes; maic things" //
        , "Important changes\ndiscussion\nmaic things");
    assertTFB(tfb, "Importat hanges; maic things;other things" //
        , "Important changes\ndiscussion\nmaic things\nImportat hanges\nmaic things\nother things");
  }

  // @Test
  // public void testConcatDescriptionShuffel() {
  //
  // TicketFilterBuilder tfb = new TicketFilterBuilder(null);
  //
  // assertTFB(tfb, "NonImportant changes; discussion" //
  // , "NonImportant changes\ndiscussion");
  // assertTFB(tfb, "Important changes; maic things" //
  // , "NonImportant changes\ndiscussion\nImportant changes\n maic things");
  // assertTFB(tfb, "Important changes; other things" //
  // , "Important changes\ndiscussion\nNonImportat changes\nother things");
  // }

  protected void assertTFB(TicketFilterBuilder tfb, String comment, String expected) {
    DateTime begin = DateTime.now();
    Duration duration = Duration.ZERO;
    Iterable<String> path = ImmutableList.of("001", "abc");
    TimeSheetPosition pos1 = new TimeSheetPosition(begin, comment, duration, path);
    assertEquals(expected, tfb.concatDescription(pos1));
  }

}
