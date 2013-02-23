package hjelpeklasser;

import java.util.*;

public class TabellStakk<T> implements Stakk<T>
{
  private T[] a;                     // en T-tabell
  private int antall;                // antall objekter på stakken

  public TabellStakk()               // konstruktør - tabellengde 8
  {
    this(8);
  }

  @SuppressWarnings("unchecked")
  public TabellStakk(int lengde)     // valgfri tabellengde
  {
    if (lengde < 0)
      throw new IllegalArgumentException("Negativ tabellengde!");

    a = (T[])new Object[lengde];     // oppretter tabellen
    antall = 0;                      // stakken er tom
  }

  public void leggInn(T t)
  {
    if (antall == a.length)
      a = Arrays.copyOf(a, antall == 0 ? 1 : 2*antall);   // dobler

    a[antall++] = t;
  }

  public T kikk()
  {
    if (antall == 0)       // sjekker om stakken er tom
      throw new NoSuchElementException("Stakken er tom!");

    return a[antall-1];    // returnerer det øverste objektet
  }

  public T taUt()
  {
    if (antall == 0)       // sjekker om stakken er tom
      throw new NoSuchElementException("Stakken er tom!");

    antall--;             // reduserer antallet

    T temp = a[antall];   // tar var på det øverste objektet
    a[antall] = null;     // tilrettelegger for resirkulering

    return temp;          // returnerer det øverste objektet
  }

  public boolean tom() {  return antall == 0; }

  public int antall() {  return antall; }

  public void nullstill()
  {
    for (int i = 0; i < antall; i++) a[i] = null;
    antall = 0;
  }

  public String toString()
  {
    if (antall == 0) return "[]";

    StringBuilder s = new StringBuilder();
    s.append('[').append(a[antall-1]);

    for (int i = antall-2; i >= 0; i--)
    {
      s.append(',').append(' ').append(a[i]);
    }
    s.append(']');

    return s.toString();
  }


}