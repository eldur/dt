package info.dt.srv;

import info.dt.data.IDateConfig;
import info.dt.report.DefaultReportMapping;
import info.dt.report.IReportMapping;

import java.net.InetSocketAddress;
import java.net.URL;

import org.junit.Test;

import com.google.common.io.Resources;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;
import com.mycila.inject.jsr250.Jsr250;

public class AppServerTest {

  @Test
  public void startJetty() throws Exception {
    Module m = new AbstractModule() {

      @Override
      protected void configure() {
        InetSocketAddress srvSocket = InetSocketAddress.createUnresolved("localhost", 9999);
        bind(InetSocketAddress.class).toInstance(srvSocket);

        bind(IReportMapping.class).to(DefaultReportMapping.class);
        URL workFile = Resources.getResource("test.yaml");
        bind(IDateConfig.class).toInstance(new YamlDateConfig(workFile));
      }

    };
    Injector injector = Guice.createInjector(m, Jsr250.newJsr250Module());
    AppServer server = new AppServer(injector);
    server.start();
    server.join();
  }

}
