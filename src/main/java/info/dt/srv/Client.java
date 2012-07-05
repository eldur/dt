package info.dt.srv;

import info.dt.data.IDateConfig;
import info.dt.data.TimeSheet;

import java.io.IOException;

import javax.inject.Inject;

import org.eclipse.jetty.websocket.WebSocket;

class Client extends Thread implements WebSocket.OnTextMessage {

  @Inject
  private IJsonSerializer serializer;

  @Inject
  private IDateConfig iDateConfig;

  private Connection connection = null;

  public void onOpen(Connection connection) {
    this.connection = connection;
    start();

  }

  protected void send(String string) {
    try {
      connection.sendMessage(string);
    } catch (IOException e) {
      throw new IllegalStateException(e);
    }
  }

  @Override
  public void run() {
    while (!isInterrupted()) {
      TimeSheet timeSheet = iDateConfig.getTimeSheet(2012, 06);
      send(serializer.toJson(timeSheet));
      try {
        synchronized (this) {
          wait(2000);
        }
      } catch (InterruptedException e) {
        interrupt();
      }
    }
  }

  public void onClose(int closeCode, String message) {
    connection = null;
    interrupt();
  }

  public void onMessage(String arg0) {
    // TODO Auto-generated method stub

  }

}