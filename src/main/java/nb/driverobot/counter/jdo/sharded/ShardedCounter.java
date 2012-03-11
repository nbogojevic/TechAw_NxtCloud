package nb.driverobot.counter.jdo.sharded;

import java.util.List;
import java.util.Random;
import javax.jdo.PersistenceManager;
import javax.jdo.Query;

import nb.driverobot.Counter;
import nb.driverobot.PMF;

/**
 * A counter which can be incremented rapidly.
 *
 * Capable of incrementing the counter and increasing the number of shards.
 * When incrementing, a random shard is selected to prevent a single shard
 * from being written to too frequently. If increments are being made too
 * quickly, increase the number of shards to divide the load. Performs
 * datastore operations using JDO.
 */
public class ShardedCounter implements Counter {
  private String counterName;
  private CounterDescriptor currentDescriptor;

  public ShardedCounter(String counterName) {
    this.counterName = counterName;
    setUpCounter(2);
  }

  @Override
  public String getCounterName() {
    return counterName;
  }

  /**
   * Retrieve the value of this sharded counter.
   *
   * @return Summed total of all shards' counts
   */
  @Override
  public int getCount() {
    int sum = 0;
    PersistenceManager pm = PMF.get().getPersistenceManager();

    try {
      Query shardsQuery = pm.newQuery(GeneralCounterShard.class, "counterName == nameParam");
      shardsQuery.declareParameters("String nameParam");

      @SuppressWarnings("unchecked")
      List<GeneralCounterShard> shards =
          (List<GeneralCounterShard>) shardsQuery.execute(counterName);
      if (shards != null && !shards.isEmpty()) {
        for (GeneralCounterShard current : shards) {
          sum += current.getCount();
        }
      }
    } finally {
      pm.close();
    }

    return sum;
  }

  /**
   * Retrieve the value of this sharded counter.
   *
   * @return Summed total of all shards' counts
   */
  @Override
  public int getCountAndClean() {
    int sum = 0;
    PersistenceManager pm = PMF.get().getPersistenceManager();

    try {
      Query shardsQuery = pm.newQuery(GeneralCounterShard.class, "counterName == nameParam");
      shardsQuery.declareParameters("String nameParam");

      @SuppressWarnings("unchecked")
      List<GeneralCounterShard> shards =
          (List<GeneralCounterShard>) shardsQuery.execute(counterName);
      if (shards != null && !shards.isEmpty()) {
        for (GeneralCounterShard current : shards) {
          sum += current.getCount();
          current.reset();
          pm.makePersistent(current);
        }
      }
    } finally {
      pm.close();
    }

    return sum;
  }

  /**
   * Increment the value of this sharded counter.
   */
  @Override
  public void increment() {
    int shardCount = currentDescriptor.getShardCount();
    // Choose the shard randomly from the available shards.
    Random generator = new Random();
    int shardNum = generator.nextInt(shardCount);

    PersistenceManager pm = PMF.get().getPersistenceManager();
    try {
      Query randomShardQuery = pm.newQuery(GeneralCounterShard.class);
      randomShardQuery.setFilter("counterName == nameParam && shardNumber == numParam");
      randomShardQuery.declareParameters("String nameParam, int numParam");

      @SuppressWarnings("unchecked")
      List<GeneralCounterShard> shards = (List<GeneralCounterShard>)
          randomShardQuery.execute(counterName, shardNum);
      if (shards != null && !shards.isEmpty()) {
        GeneralCounterShard shard = shards.get(0);
        shard.increment(1);
        pm.makePersistent(shard);
      }
    } finally {
      pm.close();
    }
  }

  /**
   * Create or setup sharded counter
   *
   * @param  shards Number of shards to build and store
   * @return Total number of sards
   */
  public int setUpCounter(int shards) {
    PersistenceManager pm = PMF.get().getPersistenceManager();

    // Find the initial shard count for this counter.
    int numShards = 0;
    try {
      currentDescriptor = getThisCounter(pm);
      if (currentDescriptor == null) {
        currentDescriptor = new CounterDescriptor(getCounterName());
      }
      if (currentDescriptor.getShardCount() <= shards) {
        currentDescriptor.setShardCount(shards);
        // Save the increased shard count for this counter.
        pm.makePersistent(currentDescriptor);
        for (int i = 0; i < shards; i++) {
          GeneralCounterShard newShard =
              new GeneralCounterShard(getCounterName(), numShards);
          pm.makePersistent(newShard);
          numShards++;
        }
      }
    } finally {
      pm.close();
    }

    return numShards;
  }

  /**
   * @return Counter datastore object matching this object's counterName value
   */
  private CounterDescriptor getThisCounter(PersistenceManager pm) {
    CounterDescriptor current = null;

    Query thisCounterQuery = pm.newQuery(CounterDescriptor.class, "counterName == nameParam");
    thisCounterQuery.declareParameters("String nameParam");

    @SuppressWarnings("unchecked")
    List<CounterDescriptor> counter =
        (List<CounterDescriptor>) thisCounterQuery.execute(counterName);
    if (counter != null && !counter.isEmpty()) {
      current = counter.get(0);
    }

    return current;
  }

  @Override
  public boolean isNeedingCleanup() {
    return true;
  }

  @Override
  public void cleanup() {
    PersistenceManager pm = PMF.get().getPersistenceManager();

    try {
      Query randomShardQuery = pm.newQuery(GeneralCounterShard.class);
      randomShardQuery.setFilter("counterName == nameParam");
      randomShardQuery.declareParameters("String nameParam");

      @SuppressWarnings("unchecked")
      List<GeneralCounterShard> shards = (List<GeneralCounterShard>)randomShardQuery.execute(counterName);
      if (shards != null) {
        for (GeneralCounterShard shard : shards) {
          shard.reset();
          pm.makePersistent(shard);
        }
      }
    } finally {
      pm.close();
    }
  }
}