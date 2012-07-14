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

    TicketFilterBuilder tfb = new TicketFilterBuilder(null).setSeparator('#');

    assertTFB(tfb, of("Important changes; discussion") //
        , "Important changes; discussion");
    assertTFB(tfb, of("Important changes;", "discussion", "maic things"), "Important changes; maic things" //
    );
    assertTFB(tfb, of("Important changes;", "discussion", "maic things", "Importat hanges; maic things"),
        "Importat hanges; maic things" //
    );

    tfb = new TicketFilterBuilder(null);

    assertTFB(tfb, of("Important changes", "discussion"), "Important changes; discussion" //
    );
    assertTFB(tfb, of("Important changes", "discussion", "maic things"), "Important changes; maic things" //
    );
    assertTFB(tfb,
        of("Important changes", "discussion", "maic things", "Importat hanges", "maic things", "other things"),
        "Importat hanges; maic things;other things" //
    );
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

  protected void assertTFB(TicketFilterBuilder tfb, List<String> expected, String comment) {
    DateTime begin = DateTime.now();
    Duration duration = Duration.ZERO;
    Iterable<String> path = ImmutableList.of("001", "abc");
    ITimeSheetPosition pos1 = new TimeSheetPosition(begin, comment, duration, path);
    assertEquals(expected, tfb.concatDescription(pos1));
  }

}
