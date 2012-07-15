package info.dt.report;

import info.dt.data.ITimeSheetPosition;
import info.dt.data.TimeSheetPosition;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import lombok.extern.slf4j.Slf4j;

import org.joda.time.DateTime;
import org.joda.time.Duration;

import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;

@Slf4j
public class TicketFilterBuilder {

  private final List<IReportPosition> positions = Lists.newArrayList();

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

  public List<IReportPosition> getResult() {
    Duration sumOfReport = Duration.ZERO;
    Multimap<String, ITimeSheetPosition> ticketSum = ArrayListMultimap.create();
    for (TimeSheetPosition pos : t) {
      if (filter(pos)) {
        Duration duration = pos.getDuration();
        sumOfReport = sumOfReport.plus(duration);
        ticketSum.put(pos.getId(), pos);
      }
    }

    List<String> lastPath = ImmutableList.of();
    for (Entry<String, Collection<ITimeSheetPosition>> entry : ticketSum.asMap().entrySet()) {
      Duration sum = new Duration(0);
      List<String> description = ImmutableList.of();
      DateTime lastDate = null;
      Map<List<String>, Duration> pathes = Maps.newHashMap();
      for (ITimeSheetPosition pos : entry.getValue()) {
        sum = sum.plus(pos.getDuration());
        description = concatDescription(pos);
        List<String> path = pos.getPath();
        lastPath = path;
        Duration duration = pathes.get(path);
        lastDate = pos.getBegin();
        if (duration == null) {
          duration = pos.getDuration();
        } else {
          duration = duration.plus(pos.getDuration());
        }
        pathes.put(pos.getPath(), duration);
      }

      if (noDescription) {
        description = ImmutableList.of();
      }

      if (entry.getValue().size() != 1) {
        lastPath = ImmutableList.of(entry.getKey());
      }
      positions.add(new ReportPosition(lastDate, formatList(description.subList(0, 1)), formatList(description.subList(
          1, description.size())), sum, lastPath, pathes, sumOfReport));
    }
    return positions;
  }

  private String formatList(List<String> subList) {
    return Joiner.on("; ").join(subList); // TODO mv to serializer
  }

  private final Multimap<String, String> ticketDesc = HashMultimap.create();

  private char separatorChar = ';';

  protected List<String> concatDescription(ITimeSheetPosition pos) {

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
    List<String> result = Lists.newArrayList();
    result.add(titleString.trim());
    if (lines.size() > 0) {
      Collections.sort(lines);
      for (String line : lines) {
        String replaceFirst = line.replaceFirst("^" + titleString, "");
        for (String s : Splitter.on(separatorChar).split(replaceFirst)) {
          String trim = s.trim();
          if (trim.length() > 0) {
            result.add(trim);
          }
        }

      }

    }

    return result;
  }

  public TicketFilterBuilder setSeparator(char separatorChar) {
    this.separatorChar = separatorChar;
    return this;
  }
}
