package nb.driverobot;

public interface Counter<T> {

  public abstract void increment();

  public abstract int getCount();

  public abstract T getCounterTag();

}
