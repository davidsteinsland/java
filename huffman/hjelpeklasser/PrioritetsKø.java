package hjelpeklasser;

public interface PrioritetsKø<T>           // Java: PriorityQueue
{
  public void leggInn(T verdi);            // Java: offer
  public T kikk();                         // Java: peek
  public T taUt();                         // Java: poll
  public int antall();                     // Java: size
  public boolean tom();                    // Java: isEmpty
  public void nullstill();                 // Java: clear
}