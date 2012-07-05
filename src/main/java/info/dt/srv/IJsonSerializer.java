package info.dt.srv;

import info.dt.data.TimeSheet;

public interface IJsonSerializer {

  String toJson(TimeSheet timeSheet);

}
