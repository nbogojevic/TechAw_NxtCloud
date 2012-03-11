package nb.driverobot.counter.jdo.sharded;

import nb.driverobot.Ballot;
import nb.driverobot.Counter;

public class ShardedCounterBallot extends Ballot {
  public ShardedCounterBallot(String...choices) {
    super(choices);
  }
  protected Counter initCounter(String choice) {
    ShardedCounter counter = new ShardedCounter(choice);
    return counter;
  }

}
