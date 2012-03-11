package nb.driverobot;

public interface Counter {

  public abstract void increment();

  public abstract int getCount();

  public abstract boolean isNeedingCleanup();

  public abstract void cleanup();
  
  public String getCounterName();

  int getCountAndClean();
}
