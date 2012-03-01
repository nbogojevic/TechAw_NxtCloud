package nb.driverobot;

import java.lang.reflect.Constructor;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

public class BallotContainer implements ServletContextListener {

  private static final String BALLOT_IMPLEMENTATION_INIT_PARAM = "ballot-implementation";
  private static final String BALLOT_CTX_KEY = "ballot";
  private static final String[] BALLOT = {"left", "right", "forward", "reverse", "stop", "grap", "drop"};

  @Override
  public void contextDestroyed(ServletContextEvent event) {
  }

  @Override
  public void contextInitialized(ServletContextEvent event) {
    ServletContext ctx = event.getServletContext();
    String implName = ctx.getInitParameter(BALLOT_IMPLEMENTATION_INIT_PARAM);
    try {
      @SuppressWarnings("unchecked")
      Class<? extends Ballot> ballotImpl = (Class<? extends Ballot>) Class.forName(implName);
      Constructor<? extends Ballot> constructor = ballotImpl.getConstructor(String[].class);
      if (constructor != null) {
        ctx.setAttribute(BALLOT_CTX_KEY, constructor.newInstance((Object) BALLOT));
      } else {
        ctx.setAttribute(BALLOT_CTX_KEY, ballotImpl.newInstance());
      }
    } catch (Exception e) {
      throw new IllegalStateException("Unable to initiate ballot instance " + implName, e);
    }
  }

  public static Ballot get(ServletContext ctx) {
    return (Ballot) ctx.getAttribute(BALLOT_CTX_KEY);
  }
}
