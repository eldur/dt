package info.dt.srv;

import info.dt.report.IReportView;

import java.io.IOException;
import java.lang.reflect.Modifier;
import java.net.InetSocketAddress;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.inject.Singleton;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

import lombok.extern.slf4j.Slf4j;

import org.reflections.Reflections;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.inject.Inject;
import com.google.inject.Injector;

@Slf4j
@Singleton
class ContentFiter implements Filter {

  @Inject
  private Injector injector;

  public void init(FilterConfig filterConfig) throws ServletException {
    // do nothing
  }

  public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException,
      ServletException {

    String report = request.getParameter("r");
    Map<String, Class<? extends IReportView>> reportMapping = Maps.newHashMap();
    Reflections reflections = new Reflections(""); // search everywhere

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

    IReportView reportView = null;
    if (report != null && report.length() > 0) {
      Class<? extends IReportView> type = reportMapping.get(report);
      if (type == null) {
        log.warn("no report for {}", report);
      } else {
        reportView = injector.getInstance(type);
        request.setAttribute("reportID", report);
      }
    }
    List<String> list = Lists.newArrayList(reportMapping.keySet());
    Collections.sort(list);
    request.setAttribute("allReports", list);
    request.setAttribute("report", reportView);
    String view = "default";
    if (reportView != null) {
      view = reportView.getId();
    }
    request.setAttribute("view", view);

    InetSocketAddress socketAddress = injector.getInstance(InetSocketAddress.class);
    request.setAttribute("srvSocket", socketAddress.getHostName() + ":" + socketAddress.getPort());

    chain.doFilter(request, response);
  }

  private String getReportShortName(Class<? extends IReportView> clazz) {
    String simpleName = clazz.getSimpleName();
    simpleName = simpleName.replaceAll("[a-z]", "");
    return simpleName;
  }

  public void destroy() {
    // do nothing

  }

}
