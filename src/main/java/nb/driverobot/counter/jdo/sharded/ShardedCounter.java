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
public class ShardedCounter<T> implements Counter<T> {
  private String counterName;
  private T counterTag;

  public ShardedCounter(String counterName, T counterTag) {
    this.counterName = counterName;
    this.counterTag = counterTag;
    addShards(20, 20);
  }

  public String getCounterName() {
    return counterName;
  }

  @Override
  public T getCounterTag() {
    return counterTag;
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
   * Increment the value of this sharded counter.
   */
  @Override
  public void increment() {
    PersistenceManager pm = PMF.get().getPersistenceManager();

    // Find how many shards are in this counter.
    int shardCount = 0;
    try {
      CounterDescriptor current = getThisCounter(pm);
      shardCount = current.getShardCount();
    } finally {
      pm.close();
    }

    // Choose the shard randomly from the available shards.
    Random generator = new Random();
    int shardNum = generator.nextInt(shardCount);

    pm = PMF.get().getPersistenceManager();
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
   * Increase the number of shards for a given sharded counter, but never adds more than maxshards
   * Will never decrease the number of shards.
   *
   * @param  count Number of new shards to build and store
   * @return Total number of shards
   */
  public int addShards(int count, int maxShards) {
    PersistenceManager pm = PMF.get().getPersistenceManager();

    // Find the initial shard count for this counter.
    int numShards = 0;
    try {
      CounterDescriptor current = getThisCounter(pm);
      if (current != null) {
        numShards = current.getShardCount();
      }
      else {
        current = new CounterDescriptor(getCounterName());
      }
      int shardCount = numShards + count;
      current.setShardCount(shardCount <= maxShards ? shardCount : maxShards);
      // Save the increased shard count for this counter.
      pm.makePersistent(current);
    } finally {
      pm.close();
    }

    // Create new shard objects for this counter.
    pm = PMF.get().getPersistenceManager();
    try {
      for (int i = 0; i < count; i++) {
        GeneralCounterShard newShard =
            new GeneralCounterShard(getCounterName(), numShards);
        pm.makePersistent(newShard);
        numShards++;
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
      CounterDescriptor current = getThisCounter(pm);
      pm.deletePersistent(current);
    } finally {
      pm.close();
    }

    pm = PMF.get().getPersistenceManager();
    try {
      Query randomShardQuery = pm.newQuery(GeneralCounterShard.class);
      randomShardQuery.setFilter("counterName == nameParam");
      randomShardQuery.declareParameters("String nameParam");

      @SuppressWarnings("unchecked")
      List<GeneralCounterShard> shards = (List<GeneralCounterShard>)
          randomShardQuery.execute(counterName);
      if (shards != null) {
        for (GeneralCounterShard shard : shards) {
          pm.deletePersistent(shard);
        }
      }
    } finally {
      pm.close();
    }
  }
}