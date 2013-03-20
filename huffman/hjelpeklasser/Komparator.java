////////////////// class Komparator //////////////////////////////

package hjelpeklasser;

import java.util.*;

public class Komparator        // samleklasse for komparatorer
{
  private Komparator() {}      // hindrer instansiering

  private static final class   // komparator for en naturlig ordning
  Naturlig<T extends Comparable<? super T>> implements Comparator<T>
  {
    public int compare(T t1, T t2) { return t1.compareTo(t2); }

  } // class Naturlig

  public static     // en konstruktørmetode
  <T extends Comparable<? super T>> Comparator<T> naturlig()
  {
    return new Naturlig<>();
  }

  private static class   // komparator for en naturlig ordning
  Naturlig2 implements Comparator<Comparable<Object>>
  {
    public int compare(Comparable<Object> t1, Comparable<Object> t2)
    {
      return t1.compareTo(t2);
    }

  } // class Naturlig

  public static     // en konstruktørmetode
  <T> Comparator<T> naturlig2()
  {
    return (Comparator<T>) new Naturlig2();
  }

  private static final class  // omvendt komparator for naturlige ordninger
  Omvendt1<T extends Comparable<? super T>> implements Comparator<T>
  {
    public int compare(T t1, T t2)
    {
      return t2.compareTo(t1);  // snur sammenligningen
    }

  } // class Omvendt1

  public static     // en konstruktørmetode
  <T extends Comparable<? super T>> Comparator<T> omvendt()
  {
    return new Omvendt1<>();
  }

  private static final class Omvendt2<T> implements Comparator<T>
  {
    private Comparator<? super T> c;  // instansvariabel

    private Omvendt2(Comparator<? super T> c) // konstruktør
    {
      this.c = c;
    }

    public int compare(T t1, T t2)
    {
      return c.compare(t2,t1);  // snur sammenligningen
    }

  } // class Omvendt2

  // en konstruktørmetode
  public static <T> Comparator<T> omvendt(Comparator<? super T> c)
  {
    return new Omvendt2<>(c);
  }

  private static final class Sammensatt1<T> implements Comparator<T>
  {
    private Comparator<? super T> c1, c2;   // to komparatorer for T

    private Sammensatt1(Comparator<? super T> c1, Comparator<? super T> c2)
    {
      this.c1 = c1;
      this.c2 = c2;
    }

    public int compare(T t1, T t2)
    {
      int cmp = c1.compare(t1,t2);   // sammenligner mhp. c1 først
      return cmp != 0 ? cmp : c2.compare(t1,t2);
    }

  } // class Sammensatt1

  public static <T> Comparator<T>   // en konstruktørmetode
  sammensatt(Comparator<? super T> c1, Comparator<? super T> c2)
  {
    return new Sammensatt1<>(c1,c2);
  }

  private static final class
  Sammensatt2<T extends Comparable<? super T>> implements Comparator<T>
  {
    private Comparator<? super T> c;

    private Sammensatt2(Comparator<? super T> c) { this.c = c; }

    public int compare(T t1, T t2)
    {
      int cmp = c.compare(t1,t2);
      return cmp != 0 ? cmp : t1.compareTo(t2);
    }

  } // class Sammensatt2

  // en konstruktørmetode
  public static <T extends Comparable<? super T>>
  Comparator<T> sammensatt(Comparator<? super T> c)
  {
    return new Sammensatt2<>(c);
  }

} // class Komparator