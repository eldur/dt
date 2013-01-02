package info.dt.srv;

import info.dt.report.IReportMapping;
import info.dt.report.IReportView;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

import lombok.extern.slf4j.Slf4j;

import com.google.common.collect.Lists;
import com.google.inject.Injector;

@Slf4j
@Singleton
class ContentFiter implements Filter {

  @Inject
  private Injector injector;

  @Inject
  private IReportMapping reportMapping;

  public void init(FilterConfig filterConfig) throws ServletException {
    // do nothing
  }

  public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
      throws IOException, ServletException {

    String report = request.getParameter("r");
    Map<String, Class<? extends IReportView>> reportMapping = this.reportMapping.get();

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
    String hostName = socketAddress.getHostName();
    if (hostName.startsWith("0.0") || hostName.startsWith("local") || hostName.startsWith("127.")) {
      log.warn("bad hostname for remote usage: \"{}\"", hostName);
    }
    request.setAttribute("srvSocket", hostName + ":" + socketAddress.getPort());

    chain.doFilter(request, response);
  }

  public void destroy() {
    // do nothing

  }

}
