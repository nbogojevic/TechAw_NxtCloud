package nb.driverobot.counter.jdo;

import java.util.List;

import javax.jdo.PersistenceManager;
import javax.jdo.Query;

import nb.driverobot.Counter;
import nb.driverobot.PMF;

public class SimplePersitedCounter implements Counter {
  private String counterName;

  public SimplePersitedCounter(String counterName) {
    this.counterName = counterName;
  }

  @Override
  public void increment() {
    PersistenceManager pm = PMF.get().getPersistenceManager();
    try {
      PersistedCounter counter = getCounterInstance(pm);
      counter.increment();
      pm.makePersistent(counter);
    } finally {
      pm.close();
    }
  }

  @Override
  public int getCount() {
    PersistenceManager pm = PMF.get().getPersistenceManager();
    try {
      return getCounterInstance(pm).getCount();
    } finally {
      pm.close();
    }
  }

  private PersistedCounter getCounterInstance(PersistenceManager pm) {
    PersistedCounter current;

    Query thisCounterQuery =
        pm.newQuery(PersistedCounter.class, "counterName == nameParam");
    thisCounterQuery.declareParameters("String nameParam");

    @SuppressWarnings("unchecked")
    List<PersistedCounter> counters =
        (List<PersistedCounter>) thisCounterQuery.execute(counterName);
    if (counters != null && !counters.isEmpty()) {
      current = counters.get(0);
    }
    else {
      current = new PersistedCounter(counterName);
      pm.makePersistent(current);
    }

    return current;

  }

  @Override
  public boolean isNeedingCleanup() {
    return false;
  }

  @Override
  public void cleanup() {
  }

  @Override
  public String getCounterName() {
    return counterName;
  }

  @Override
  public int getCountAndClean() {
    PersistenceManager pm = PMF.get().getPersistenceManager();
    try {
      PersistedCounter counterInstance = getCounterInstance(pm);
      int count = counterInstance.getCount();
      counterInstance.reset();
      pm.makePersistent(counterInstance);
      return count;
    } finally {
      pm.close();
    }
  }

}
