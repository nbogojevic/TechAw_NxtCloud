package nb.driverobot.counter.jdo.sharded;

import javax.jdo.annotations.IdGeneratorStrategy;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;

@PersistenceCapable(identityType = IdentityType.APPLICATION)
public class GeneralCounterShard {
  @PrimaryKey
  @Persistent(valueStrategy = IdGeneratorStrategy.IDENTITY)
  private Long id;

  @Persistent
  private String counterName;
  
  @Persistent
  private int count;

  @Persistent
  private int shardNumber;
  
  public GeneralCounterShard(String counterName, int shardNumber) {
    this.counterName = counterName;
    this.shardNumber = shardNumber;
    count = 0;
  }

  public Long getId() {
    return id;
  }

  public int getCount() {
    return count;
  }

  public String getCounterName() {
    return counterName;
  }

  public void increment(int i) {
    count+=i;
  }

  public int getShardNumber() {
    return shardNumber;
  }
}
      