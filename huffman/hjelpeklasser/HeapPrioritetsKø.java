////////////////// class HeapPrioritetsKø //////////////////////////////

package hjelpeklasser;

import java.util.*;

public class HeapPrioritetsKø<T> implements PrioritetsKø<T>
{
  private T[] heap;                          // heaptabellen
  private int antall;                        // antall verdier i køen
  private Comparator<? super T> comp;        // for sammenligninger

  public HeapPrioritetsKø(int kapasitet, Comparator<? super T> c)
  {
    if (kapasitet < 0)
      throw new IllegalArgumentException("Må ha kapasitet >= 0!");

    heap = (T[])new Object[kapasitet + 1];  // posisjon 0 brukes ikke
    antall = 0;
    comp = c;
  }

  public HeapPrioritetsKø(Comparator<? super T> c)
  {
    this(8,c);  // bruker 8 som startkapasitet
  }

  public static <T extends Comparable<? super T>>
  HeapPrioritetsKø<T> lagHeap(int kapasitet)
  {
    return new HeapPrioritetsKø<>(kapasitet,Komparator.<T>naturlig());
  }

  public static
  <T extends Comparable<? super T>> HeapPrioritetsKø<T> lagHeap()
  {
    return HeapPrioritetsKø.<T>lagHeap(8);
  }

  public void leggInn(T verdi)
  {
    antall++;                          // øker antall med 1

    // tabellen må "utvides" hvis den er full
    if (antall == heap.length) heap = Arrays.copyOf(heap,2*antall);

    int k = antall;                    // første ledige plass i tabellen
    heap[0] = verdi;                   // stoppverdi for while-løkken

    while (comp.compare(heap[k/2],verdi) > 0)
    {
      heap[k] = heap[k/2];             // trekker verdien i heap[k/2] nedover
      k /= 2;                          // k går opp til forelderen
    }
    heap[k] = verdi;                   // verdi skal ligge i posisjon k
  }

  public T kikk()
  {
    if (antall == 0)
      throw new NoSuchElementException("Køen er tom!");
    return heap[1];
  }

  public T taUt()
  {
    if (antall == 0)
      throw new NoSuchElementException("Køen er tom!");

    T min = heap[1];                   // maksverdien ligger øverst
    T verdi = heap[antall];            // skal omplasseres
    heap[antall] = null;               // for resirkulasjon
    antall--;                          // en verdi mindre i køen

    int i = 1;
    int anthalv = antall/2;

    while (i <= anthalv)
    {
      int j = 2*i;
      if (j < antall && comp.compare(heap[j+1],heap[j]) < 0) j++;
      if (comp.compare(verdi,heap[j]) <= 0) break;
      heap[i] = heap[j];
      i = j;
    }

    heap[i] = verdi;                   // legges i posisjon i
    return min;
  }

  public int antall()
  {
    return antall;
  }

  public boolean tom()
  {
    return antall == 0;
  }

  public void nullstill()
  {
    for (int i = 0; i <= antall; i++) heap[i] = null;
    antall = 0;
  }

  @Override
  public String toString()
  {
    StringBuilder s = new StringBuilder();
    s.append('[');

    if (antall > 0) s.append(heap[1]);

    for (int i = 2; i <= antall; i++)
    {
      s.append(',');
      s.append(' ');
      s.append(heap[i]);
    }

    s.append(']');

    return s.toString();
  }

}  // HeapPrioritetsKø