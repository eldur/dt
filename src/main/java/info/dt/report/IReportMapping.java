package info.dt.report;

import java.util.Map;

import javax.inject.Provider;

public interface IReportMapping extends Provider<Map<String, Class<? extends IReportView>>> {

}
