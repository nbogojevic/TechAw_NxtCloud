package nb.driverobot.counter.sharded;

import javax.jdo.annotations.IdGeneratorStrategy;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;

@PersistenceCapable(identityType = IdentityType.APPLICATION)
public class CounterDescriptor {
  @PrimaryKey
  @Persistent(valueStrategy = IdGeneratorStrategy.IDENTITY)
  private Long id;

  @Persistent
  private String counterName;
  
  @Persistent
  private Integer shardCount;

  public CounterDescriptor(String counterName) {
    this.counterName = counterName;
  }

  public Long getId() {
    return id;
  }

  public String getCounterName() {
    return counterName;
  }

  public Integer getShardCount() {
    return shardCount;
  }

  public void setShardCount(int shardCount) {
    this.shardCount = shardCount;
  }

}
