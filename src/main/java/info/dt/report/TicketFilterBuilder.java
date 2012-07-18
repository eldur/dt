package info.dt.report;

import info.dt.data.ITimeSheetPosition;
import info.dt.data.TimeSheetPosition;

import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import lombok.Data;
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

  private Comparator<? super CharPosition> charPosComparator = new Comparator<CharPosition>() {

    public int compare(CharPosition arg0, CharPosition arg1) {
      return Integer.valueOf(arg0.getPos()).compareTo(Integer.valueOf(arg1.getPos()));

    }
  };

  @Data
  private static class CharPosition {
    private final int pos;
    private final char character;
  }

  protected List<String> concatDescription(ITimeSheetPosition pos) {

    String desc = pos.getComment();
    String id = pos.getId();
    if (id == null) {
      log.error("key is null ");
    }

    ticketDesc.put(id, desc);

    List<String> lines = Lists.newArrayList(ticketDesc.get(id));

    Multimap<CharPosition, Integer> histogramm = ArrayListMultimap.create();
    for (String line : lines) {
      if (lines.size() == 1 && !line.contains(separatorChar + "")) {
        return ImmutableList.of(line);
      }
      char[] charArray = line.trim().toCharArray();
      for (int i = 0; i < charArray.length; i++) {
        histogramm.put(new CharPosition(i, charArray[i]), Integer.valueOf(1));
      }
    }
    Multimap<Integer, CharPosition> sizeGroupMap = ArrayListMultimap.create();
    for (Entry<CharPosition, Collection<Integer>> entry : histogramm.asMap().entrySet()) {
      sizeGroupMap.put(entry.getValue().size(), entry.getKey());
    }

    int length = 0;
    boolean separatorBreak = false;
    String title = "";
    for (Entry<Integer, Collection<CharPosition>> entry : sizeGroupMap.asMap().entrySet()) {
      List<CharPosition> list = Lists.newArrayList(entry.getValue());
      Collections.sort(list, charPosComparator);
      if (list.size() > length) {
        StringBuilder workTitle = new StringBuilder();
        int index = 0;
        length = list.size();
        for (CharPosition cp : list) {
          if (cp.getPos() == index) {
            if (cp.getCharacter() == separatorChar) {
              separatorBreak = true;
              break;
            }
            workTitle.append(cp.getCharacter());
          } else {
            separatorBreak = false;
            break;
          }
          index++;
        }
        if (separatorBreak) {
          title = workTitle.toString();
        } else {
          title = "FIXME";
        }
      }
    }

    String titleString = title.toString();

    List<String> result = Lists.newArrayList();
    result.add(titleString.trim());
    Collections.sort(lines);
    for (String line : lines) {
      if (line.startsWith(titleString)) {
        line = line.substring(titleString.length());
      }
      if (separatorBreak) {
        for (String s : Splitter.on(separatorChar).split(line)) {
          String trim = s.trim();
          if (trim.length() > 0) {
            result.add(trim);
          }
        }
      } else {
        result.add(line);
      }
    }

    return result;
  }

  public TicketFilterBuilder setSeparator(char separatorChar) {
    this.separatorChar = separatorChar;
    return this;
  }
}
