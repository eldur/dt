package info.dt.srv;

import info.dt.data.TimeSheet;

import java.util.Set;

public interface IJsonSerializer {

  String toJson(TimeSheet timeSheet, Set<String> idsOnClient);

}
