package hjelpeklasser;

public interface Stakk<T>          // eng: Stack
{
  public void leggInn(T t);        // eng: push
  public T kikk();                 // eng: peek
  public T taUt();                 // eng: pop
  public int antall();             // eng: size
  public boolean tom();            // eng: isEmpty
  public void nullstill();         // eng: clear

}