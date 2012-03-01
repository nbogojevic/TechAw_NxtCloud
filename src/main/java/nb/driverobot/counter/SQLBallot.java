package nb.driverobot.counter;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.sql.DataSource;

import nb.driverobot.Ballot;
import nb.driverobot.Counter;

public class SQLBallot extends Ballot {
  private DataSource dataSource;

  public SQLBallot(String...choices) {
    super(choices);
    try {
      Context ctx = new InitialContext();
      this.dataSource = (DataSource)ctx.lookup("java:comp/env/jdbc/TestDB");  
    } catch (Exception e) {
      throw new IllegalStateException("Unable to access datasource.", e);
    }
  }

  @Override
  protected Counter<String> initCounter(String choice) {
    return new SQLCounter(choice);
  }
  
  private class SQLCounter implements Counter<String> {

    private String counterName;

    public SQLCounter(String counterName) {
      this.counterName = counterName;
      try {
        Connection conn = dataSource.getConnection();
        try {
          PreparedStatement stmt = conn.prepareStatement("insert into counter (?, 0)");
          try {
            stmt.setString(1, counterName);
            stmt.execute();
          } finally {
            stmt.close();
          }          
        } finally {
          conn.close();
        }
      } catch (Exception e) {
        throw new IllegalStateException("Unable to create counter " + counterName, e);
      }
    }

    @Override
    public void increment() {
      // TODO Auto-generated method stub
      
    }

    @Override
    public int getCount() {
      // TODO Auto-generated method stub
      return 0;
    }

    @Override
    public String getCounterTag() {
      // TODO Auto-generated method stub
      return null;
    }
    
  }

}
