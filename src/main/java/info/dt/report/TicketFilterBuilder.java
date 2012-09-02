package info.dt.report;

import info.dt.data.ITimeSheetPosition;
import info.dt.data.TimeSheetPosition;
import info.dt.data.TimeSheetPosition.Status;

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

  private static final String FIXME = "FIXME";

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
      Status status = Status.NONE;
      for (ITimeSheetPosition pos : entry.getValue()) {
        sum = sum.plus(pos.getDuration());
        Status posStatus = pos.getStatus();
        if (posStatus.greaterThen(status)) {
          status = posStatus;
        }

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

      if (pathes.size() > 1 && entry.getValue().size() != 1) {
        lastPath = ImmutableList.of(entry.getKey());
      }
      positions.add(new ReportPosition(lastDate, formatList(description.subList(0, 1)) //
          , description.subList(1, description.size()) //
          , sum, lastPath, pathes, sumOfReport, status));
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

    String desc = removeWhitespace(pos.getComment(), separatorChar);
    String id = removeWhitespace(pos.getId(), separatorChar);
    if (id == null) {
      log.error("key is null ");
    }

    ticketDesc.put(id, desc);

    List<String> lines = Lists.newArrayList(ticketDesc.get(id));

    Multimap<Integer, CharPosition> sizeGroupMap = toCharHistogram(lines);

    boolean separatorBreak = false;
    String title = FIXME;
    for (Entry<Integer, Collection<CharPosition>> entry : sizeGroupMap.asMap().entrySet()) {

      List<CharPosition> list = Lists.newArrayList(entry.getValue());
      Collections.sort(list, charPosComparator);
      StringBuilder workTitle = new StringBuilder();
      int index = 0;

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
      String workTitleString = workTitle.toString().trim();
      if (separatorBreak //
          || lines.size() == 1 //
          || FIXME.equals(title) && workTitleString.length() > 0 //
      ) {
        title = workTitleString;
      }
    }

    String titleString = title.toString();

    List<String> result = Lists.newArrayList();
    result.add(titleString.trim());
    Collections.sort(lines);
    for (String line : lines) {
      line = line.trim();
      if (line.startsWith(titleString)) {
        line = line.substring(titleString.length());
      }
      for (String s : Splitter.on(separatorChar).split(line)) {
        addIfNotEmpty(result, s);
      }
    }

    return result;
  }

  private String removeWhitespace(String string, char separator) {

    return string.replaceAll("[\\W]*" + separator + "[\\W]*", "" + separator);
  }

  private void addIfNotEmpty(List<String> result, String s) {
    String trim = s.trim();
    if (trim.length() > 0 && !result.contains(s)) {
      result.add(trim);
    }
  }

  protected Multimap<Integer, CharPosition> toCharHistogram(List<String> lines) {
    Multimap<CharPosition, Integer> histogramm = ArrayListMultimap.create();
    for (String line : lines) {
      char[] charArray = line.trim().toCharArray();
      for (int i = 0; i < charArray.length; i++) {
        histogramm.put(new CharPosition(i, charArray[i]), Integer.valueOf(1));
      }
    }
    Multimap<Integer, CharPosition> sizeGroupMap = ArrayListMultimap.create();
    for (Entry<CharPosition, Collection<Integer>> entry : histogramm.asMap().entrySet()) {
      sizeGroupMap.put(entry.getValue().size(), entry.getKey());
    }
    return sizeGroupMap;
  }

  public TicketFilterBuilder setSeparator(char separatorChar) {
    this.separatorChar = separatorChar;
    return this;
  }
}
