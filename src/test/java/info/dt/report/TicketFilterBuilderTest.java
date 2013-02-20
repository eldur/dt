package info.dt.report;

import static org.junit.Assert.assertEquals;
import info.dt.data.ITimeSheetPosition;
import info.dt.data.Status;
import info.dt.data.TimeSheetPosition;

import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;

import org.joda.time.DateTime;
import org.joda.time.Duration;
import org.junit.Test;

import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

public class TicketFilterBuilderTest {

  private TicketFilterBuilder tfb = new TicketFilterBuilder(null);

  private static List<String> of(String... elements) {
    List<String> elmz = Lists.newArrayList(elements);
    Iterable<String> filtered = Iterables.filter(elmz, new Predicate<String>() {

      public boolean apply(@Nullable String input) {
        return input != null;
      }
    });

    return ImmutableList.copyOf(filtered);
  }

  @Test
  public void testConcatDescription() {

    TicketFilterBuilder tfb = new TicketFilterBuilder(null);

    assertTFB(tfb, of(null //
        , "Important (.changes)" //
        , "discussion") //
        , "Important (.changes); discussion" //
    );
    assertTFB(tfb, of(null //
        , "Important (.changes)" //
        , "discussion" //
        , "magic and dangerous but unbelievable" //
        , "things") //
        , "Important (.changes); magic and dangerous but unbelievable ; things" //
    );
    assertTFB(
        tfb,
        of("FIXME", "Importa", "nt (.changes)", "discussion",
            "magic and dangerous but unbelievable" //
            , "things", "t hanges", "maic things", "other things"),
        "Importat hanges; maic things;other things" //
    );
  }

  @Test
  public void testConcatDescriptionWithOtherSeparator() {
    TicketFilterBuilder tfb = new TicketFilterBuilder(null).setSeparator('#');

    assertTFB(tfb, of("Important changes", "discussion") //
        , "Important changes# discussion");
    assertTFB(tfb, of("FIXME", "Importa", "nt changes", "discussion", "t hanges", "maic things") //
        , "Importat hanges# maic things");

    assertTFB(tfb, of("FIXME", "Importa", "nt changes", "discussion", "maic things", "t hanges")//
        , "Important changes# maic things");
  }

  @Test
  public void testConcatDescriptionNo() {

    assertTFB(tfb, of("NonImportant changes discussion") //
        , "NonImportant changes discussion");
  }

  @Test
  public void testConcatDescriptionLength() {

    assertTFB(tfb, of("b", "NonImportant changes discussion") //
        , "b;NonImportant changes discussion");

    assertTFB(tfb, of("b", "NonImportant changes discussion") //
        , "b;NonImportant changes discussion");

    assertTFB(tfb, of("b", "NonImportant changes discussion") //
        , "b;NonImportant changes discussion; ");
  }

  @Test
  public void testConcatDescriptionOptional() {

    assertTFB(tfb, of("NonImportant changes discussion") //
        , "NonImportant changes discussion");

    assertTFB(tfb, of("NonImportant changes discussion", "test") //
        , "NonImportant changes discussion; test");
  }

  @Test
  public void testConcatDescriptionLengthVari() {

    assertTFB(tfb, of("a", "Important changes discussion") //
        , "a;Important changes discussion");

    assertTFB(tfb, of("a", "Important changes discussion", "Non") //
        , "a; Important changes discussion; Non");

    assertTFB(tfb, of("a", "Important changes discussion", "Non") //
        , "a ;Important changes discussion;Non ");

    assertTFB(tfb, of("a", "Important changes discussion", "Non") //
        , "a\u205F;Important changes discussion;Non ");

  }

  @Test
  public void testConcatDescriptionOnly() {

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
    ITimeSheetPosition pos1 = new TimeSheetPosition(begin, comment, duration, path, Status.NONE);
    assertEquals(expected.toString(), tfb.concatDescription(pos1).toString());
  }

  @Test
  public void testGetResult() {
    DateTime now = DateTime.now();
    Duration hours = Duration.standardHours(1);
    TimeSheetPosition p1 = new TimeSheetPosition(now, "b;a", hours, ImmutableList.<String> of("b",
        "c"), Status.NONE);
    tfb = new TicketFilterBuilder(ImmutableList.of(p1));

    TimeSheetPosition p2 = new TimeSheetPosition(now, "b;a", hours, ImmutableList.<String> of("b",
        "c"), Status.NONE);
    Map<List<String>, Duration> pathes = ImmutableMap.of((List<String>) ImmutableList.of("b", "c"),
        Duration.standardHours(2));
    DateTime begin = now;
    List<String> commentLines = Lists.newArrayList("a");
    Duration duration = Duration.standardSeconds(1);
    List<String> path = Lists.newArrayList("b");
    ReportPosition reportPos = new ReportPosition(begin, "b", commentLines, duration, path, pathes,
        Duration.standardHours(2), Status.NONE);

    tfb = new TicketFilterBuilder(ImmutableList.of(p1, p2));
    assertEquals(Lists.newArrayList(reportPos).toString(), tfb.getResult().toString());

    // TODO play with more elements
  }

}
