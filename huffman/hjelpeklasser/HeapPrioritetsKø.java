////////////////// class HeapPrioritetsK� //////////////////////////////

package hjelpeklasser;

import java.util.*;

public class HeapPrioritetsK�<T> implements PrioritetsK�<T>
{
  private T[] heap;                          // heaptabellen
  private int antall;                        // antall verdier i k�en
  private Comparator<? super T> comp;        // for sammenligninger

  public HeapPrioritetsK�(int kapasitet, Comparator<? super T> c)
  {
    if (kapasitet < 0)
      throw new IllegalArgumentException("M� ha kapasitet >= 0!");

    heap = (T[])new Object[kapasitet + 1];  // posisjon 0 brukes ikke
    antall = 0;
    comp = c;
  }

  public HeapPrioritetsK�(Comparator<? super T> c)
  {
    this(8,c);  // bruker 8 som startkapasitet
  }

  public static <T extends Comparable<? super T>>
  HeapPrioritetsK�<T> lagHeap(int kapasitet)
  {
    return new HeapPrioritetsK�<>(kapasitet,Komparator.<T>naturlig());
  }

  public static
  <T extends Comparable<? super T>> HeapPrioritetsK�<T> lagHeap()
  {
    return HeapPrioritetsK�.<T>lagHeap(8);
  }

  public void leggInn(T verdi)
  {
    antall++;                          // �ker antall med 1

    // tabellen m� "utvides" hvis den er full
    if (antall == heap.length) heap = Arrays.copyOf(heap,2*antall);

    int k = antall;                    // f�rste ledige plass i tabellen
    heap[0] = verdi;                   // stoppverdi for while-l�kken

    while (comp.compare(heap[k/2],verdi) > 0)
    {
      heap[k] = heap[k/2];             // trekker verdien i heap[k/2] nedover
      k /= 2;                          // k g�r opp til forelderen
    }
    heap[k] = verdi;                   // verdi skal ligge i posisjon k
  }

  public T kikk()
  {
    if (antall == 0)
      throw new NoSuchElementException("K�en er tom!");
    return heap[1];
  }

  public T taUt()
  {
    if (antall == 0)
      throw new NoSuchElementException("K�en er tom!");

    T min = heap[1];                   // maksverdien ligger �verst
    T verdi = heap[antall];            // skal omplasseres
    heap[antall] = null;               // for resirkulasjon
    antall--;                          // en verdi mindre i k�en

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

}  // HeapPrioritetsK�