package nb.driverobot.counter.jdo;

import nb.driverobot.Ballot;
import nb.driverobot.Counter;

public class SimpleBallot extends Ballot {
  public SimpleBallot(String...choices) {
    super(choices);
  }
  @Override
  protected Counter initCounter(String choice) {
    SimplePersitedCounter counter = new SimplePersitedCounter(choice);
    return counter;
  }

}
