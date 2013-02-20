package info.dt.report;

import info.dt.data.ITimeSheetPosition;
import info.dt.data.Status;
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
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;

@Slf4j
public class TicketFilterBuilder {

  private static final String FIXME = "FIXME";

  String whitespace_chars = "" //
      + "\\u0009" // CHARACTER TABULATION
      + "\\u000A" // LINE FEED (LF)
      + "\\u000B" // LINE TABULATION
      + "\\u000C" // FORM FEED (FF)
      + "\\u000D" // CARRIAGE RETURN (CR)
      + "\\u0020" // SPACE
      + "\\u0085" // NEXT LINE (NEL)
      + "\\u00A0" // NO-BREAK SPACE
      + "\\u1680" // OGHAM SPACE MARK
      + "\\u180E" // MONGOLIAN VOWEL SEPARATOR
      + "\\u2000" // EN QUAD
      + "\\u2001" // EM QUAD
      + "\\u2002" // EN SPACE
      + "\\u2003" // EM SPACE
      + "\\u2004" // THREE-PER-EM SPACE
      + "\\u2005" // FOUR-PER-EM SPACE
      + "\\u2006" // SIX-PER-EM SPACE
      + "\\u2007" // FIGURE SPACE
      + "\\u2008" // PUNCTUATION SPACE
      + "\\u2009" // THIN SPACE
      + "\\u200A" // HAIR SPACE
      + "\\u2028" // LINE SEPARATOR
      + "\\u2029" // PARAGRAPH SEPARATOR
      + "\\u202F" // NARROW NO-BREAK SPACE
      + "\\u205F" // MEDIUM MATHEMATICAL SPACE
      + "\\u3000" // IDEOGRAPHIC SPACE
  ;

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

  private static Comparator<? super CharPosition> charPosComparator = new Comparator<CharPosition>() {

    public int compare(CharPosition arg0, CharPosition arg1) {
      return Integer.valueOf(arg0.getPos()).compareTo(Integer.valueOf(arg1.getPos()));

    }
  };

  @Data
  private static class CharPosition {
    private final int pos;
    private final char character;

    public static CharPosition valueOf(int i, char c) {

      return new CharPosition(i, c);
    }
  }

  private static final Cache<ITimeSheetPosition, List<String>> positionListCache = CacheBuilder
      .newBuilder().maximumSize(2000).build();

  protected List<String> concatDescription(ITimeSheetPosition pos) {

    List<String> ifPresent = positionListCache.getIfPresent(pos);
    if (ifPresent != null) {
      return ifPresent;
    } else {
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

      final String titleString = title.toString().trim();

      List<String> result = Lists.newArrayList();
      result.add(titleString);
      Collections.sort(lines);
      for (final String line : lines) {
        String trimLine = line.trim();
        if (trimLine.startsWith(titleString)) {
          trimLine = trimLine.substring(titleString.length());
          if (!(trimLine.startsWith(separatorChar + "") //
              || FIXME.equals(result.get(0)) //
          || trimLine.isEmpty() //
          )) {
            result.add(0, FIXME);
          }
        }
        for (String s : Splitter.on(separatorChar).split(trimLine)) {
          addIfNotEmpty(result, s);
        }
      }
      positionListCache.put(pos, result);
      return result;
    }
  }

  private String removeWhitespace(String string, char separator) {
    return string.replaceAll("[" + whitespace_chars + "]*" + separator + "[" + whitespace_chars
        + "]*", "" + separator);
  }

  private void addIfNotEmpty(List<String> result, String s) {
    String trim = s.trim();
    trim = trim.replaceAll("[" + whitespace_chars + "]+$", "").replaceAll(
        "^[" + whitespace_chars + "]+", "");
    if (trim.length() > 0 && !result.contains(s)) {
      result.add(trim);
    }
  }

  protected Multimap<Integer, CharPosition> toCharHistogram(List<String> lines) {
    Map<CharPosition, Integer> histogramm = Maps.newHashMap();
    for (String line : lines) {
      char[] charArray = line.trim().toCharArray();
      for (int i = 0; i < charArray.length; i++) {
        CharPosition pos = CharPosition.valueOf(i, charArray[i]);
        Integer integer = histogramm.get(pos);
        if (integer == null) {
          histogramm.put(pos, Integer.valueOf(1));
        } else {
          histogramm.put(pos, integer.intValue() + 1);
        }
      }
    }
    Multimap<Integer, CharPosition> sizeGroupMap = ArrayListMultimap.create();
    for (Entry<CharPosition, Integer> entry : histogramm.entrySet()) {
      sizeGroupMap.put(entry.getValue(), entry.getKey());
    }
    return sizeGroupMap;
  }

  public TicketFilterBuilder setSeparator(char separatorChar) {
    this.separatorChar = separatorChar;
    return this;
  }
}
