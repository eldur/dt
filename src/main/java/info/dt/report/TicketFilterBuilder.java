package info.dt.report;

import info.dt.data.TimeSheetPosition;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map.Entry;

import lombok.extern.slf4j.Slf4j;

import org.joda.time.DateTime;
import org.joda.time.Duration;

import com.google.common.base.Splitter;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;

@Slf4j
public class TicketFilterBuilder {

  private final List<TimeSheetPosition> positions = Lists.newArrayList();

  private final Iterable<TimeSheetPosition> t;
  private boolean noDescription = false;

  public TicketFilterBuilder(Iterable<TimeSheetPosition> t) {
    this.t = t;
  }

  public TicketFilterBuilder noDescription() {
    noDescription = true;
    return this;
  }

  protected boolean filter(TimeSheetPosition pos) {
    return true; // do not filter
  }

  /**
   * 
   * TODO return IReportPosition
   */
  public List<TimeSheetPosition> getResult() {
    Multimap<String, TimeSheetPosition> ticketSum = ArrayListMultimap.create();
    for (TimeSheetPosition pos : t) {
      if (filter(pos)) {
        ticketSum.put(pos.getId(), pos);
      }
    }
    for (Entry<String, Collection<TimeSheetPosition>> entry : ticketSum.asMap().entrySet()) {
      Duration sum = new Duration(0);
      String description = "";
      String activity = "";
      for (TimeSheetPosition pos : entry.getValue()) {
        sum = sum.plus(pos.getDuration());
        description = getDescription(pos);
        activity = getActivity(pos);
      }

      if (noDescription) {
        description = "";
      }
      positions.add(new TimeSheetPosition(DateTime.now(), activity, description, sum));
    }
    return positions;
  }

  protected String getActivity(TimeSheetPosition pos) {
    return pos.getId();
  }

  protected String getDescription(TimeSheetPosition pos) {
    return pos.getComment();
  }

  private final Multimap<String, String> ticketDesc = HashMultimap.create();

  private char separatorChar = ';';

  protected String concatDescription(TimeSheetPosition pos) {

    String desc = pos.getComment();
    String id = pos.getId();
    if (id == null) {
      log.error("key is null ");
    }

    ticketDesc.put(id, desc);
    StringBuilder title = new StringBuilder();
    String titleBuff = null;
    List<String> lines = Lists.newArrayList(ticketDesc.get(id));

    for (String line : lines) {
      if (titleBuff == null) {

        titleBuff = line.trim();
        if (lines.size() == 1) {
          title.append(titleBuff);
        }
        continue;
      }
      char[] charArray = line.trim().toCharArray();
      char[] charArray2 = titleBuff.toCharArray();
      String newTile = title.toString();
      boolean notFound = true;
      for (int i = 0; i < charArray.length; i++) {
        char a;
        if (i >= charArray2.length) {
          a = 'δ';
        } else {
          a = charArray2[i];

        }
        char b = charArray[i];
        if (a == b && newTile.length() <= i && notFound) {
          title.append(b);
        } else {
          notFound = false;
        }
      }

      titleBuff = line;
    }
    String titleString = title.toString();
    titleString = titleString.replace(separatorChar, '\n').replaceAll("\n.*", "");
    String partsOf = "";
    if (lines.size() > 0) {
      StringBuilder parts = new StringBuilder();
      Collections.sort(lines);
      for (String line : lines) {
        String replaceFirst = line.replaceFirst("^" + titleString, "");
        for (String s : Splitter.on(separatorChar).split(replaceFirst)) {
          parts.append(s.trim()).append("\n");
        }

        parts.trimToSize();
      }
      partsOf = parts.toString();

      partsOf = partsOf.substring(0, partsOf.length() - 1);
    }
    if (partsOf.length() > 0) {
      partsOf = "\n" + partsOf;
    }

    return (titleString.trim() + partsOf).replaceAll("[\n]{2}", "\n");

  }

  public TicketFilterBuilder setSeparator(char separatorChar) {
    this.separatorChar = separatorChar;
    return this;
  }
}
