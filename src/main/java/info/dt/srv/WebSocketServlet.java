package info.dt.srv;

import java.io.IOException;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.atomic.AtomicLong;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jetty.websocket.WebSocket;
import org.eclipse.jetty.websocket.WebSocketFactory;

public class WebSocketServlet extends HttpServlet {

  private static final long serialVersionUID = 1L;
  private WebSocketFactory wsFactory;

  @Override
  public void init() throws ServletException {
    wsFactory = new WebSocketFactory(new WebSocketFactory.Acceptor() {
      public boolean checkOrigin(HttpServletRequest request, String origin) {
        return true;
      }

      public WebSocket doWebSocketConnect(HttpServletRequest request, String protocol) {
        return new Client(protocol);
      }
    });
    wsFactory.setBufferSize(4096);
    wsFactory.setMaxIdleTime(1000);
  }

  @Override
  protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    if (wsFactory.acceptWebSocket(request, response))
      return;
    response.sendError(HttpServletResponse.SC_SERVICE_UNAVAILABLE, "Websocket only");
  }

  public static class Client implements WebSocket.OnTextMessage {
    private static final AtomicLong received = new AtomicLong(0);
    private static final Set<Client> members = new CopyOnWriteArraySet<Client>();
    private final String protocol;

    public Client(String protocol) {
      this.protocol = protocol;
    }

    public void onOpen(Connection connection) {
      members.add(this);
      try {
        connection.sendMessage("aaa");
      } catch (IOException e) {
        throw new IllegalStateException(e);
      }
    }

    public void onClose(int closeCode, String message) {
      members.remove(this);
    }

    public void onMessage(String data) {
      received.incrementAndGet();
    }

  }
}
