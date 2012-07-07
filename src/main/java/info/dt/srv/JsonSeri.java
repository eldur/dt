package info.dt.srv;

import flexjson.JSONSerializer;
import flexjson.transformer.MapTransformer;
import info.dt.data.TimeSheet;
import info.dt.data.TimeSheetPosition;

import java.util.Map;

import org.joda.time.format.PeriodFormatter;
import org.joda.time.format.PeriodFormatterBuilder;

import com.mycila.inject.internal.guava.collect.Maps;

public class JsonSeri implements IJsonSerializer {

  public String toJson(TimeSheet timeSheet) {
    JSONSerializer seri = new JSONSerializer();
    seri.transform(new Aasf(), TimeSheetPosition.class);
    return seri.serialize(timeSheet);
  }

  private static PeriodFormatter newPeriodFormatter() {
    PeriodFormatter formatter = new PeriodFormatterBuilder() //
        .appendHours() //
        .appendSuffix("h") //
        .appendSeparator("&nbsp;") //
        .appendMinutes() //
        .appendSuffix("m").toFormatter();
    return formatter;
  }

  private static class Aasf extends MapTransformer {

    @Override
    public void transform(Object object) {
      TimeSheetPosition timeSheetPosition = (TimeSheetPosition) object;
      Map<String, Object> map = Maps.newHashMap();
      map.put("label", timeSheetPosition.getMainLabel());
      map.put("title", timeSheetPosition.getTitle());
      map.put("comment", timeSheetPosition.getComment());
      map.put("duration", toDuration(timeSheetPosition));
      Map<String, String> submap = Maps.newHashMap();
      submap.put("duration", toDuration(timeSheetPosition));
      submap.put("label", "abc");
      map.put("sub", submap);
      super.transform(map);

    }

    private String toDuration(TimeSheetPosition value) {
      return newPeriodFormatter().print(value.getDuration().toPeriod());
    }

  }
}
