package nb.driverobot.counter;

import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;

@PersistenceCapable(identityType = IdentityType.APPLICATION)
public class PersistedCounter {
  @PrimaryKey
  @Persistent
  private String counterName;
  
  public PersistedCounter(String counterName) {
    super();
    this.counterName = counterName;
  }

  @Persistent
  private int count;

  public String getCounterName() {
    return counterName;
  }

  public int getCount() {
    return count;
  }

  public void increment() {
    count++;
  }
}
