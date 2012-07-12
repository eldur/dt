package info.dt.srv;

import info.dt.report.IReportMapping;
import info.dt.report.IReportView;

import java.io.IOException;

import javax.inject.Inject;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import lombok.extern.slf4j.Slf4j;

import org.eclipse.jetty.websocket.WebSocket;
import org.eclipse.jetty.websocket.WebSocketFactory;

import com.google.inject.AbstractModule;
import com.google.inject.Injector;

@Slf4j
public class WebSocketServlet extends HttpServlet {

  private static final long serialVersionUID = 1L;
  private WebSocketFactory wsFactory;

  @Inject
  private Injector injector;

  @Inject
  private IReportMapping reportMapping;

  @Override
  public void init() throws ServletException {
    wsFactory = new WebSocketFactory(new WebSocketFactory.Acceptor() {
      public boolean checkOrigin(HttpServletRequest request, String origin) {
        return true;
      }

      public WebSocket doWebSocketConnect(HttpServletRequest request, String protocol) {
        Client instance = null;
        try {
          Class<? extends IReportView> tempReport = reportMapping.get().get(protocol);
          if (tempReport == null) {
            // XXX not very nice
            tempReport = reportMapping.get().entrySet().iterator().next().getValue();
          }
          final Class<? extends IReportView> report = tempReport;
          Injector childInjector = injector.createChildInjector(new AbstractModule() {

            @Override
            protected void configure() {
              bind(IReportView.class).to(report);
              bind(IJsonSerializer.class).to(JsonSeri.class);
            }
          });
          instance = childInjector.getInstance(Client.class);
        } catch (RuntimeException e) {
          log.error("", e);
        }
        instance.getClass();
        return instance;

      }
    });
    wsFactory.setBufferSize(4096);
    wsFactory.setMaxIdleTime(0);
  }

  @Override
  protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    if (wsFactory.acceptWebSocket(request, response))
      return;
    response.sendError(HttpServletResponse.SC_SERVICE_UNAVAILABLE, "Websocket only");
  }
}
