package info.dt.srv;

import static javax.servlet.DispatcherType.FORWARD;
import static javax.servlet.DispatcherType.REQUEST;

import java.net.InetSocketAddress;
import java.util.EnumSet;

import javax.servlet.DispatcherType;

import org.apache.jasper.servlet.JspServlet;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.webapp.WebAppContext;

import com.google.common.collect.Sets;
import com.google.inject.Injector;
import com.google.inject.servlet.GuiceFilter;
import com.google.inject.servlet.GuiceServletContextListener;
import com.google.inject.servlet.ServletModule;

public class AppServer extends Server {

  public AppServer(Injector injector) {
    super(injector.getInstance(InetSocketAddress.class));

    WebAppContext webapp = new WebAppContext();
    webapp.setWar("./src/main/webapp/WEB-INF/");

    webapp.addFilter(GuiceFilter.class, "/*", toESet(REQUEST, FORWARD));
    webapp.addEventListener(new InjeciveServletConfig(injector));

    webapp.addServlet(new ServletHolder(new JspServlet()), "/");
    webapp.addServlet(new ServletHolder(new WebSocketServlet()), "/ws/");
    setHandler(webapp);

  }

  private static EnumSet<DispatcherType> toESet(DispatcherType... types) {
    EnumSet<DispatcherType> result = Sets.newEnumSet(Sets.newHashSet(types), DispatcherType.class);
    return result;
  }

  private static class InjeciveServletConfig extends GuiceServletContextListener {

    private final Injector moduleList;

    public InjeciveServletConfig(Injector moduleList) {
      this.moduleList = moduleList;
    }

    @Override
    protected Injector getInjector() {

      ServletModule servletModule = new ServletModule() {

        @Override
        protected void configureServlets() {
          filter("/*").through(UrlFilter.class);
          filter("/jsp/*").through(ContentFiter.class);
        }

      };
      return moduleList.createChildInjector(servletModule);
    }
  }

}
