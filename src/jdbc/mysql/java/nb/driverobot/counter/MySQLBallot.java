package nb.driverobot.counter;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.sql.DataSource;

import nb.driverobot.Ballot;
import nb.driverobot.Counter;

public class MySQLBallot extends Ballot {
  private DataSource dataSource;

  public MySQLBallot(String...choices) {
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
      createTable();
      executeSQLQuery("INSTERT INTO counters (?, 0)  ON DUPLICATE KEY UPDATE counter=0", counterName);
    }

    private void createTable() {
      executeSQLQuery("CREATE TABLE IF NOT EXISTS counters(counterName VARCHAR PRIMARY KEY, counter INT)");

    }

    private ResultSet executeSQLQuery(String statement, String...args) {
      try {
        Connection conn = dataSource.getConnection();
        try {
          PreparedStatement stmt = conn.prepareStatement(statement);
          try {
            int param = 1;
            for (String arg : args) {
              stmt.setString(param++, arg);
            }
            boolean result = stmt.execute();
            return result ? stmt.getResultSet() : null;
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
      executeSQLQuery("UPDATE count SET counter = counter+1 WHERE counterName = ?", counterName);
    }

    @Override
    public int getCount() {
      ResultSet rs = executeSQLQuery("SELECT count FROM counters WHERE counterName = ?", counterName);
      try {
        while (rs.next()) {
          return rs.getInt(1);
        }
      } catch (Exception e) {
        throw new IllegalStateException("Unable to read counter " + counterName, e);
      }
      return 0;
    }

    @Override
    public String getCounterTag() {
      return counterName;
    }

    @Override
    public boolean isNeedingCleanup() {
      return false;
    }

    @Override
    public void cleanup() {
    }
  }

}
