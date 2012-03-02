package nb.driverobot.counter.jdo.sharded;

import nb.driverobot.Ballot;
import nb.driverobot.Counter;

public class ShardedCounterBallot extends Ballot {
  public ShardedCounterBallot(String...choices) {
    super(choices);
  }
  protected Counter<String> initCounter(String choice) {
    ShardedCounter<String> counter = new ShardedCounter<String>(choice + getIteration(), choice);
    counter.addShards(20);
    return counter;
  }

}
