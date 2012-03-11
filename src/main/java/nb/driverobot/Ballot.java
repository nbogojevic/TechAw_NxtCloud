package nb.driverobot;

import java.util.HashMap;

public abstract class Ballot {
  private HashMap<String, Counter> counters = new HashMap<String, Counter>();
  private final String[] choices;

  public Ballot(String...choices) {
    this.choices = choices;
    init();
  }

  public final void vote(String option) {
    Counter counter = counters.get(option);
    if (counter != null) {
      counter.increment();
    } else {
      throw new IllegalArgumentException("Option not recognized");
    }
  }

  public String getWinner() {
    int maxCount = -1;
    String leader = null;
    for (Counter counter : counters.values()) {
      int currentCounter = counter.getCountAndClean();
      if (currentCounter > maxCount) {
        maxCount = currentCounter;
        leader = counter.getCounterName();
      }
      else if (currentCounter == maxCount) {
        leader = leader + ";" + counter.getCounterName();
      }
    }
    return maxCount > 0 ? leader : "none";
  }
  public final String[] getChoices() {
    return choices;
  }
  public int getVotesForChoice(String choice) {
    Counter counter = counters.get(choice);
    if (counter != null) {
      return counter.getCount();
    }
    return 0;
  }

  private void init() {
    HashMap<String, Counter> newCounters = new HashMap<String, Counter>();
    for (String choice : choices) {
      Counter counter = initCounter(choice);
      newCounters.put(choice, counter);
    }
    counters = newCounters;
  }
  
  public final void cleanup() {
    for (String choice : choices) {
      Counter counter = counters.get(choice);
      if (counter != null && counter.isNeedingCleanup()) {
        counter.cleanup();
      }
    }
    
  }

  protected abstract Counter initCounter(String choice);

}
