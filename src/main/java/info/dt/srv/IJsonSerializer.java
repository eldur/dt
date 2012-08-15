package info.dt.srv;

import info.dt.data.TimeSheet;

import java.util.Set;

import org.joda.time.ReadableInterval;

public interface IJsonSerializer {

  String toJson(TimeSheet timeSheet, Set<String> idsOnClient, ReadableInterval currentInterval);

}
