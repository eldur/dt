package info.dt.srv;

import flexjson.JSONSerializer;
import info.dt.data.TimeSheet;

public class JsonSeri implements IJsonSerializer {

  public String toJson(TimeSheet timeSheet) {
    JSONSerializer seri = new JSONSerializer();

    return seri.serialize(timeSheet);
  }
}
