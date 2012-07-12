package info.dt.report;

import java.lang.reflect.Modifier;
import java.util.Map;
import java.util.Set;

import javax.inject.Singleton;

import org.reflections.Reflections;

import com.google.common.collect.Maps;

@Singleton
public class DefaultReportMapping implements IReportMapping {

  public Map<String, Class<? extends IReportView>> get() {
    Reflections reflections = new Reflections(""); // search everywhere
    Map<String, Class<? extends IReportView>> reportMapping = Maps.newHashMap();
    Set<Class<? extends IReportView>> subTypes = reflections.getSubTypesOf(IReportView.class);
    if (subTypes.isEmpty()) {
      throw new IllegalStateException("no report implementation was found");
    }
    for (Class<? extends IReportView> clazz : subTypes) {
      if (!clazz.isInterface() && !Modifier.isAbstract(clazz.getModifiers())) {
        String reportShortName = getReportShortName(clazz);
        if (reportMapping.get(reportMapping) != null) {
          throw new IllegalStateException("report name is already registered: " + reportShortName);
        }
        reportMapping.put(reportShortName, clazz);
      }
    }
    return reportMapping;
  }

  private String getReportShortName(Class<? extends IReportView> clazz) {
    String simpleName = clazz.getSimpleName();
    simpleName = simpleName.replaceAll("[a-z]", "");
    return simpleName;
  }
}
