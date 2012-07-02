package info.dt.srv;

import java.io.IOException;

import javax.inject.Singleton;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

import lombok.extern.slf4j.Slf4j;

@Singleton
@Slf4j
public class UrlFilter implements Filter {

  public void init(FilterConfig filterConfig) throws ServletException {
    // do nothing
  }

  public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException,
      ServletException {
    HttpServletRequest req = (HttpServletRequest) request;
    String url = req.getRequestURI().toString();
    if (!url.contains("ws")) {
      if (!(url.contains("assets") || url.contains("/jsp/")) || "/".equals(url)) {
        log.debug("filter url {} ", url);
        RequestDispatcher dispatcher = req.getRequestDispatcher("/jsp/index.jsp");
        dispatcher.forward(req, response);
        return;
      }
    }
    chain.doFilter(request, response);

  }

  public void destroy() {
    // do nothing
  }

}
