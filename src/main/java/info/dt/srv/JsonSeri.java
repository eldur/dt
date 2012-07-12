package info.dt.srv;

import flexjson.JSONSerializer;
import flexjson.transformer.MapTransformer;
import info.dt.data.TimeSheet;
import info.dt.report.IReportPosition;
import info.dt.report.IReportView;

import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.inject.Inject;

import org.joda.time.format.PeriodFormatter;
import org.joda.time.format.PeriodFormatterBuilder;

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
      hashes.add(hash(pos, timeSheet));
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

  private static String hash(IReportPosition timeSheetPosition, TimeSheet timeSheet) {
    return Hashing.sha1().hashString(timeSheetPosition.toString() + timeSheet.toString()).toString();
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
      map.put("id", timeSheetPosition.getId());
      map.put("label", timeSheetPosition.getLabel());
      map.put("title", timeSheetPosition.getTitle());
      map.put("comment", timeSheetPosition.getComment());
      map.put("duration", toDuration(timeSheetPosition));
      List<Map<String, String>> list = Lists.newArrayList();
      Map<String, String> submap = Maps.newHashMap();
      submap.put("duration", toDuration(timeSheetPosition));
      submap.put("label", "abc");
      // list.add(submap);
      // list.add(submap);
      map.put("sub", list);
      super.transform(map);

    }

    private String toDuration(IReportPosition value) {
      return newPeriodFormatter().print(value.getDuration().toPeriod());
    }

  }
}
