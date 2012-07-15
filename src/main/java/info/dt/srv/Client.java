package info.dt.srv;

import info.dt.data.IDateConfig;
import info.dt.data.TimeSheet;
import info.dt.report.IReportView;

import java.io.IOException;
import java.util.Set;

import javax.inject.Inject;

import org.eclipse.jetty.websocket.WebSocket;
import org.joda.time.DateTime;
import org.joda.time.Interval;

import com.google.common.base.Splitter;
import com.google.common.collect.Sets;

class Client extends Thread implements WebSocket.OnTextMessage {

  @Inject
  private IJsonSerializer serializer;

  @Inject
  private IDateConfig iDateConfig;

  @Inject
  private IReportView report;

  private Connection connection = null;

  private Set<String> idsOnClient = Sets.newHashSet();

  public void onOpen(Connection connection) {
    this.connection = connection;
    start();

  }

  protected void send(String string) {
    if (string.length() > 0) {
      try {
        connection.sendMessage(string);
      } catch (IOException e) {
        throw new IllegalStateException(e);
      }
    }
  }

  @Override
  public void run() {
    while (!isInterrupted()) {
      Interval currentInterval = report.getCurrentInterval();
      DateTime start = currentInterval.getStart();
      TimeSheet timeSheet = iDateConfig.getTimeSheet(start.getYear(), start.getMonthOfYear());
      send(serializer.toJson(timeSheet, idsOnClient));
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
    Iterable<String> split = Splitter.on(",").split(arg0);
    idsOnClient = Sets.newHashSet(split);

  }

}