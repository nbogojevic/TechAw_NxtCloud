package nb.driverobot.counter;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import nb.driverobot.Ballot;
import nb.driverobot.Counter;

public class MySQLBallot extends Ballot {
  public MySQLBallot(String...choices) {
    super(choices);
  }

  @Override
  protected Counter initCounter(String choice) {
    return new SQLCounter(choice);
  }

  private Connection createConnection() {
    Connection connection = null;
    try {
        // Load the JDBC driver
        Class.forName("com.mysql.jdbc.Driver");
        // Create a connection to the database
        connection = DriverManager.getConnection("jdbc:mysql://http://mariadb-1ajelastic.jelastic.com/jelasticDb", "jelastic", "password");
    } catch (Exception ex) {
        throw new IllegalStateException("Unable to get connection to database", ex);
    }
    return connection;
}


  private class SQLCounter implements Counter {

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
        Connection conn = createConnection();
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
    public boolean isNeedingCleanup() {
      return false;
    }

    @Override
    public void cleanup() {
    }

    @Override
    public String getCounterName() {
      return counterName;
    }

    @Override
    public int getCountAndClean() {
      int count = getCount();
      executeSQLQuery("UPDATE count SET counter = 0 WHERE counterName = ?", counterName);
      return count;
    }
  }

}
