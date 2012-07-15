package info.dt.srv;

import flexjson.JSONSerializer;
import flexjson.transformer.MapTransformer;
import info.dt.data.TimeSheet;
import info.dt.report.IReportPosition;
import info.dt.report.IReportView;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.inject.Inject;

import org.joda.time.Duration;
import org.joda.time.format.PeriodFormatter;
import org.joda.time.format.PeriodFormatterBuilder;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.common.hash.Hashing;

public class JsonSeri implements IJsonSerializer {

  @Inject
  private IReportView reportView;

  public String toJson(TimeSheet timeSheet, Set<String> idsOnClient) {
    List<IReportPosition> positions = reportView.toReportPositions(timeSheet);

    Set<String> hashes = Sets.newHashSet();

    for (IReportPosition pos : positions) {
      String hash = hash(pos, timeSheet);
      hashes.add(hash);
      if (pos.getPathes().size() > 1) {
        for (Entry<List<String>, Duration> entry : pos.getPathes().entrySet()) {
          String subhash = hash(entry.getKey(), entry.getValue(), pos);
          hashes.add(subhash);
        }
      }
    }

    if (hashes.equals(idsOnClient)) {
      return "";
    }

    JSONSerializer seri = new JSONSerializer();
    seri.transform(new Aasf(timeSheet), IReportPosition.class);
    return seri.deepSerialize(positions);
  }

  private static PeriodFormatter newPeriodFormatter() {
    PeriodFormatter formatter = new PeriodFormatterBuilder() //
        .appendHours() //
        .appendSuffix("h") //
        .appendSeparator(" ") //
        .appendMinutes() //
        .appendSuffix("m").toFormatter();
    return formatter;
  }

  private static String hash(Object... objects) {
    StringBuilder sb = new StringBuilder();
    for (Object o : objects) {
      String string = o.toString();
      sb.append(string);
    }
    return Hashing.sha1().hashString(sb.toString()).toString();
  }

  private static class Aasf extends MapTransformer {

    private TimeSheet timeSheet;

    public Aasf(TimeSheet timeSheet) {
      this.timeSheet = timeSheet;
    }

    @Override
    public void transform(Object object) {
      IReportPosition timeSheetPosition = (IReportPosition) object;
      Map<String, Object> map = Maps.newHashMap();
      map.put("htmlid", hash(timeSheetPosition, timeSheet));
      String id = timeSheetPosition.getId();
      map.put("id", id);
      map.put("path", formatPath(timeSheetPosition.getPath(), id));
      map.put("title", timeSheetPosition.getTitle());
      map.put("comment", timeSheetPosition.getComment());
      map.put("duration", toDuration(timeSheetPosition.getDuration()));
      map.put("durationPercentage", timeSheetPosition.getDurationPercentage());
      List<Map<String, String>> list = Lists.newArrayList();
      for (Entry<List<String>, Duration> entry : timeSheetPosition.getPathes().entrySet()) {
        Map<String, String> submap = Maps.newHashMap();
        submap.put("htmlid", hash(entry.getKey(), entry.getValue(), timeSheetPosition));
        submap.put("duration", toDuration(entry.getValue()));
        int subPercentage = (int) (100 * entry.getValue().getMillis() / timeSheetPosition.getDuration().getMillis());
        submap.put("durationPercentage", subPercentage + "");
        submap.put("path", formatPath(entry.getKey(), id));

        list.add(submap);
      }
      map.put("sub", list);
      super.transform(map);

    }

    public String formatPath(Iterable<String> path, String id) {
      String join = Joiner.on("-").join(path);
      int length = id.length();
      if (join.length() > length) {
        return join.substring(length + 1);
      } else {
        return "";
      }
    }

    private String toDuration(Duration value) {
      return newPeriodFormatter().print(value.toPeriod());
    }

  }
}
