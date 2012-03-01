package nb.driverobot;

import java.util.ArrayList;
import java.util.HashMap;

public abstract class Ballot {
  private final HashMap<String, Counter<String>> counters = new HashMap<String, Counter<String>>();
  private final ArrayList<Counter<String>> oldCounters = new ArrayList<Counter<String>>();
  private final String[] choices;
  private int iteration;
  
  public Ballot(String...choices) {
    this.choices = choices;
    init();
  }

  public final void vote(String option) {
    Counter<String> counter = counters.get(option);
    if (counter != null) {
      counter.increment();
    } else {
      throw new IllegalArgumentException("Option not recognized");
    }
  }

  public final String getWinner() {
    synchronized (this) {
      String leader = getLeader();
      init();
      return leader;
    }
  }
  
  public final String getLeader() {
    int maxCount = -1;
    String leader = null;
    for (Counter<String> counter : counters.values()) {
      int currentCounter = counter.getCount();
      if (currentCounter > maxCount) {
        maxCount = currentCounter;
        leader = counter.getCounterTag();
      }
    }
    return leader;
  }
  public final String[] getChoices() {
    return choices;
  }
  public int getVotesForChoice(String choice) {
    Counter<String> counter = counters.get(choice);
    if (counter != null) {
      return counter.getCount();
    }
    return 0;
  }
  
  private void init() {
    for (String choice : choices) {
      Counter<String> counter = counters.get(choice);
      if (counter != null) {
        oldCounters.add(counter);
      }
      counter = initCounter(choice);
      counters.put(choice, counter);
    }
    iteration++;
  }

  protected final int getIteration() {
    return iteration;
  }
  
  protected abstract Counter<String> initCounter(String choice);

}
