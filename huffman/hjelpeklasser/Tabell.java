////////////////// class Tabell //////////////////////////////

package hjelpeklasser;

import java.util.*;

public class Tabell
{
  private Tabell() {}

  public static void fratilKontroll(int tablengde, int fra, int til)
  {
    if (fra < 0)                             // fra er negativ
      throw new ArrayIndexOutOfBoundsException
        ("fra(" + fra + ") er negativ!");

    if (til > tablengde)                     // til er utenfor tabellen
      throw new ArrayIndexOutOfBoundsException
        ("til(" + til + ") > tablengde(" + tablengde + ")");

    if (fra > til)                           // fra er større enn til
      throw new IllegalArgumentException
        ("fra(" + fra + ") > til(" + til + ") - illegalt intervall!");
  }

  public static void bytt(int[] a, int i, int j)
  {
    int temp = a[i]; a[i] = a[j]; a[j] = temp;
  }

  public static void skriv(int... a)
  {
    for (int k : a) System.out.print(k + " ");
  }

  public static void skrivln(int... a)
  {
    skriv(a);
    System.out.println();
  }

  public static void skriv(Object... a)
  {
    for (Object o : a) System.out.print(o + " ");
  }

  public static void skrivln(Object... a)
  {
    skriv(a);
    System.out.println();
  }

  public static int parter(int[] a, int v, int h, int skilleverdi)
  {
    while (v <= h && a[v] < skilleverdi) v++;   // h er stoppverdi for v
    while (v <= h && skilleverdi <= a[h]) h--;  // v er stoppverdi for h

    while (true)
    {
      if (v < h) Tabell.bytt(a,v++,h--);   // bytter om a[v] og a[h]
      else  return v;                      // partisjoneringen er ferdig
      while (a[v] < skilleverdi) v++;      // flytter v mot høyre
      while (skilleverdi <= a[h]) h--;     // flytter h mot venstre
    }
  }

  public static int parter(int[] a, int skilleverdi)
  {
    return parter(a,0,a.length-1,skilleverdi);  // kaller metoden over
  }

  public static int sParter(int[] a, int v, int h, int indeks)
  {
    if (indeks < v || indeks > h) throw new IllegalArgumentException();

    bytt(a,h,indeks);
    int k = parter(a,v,h-1,a[h]);
    bytt(a,h,k);
    return k;
  }

  public static int sParter(int[] a, int k)   // bruker hele tabellen
  {
    return sParter(a,0,a.length-1,k);   // v = 0 og h = a.lenght - 1
  }

  public static void kvikksortering(int[] a)
  {
    kvikksortering(a, 0, a.length - 1);
  }

  private static void kvikksortering(int[] a, int v, int h)
  {
    if (v >= h) return;

    int m = (v + h)/2;

    int p = sParter(a, v, h, m);

    kvikksortering(a, v, p - 1);
    kvikksortering(a, p + 1, h);
  }

  public static int[] randPerm(int n)  // virker, men er svÃ¦rt ineffektiv
  {
    Random r = new Random();      // en randomgenerator
    int[] a = new int[n];         // en tabell med plass til n tall

    for (int i = 0; i < n; )      // vi skal legge inn n tall
    {
      int k = r.nextInt(n) + 1;   // trekker et nytt tall
      boolean nyVerdi = true;     // antar at tallet ikke finnes fra før

      for (int j = 0; j < i && nyVerdi; j++)  // leter i a[0:i>
      {
        if (a[j] == k) nyVerdi = false;  // har vi k fra før?
      }
      if (nyVerdi) a[i++] = k;    // legger inn k og øker i
    }
    return a;                     // tabellen returneres
  }

  public static int maks(double[] a)     // legges i class Tabell
  {
    int m = 0;                           // indeks til største verdi
    double maksverdi = a[0];             // største verdi

    for (int i = 1; i < a.length; i++) if (a[i] > maksverdi)
    {
      maksverdi = a[i];     // største verdi oppdateres
      m = i;                // indeks til største verdi oppdaters
    }
    return m;     // returnerer posisjonen til største verdi

  } // maks

  public static int maks(int[] a)   // versjon 2 av maks-metoden
  {
    int m = 0;               // indeks til største verdi
    int maksverdi = a[0];    // største verdi

    for (int i = 1; i < a.length; i++) if (a[i] > maksverdi)
    {
      maksverdi = a[i];     // største verdi oppdateres
      m = i;                // indeks til største verdi oppdaters
    }
    return m;   // returnerer indeks/posisjonen til største verdi

  } // maks

  public static int[] naturligeTall(int n)
  {
    int[] a = new int[n];
    for (int i = 0; i < n; i++) a[i] = i + 1;
    return a;
  }

  public static boolean nestePermutasjon(int[] a)
  {
    int n = a.length, i = n - 2;  // i starter nest bakerst i a
    while (i >= 0 && a[i] > a[i+1]) i--;  // gÃ¥r forover i a

    // hvis i nÃ¥ er lik -1, mÃ¥ a = {n,n-1, . . . , 2,1}
    if (i < 0) return false;  // a er den siste

    int j = n - 1;  // j starter bakerst i a
    for (int x = a[i]; a[j] < x; j--);     // den første større enn x
    bytt(a,i,j);    // bytter om

    for (j = n; ++i < --j; ) bytt(a,i,j);  // snur det til høyre for i
    return true;    // a inneholder en ny permutasjon
  }

  private static class PermIterator implements Iterator<int[]>
  {
    private int[] a;                 // tabell for permutasjoner
    private boolean flere = true;    // flere permutasjoner?

    private PermIterator(int n)      // privat konstruktør
    {
      a = naturligeTall(n);          // a = {1,2,3, . . ,n}
    }

    private PermIterator(int[] p)    // privat konstruktør
    {
      a = p.clone();                 // a blir en kopi av p
    }

    public int[] next()
    {
      int[] b = a.clone();           // b blir en kopi av a
      flere = nestePermutasjon(a);   // bruker Programkode 1.3.3 a)
      return b;
    }

    public boolean hasNext() { return flere; }

    public void remove() { throw new UnsupportedOperationException(); }

  }  // class PermIterator

  public static Iterator<int[]> permiterator(int n)
  {
    return new PermIterator(n);
  }

  public static Iterator<int[]> permiterator(int[] a)
  {
    return new PermIterator(a);
  }

  public static <T extends Comparable<? super T>> int maks(T[] a)
  {
    int m = 0;                     // indeks til største verdi
    T maksverdi = a[0];            // største verdi

    for (int i = 1; i < a.length; i++) if (a[i].compareTo(maksverdi) > 0)
    {
      maksverdi = a[i];  // største verdi oppdateres
      m = i;             // indeks til største verdi oppdaters
    }
    return m;  // returnerer posisjonen til største verdi

  } // maks

  public static void bytt(Object[] a, int i, int j)
  {
    Object temp = a[i]; a[i] = a[j]; a[j] = temp;
  }

  public static Integer[] randPermInteger(int n)
  {
    Integer[] a = new Integer[n];       // legger inn 1, 2, . . , n
    for (int i = 0; i < n; i++) a[i] = i+1;    // autoboksing

    Random r = new Random();  // hentes fra java.util

    for (int k = n-1; k > 0; k--)
    {
      int i = r.nextInt(k+1); // tilfeldig tall fra [0,k]
      bytt(a,k,i);
    }
    return a; // tabellen med permutasjonen returneres
  }

  public static <T extends Comparable<T>> void innsettingssortering(T[] a)
  {
    for (int i = 1; i < a.length; i++)
    {
      T temp = a[i];       // flytter a[i] til en hjelpevariabel

      int j = i-1;         // starter med neste tabellposisjon

      // en og en verdi flyttes inntil rett sortert plass er funnet

      for (; j >= 0 && temp.compareTo(a[j]) < 0; j--) a[j+1] = a[j];

      a[j+1] = temp;  // temp legges inn pÃ¥ rett plass
    }
  } // generisk innsettingssortering

  public static <T> int maks(T[] a, Comparator<? super T> c)
  {
    int m = 0;                // indeks til største verdi
    T maksverdi = a[0];       // største verdi

    for (int i = 1; i < a.length; i++) if (c.compare(a[i],maksverdi) > 0)
    {
      maksverdi = a[i];       // største verdi oppdateres
      m = i;                  // indeks til største verdi oppdateres
    }
    return m;        // returnerer posisjonen til største verdi

  }  // maks

  public static int binærsøk(int[] a, int fra, int til, int verdi)
  {
    Tabell.fratilKontroll(a.length,fra,til);  // se Programkode 1.2.3 a)
    int v = fra, h = til - 1;  // v og h er intervallets endepunkter

    while (v < h)  // obs. må ha v < h her og ikke v <= h
    {
      int m = (v + h)/2;  // heltallsdivisjon - finner midten

      if (verdi > a[m]) v = m + 1;   // verdi må ligge i a[m+1:h]
      else  h = m;                   // verdi må ligge i a[v:m]
    }
    if (h < v || verdi < a[v]) return -(v + 1);  // ikke funnet
    else if (verdi == a[v]) return v;            // funnet
    else  return -(v + 2);                       // ikke funnet
  }

  public static int binærsøk(int[] a, int verdi)
  {
    return binærsøk(a,0,a.length,verdi);
  }

  public static <T>
  void innsettingssortering(T[] a, Comparator<? super T> c)
  {
    for (int i = 1; i < a.length; i++)
    {
      T temp = a[i]; // flytter a[i] til en hjelpevariabel

      int j = i-1;    // starter med neste tabellposisjon

      // en og en verdi flyttes inntil rett sortert plass er funnet
      for (; j >= 0 && c.compare(temp,a[j]) < 0; j--) a[j+1] = a[j];

      a[j+1] = temp;  // temp legges inn pÃ¥ rett plass
    }
  } // innsettingssortering

  private static <T>
  void flett(T[] a, T[] b, int fra, int m, int til, Comparator<? super T> c)
  {
    int n = m - fra;   // antall elementer i a[fra:m>
    System.arraycopy(a,fra,b,0,n);   // kopierer a[fra:m> over i b[0:n>

    int i = 0, j = m, k = fra;       // løkkevariabler og indekser

    while (i < n && j < til)    // fletter b[0:n> og a[m:til>
      a[k++] = c.compare(b[i],a[j]) < 0 ? b[i++] : a[j++];

    while (i < n) a[k++] = b[i++];  // tar med resten av b[0:n>
  }

  private static <T>
  void flettesortering(T[] a, T[] b, int fra, int til, Comparator<? super T> c)
  {
    if (til - fra <= 1) return;     // a[fra:til> har maks ett element

    int m = (fra + til)/2;          // midt mellom fra og til

    flettesortering(a,b,fra,m,c);   // sorterer a[fra:m>
    flettesortering(a,b,m,til,c);   // sorterer a[m:til>

    flett(a,b,fra,m,til,c);         // fletter a[fra:m> og a[m:til>
  }

  public static <T> void flettesortering(T[] a, Comparator<? super T> c)
  {
    T[] b = (T[]) new Object[a.length/2];  // en hjelpetabell for flettingen
    flettesortering(a,b,0,a.length,c);     // kaller metoden over
  }

  private static void flett(int[] a, int[] b, int fra, int m, int til)
  {
    int n = m - fra;   // antall elementer i a[fra:m>
    System.arraycopy(a,fra,b,0,n); // kopierer a[fra:m> over i b[0:n>

    int i = 0, j = m, k = fra;     // løkkevariabler og indekser

    while (i < n && j < til)  // fletter b[0:n> og a[m:til>, legger
      a[k++] = b[i] < a[j] ? b[i++] : a[j++];  // resultatet i a[fra:til>

    while (i < n) a[k++] = b[i++];  // tar med resten av b[0:n>
  }

  private static void flettesortering(int[] a, int[] b, int fra, int til)
  {
    if (til - fra <= 1) return;   // a[fra:til> har maks ett element

    int m = (fra + til)/2;        // midt mellom fra og til

    flettesortering(a,b,fra,m);   // sorterer a[fra:m>
    flettesortering(a,b,m,til);   // sorterer a[m:til>

    flett(a,b,fra,m,til);         // fletter a[fra:m> og a[m:til>
  }

  public static void flettesortering(int[] a)
  {
    int[] b = new int[a.length/2];    // en hjelpetabell for flettingen
    flettesortering(a,b,0,a.length);  // kaller metoden over
  }

  public static <T>
  int parter(T[] a, int v, int h, T skilleverdi, Comparator<? super T> c)
  {
    while (v <= h && c.compare(a[v],skilleverdi) < 0) v++;
    while (v <= h && c.compare(skilleverdi,a[h]) <= 0) h--;

    while (true)
    {
      if (v < h) Tabell.bytt(a,v++,h--); else return v;
      while (c.compare(a[v],skilleverdi) < 0) v++;
      while (c.compare(skilleverdi,a[h]) <= 0) h--;
    }
  }

  public static <T> int parter(T[] a, T skilleverdi, Comparator<? super T> c)
  {
    return parter(a,0,a.length-1,skilleverdi,c);  // kaller metoden over
  }

  public static <T>
  int sParter(T[] a, int v, int h, int k, Comparator<? super T> c)
  {
    if (v < 0 || h >= a.length || k < v || k > h) throw new
        IllegalArgumentException("Ulovlig parameterverdi");

    bytt(a,k,h);   // bytter - skilleverdien a[k] legges bakerst
    int p = parter(a,v,h-1,a[h],c);  // partisjonerer a[v:h-1]
    bytt(a,p,h);   // bytter for å få skilleverdien på rett plass

    return p;    // returnerer posisjonen til skilleverdien
  }

  public static <T>
  int sParter(T[] a, int k, Comparator<? super T> c)   // bruker hele tabellen
  {
    return sParter(a,0,a.length-1,k,c); // v = 0 og h = a.lenght-1
  }

  private static <T>
  void kvikksortering(T[] a, int v, int h, Comparator<? super T> c)
  {
    if (v >= h) return;  // hvis v = h er a[v:h] allerede sortert

    int p = sParter(a,v,h,(v + h)/2,c);
    kvikksortering(a,v,p-1,c);
    kvikksortering(a,p+1,h,c);
  }

  public static <T>
  void kvikksortering(T[] a, Comparator<? super T> c) // sorterer hele tabellen
  {
    kvikksortering(a,0,a.length-1,c);
  }

  public static boolean erSortertStigende(int[] a)
  {
    for (int i = 1; i < a.length; i++)
      if (a[i-1] > a[i]) return false;  // ikke sortert stigende

    return true;  // a er sortert stigende
  }

  public static void innsettingssortering(int[] a, int fra, int til)
  {
    fratilKontroll(a.length,fra,til);  // se Programkode 1.2.3 a)

    for (int i = fra + 1; i < til; i++)  // a[fra] er første verdi
    {
      int temp = a[i];  // flytter a[i] til en hjelpevariabel

      // verdier flyttes inntil rett sortert plass i a[fra:i> er funnet
      int j = i-1; for (; j >= fra && temp < a[j]; j--) a[j+1] = a[j];

      a[j+1] = temp;  // verdien settes inn på rett sortert plass
    }
  }

  public static void innsettingssortering(int[] a)
  {
    innsettingssortering(a,0,a.length);   // sorterer hele tabellen
  }

  public static int antallForskjellige(int[] a)  // a må være sortert
  {
    if (a.length <= 1) return a.length;  // én eller ingen verdier

    int antall = 1;
    for (int i = 1; i < a.length; i++) if (a[i-1] < a[i]) antall++;
    return antall;
  }

  public static int union(int[] a, int m, int[] b, int n, int[] c)
  {
    int i = 0, j = 0, k = 0;

    while (i < m && j < n)
    {
      if (a[i] < b[j]) c[k++] = a[i++];
      else if (a[i] == b[j])            // a[i] og b[j] er like
      {
        c[k++] = a[i++];                // tar med a[i]
        j++;                            // hopper over b[j]
      }
      else  c[k++] = b[j++];
    }

    while (i < m) c[k++] = a[i++];      // tar med resten av a[0:m>
    while (j < n) c[k++] = b[j++];      // tar med resten av b[0:n>

    return k;    // antall verdier lagt inn i c
  }

  public static int union(int[] a, int[] b, int[] c)
  {
    return union(a,a.length,b,b.length,c);
  }

  public static int snitt(int[] a, int m, int[] b, int n, int[] c)
  {
    int i = 0, j = 0, k = 0;

    while (i < m && j < n)
    {
      if (a[i] < b[j]) i++;      // hopper over a[i]
      else if (a[i] == b[j])
      {
        c[k++] = a[i++];         // a[i] == b[j], tar med a[i]
        j++;
      }
      else  j++;                 // hopper over b[j]
    }

    return k;    // antall verdier i snittet
  }

  // En metode som finner snittet av to hele tabeller:

  public static int snitt(int[] a, int[] b, int[] c)
  {
    return snitt(a,a.length,b,b.length,c);
  }

  public static int differans(int[] a, int m, int[] b, int n, int[] c)
  {
    if (m < 0 || m > a.length)
      throw new IllegalArgumentException("a[0:m> er ulovlig!");
    if (n < 0 || n > b.length)
      throw new IllegalArgumentException("b[0:n> er ulovlig!");

    int i = 0, j = 0, k = 0;

    while (i < m && j < n)
    {
      if (a[i] < b[j]) c[k++] = a[i++];
      else if (a[i] == b[j]) { i++; j++;}
      else j++;
    }
    while (i < m) c[k++] = a[i++];
    return k;
  }

  public static int differans(int[] a, int[] b, int[] c)
  {
    return differans(a,a.length,b,b.length,c);
  }

  public static boolean erLik(int[] a, int m, int[] b, int n)
  {
    if (m < 0 || m > a.length)
      throw new IllegalArgumentException("a[0:m> er ulovlig!");
    if (n < 0 || n > b.length)
      throw new IllegalArgumentException("b[0:n> er ulovlig!");

    if (m != n) return false;  // forskjellige lengder

    if (a == b) return true;   // det samme intervallet

    for (int i = 0; i < m; i++)
      if (a[i] != b[i]) return false;
    return true;
  }

  public static boolean erLik(int[] a, int[] b)
  {
    return erLik(a,a.length,b,b.length);
  }

  public static boolean inklusjon(int[] a, int m, int[] b, int n)
  {
    if (m < 0 || m > a.length)
      throw new IllegalArgumentException("a[0:m> er ulovlig!");
    if (n < 0 || n > b.length)
      throw new IllegalArgumentException("b[0:n> er ulovlig!");

    int i = 0, j = 0;

    while (i < m && j < n)
    {
      if (a[i] < b[j]) i++;
      else if (a[i] == b[j]) {i++; j++;}
      else return false;
    }
    return j == n;
  }

  public static boolean inklusjon(int[] a, int[] b)
  {
    return inklusjon(a,a.length,b,b.length);
  }

  public static int xunion(int[] a, int m, int[] b, int n, int[] c)
  {
    if (m < 0 || m > a.length)
      throw new IllegalArgumentException("a[0:m> er ulovlig!");
    if (n < 0 || n > b.length)
      throw new IllegalArgumentException("b[0:n> er ulovlig!");

    int i = 0, j = 0, k = 0;

    while (i < m && j < n)
    {
      if (a[i] < b[j]) c[k++] = a[i++];
      else if (a[i] == b[j]) { i++; j++;}
      else c[k++] = b[j++];
    }
    while (i < m) c[k++] = a[i++];
    while (j < n) c[k++] = b[j++];

    return k;
  }

  public static int xunion(int[] a, int[] b, int[] c)
  {
    return xunion(a,a.length,b,b.length,c);
  }

  public static int maks(int[] a, int fra, int til)
  {
    if (fra < 0 || til > a.length || fra >= til)
    {
      throw new IllegalArgumentException("Illegalt intervall!");
    }

    int m = fra;              // indeks til største verdi i a[fra:til>
    int maksverdi = a[fra];   // største verdi i a[fra:til>

    for (int i = fra + 1; i < til; i++)
    {
      if (a[i] > maksverdi)
      {
        m = i;                  // indeks til største verdi oppdateres
        maksverdi = a[m];       // største verdi oppdateres
      }
    }

    return m;  // posisjonen til største verdi i a[fra:til>
  }


  public static void utvalgssortering(int[] a)
  {
    for (int k = a.length; k > 1; k--)
    {
      // bytter om: største tall i a[0:k> flyttes til plass k-1
      bytt(a,maks(a,0,k),k-1);   // maks ? se Programkode 1.2.1 b)
    }
  }

  public static <T> int maks(T[] a, int fra, int til, Comparator<? super T> c)
  {
    if (fra < 0 || til > a.length || fra >= til)
    {
      throw new IllegalArgumentException("Illegalt intervall!");
    }

    int m = fra;              // indeks til største verdi i a[fra:til>
    T maksverdi = a[fra];   // største verdi i a[fra:til>

    for (int i = fra + 1; i < til; i++)
    {
      if (c.compare(a[i], maksverdi) > 0)
      {
        m = i;                  // indeks til største verdi oppdateres
        maksverdi = a[m];       // største verdi oppdateres
      }
    }

    return m;  // posisjonen til største verdi i a[fra:til>
  }

  public static <T> void utvalgssortering(T[] a, Comparator<? super T> c)
  {
    for (int k = a.length; k > 1; k--)
    {
      // bytter om: største tall i a[0:k> flyttes til plass k-1
      bytt(a,maks(a,0,k,c),k-1);   // maks ? se Programkode 1.2.1 b)
    }
  }

  public static void snu(int[] a, int fra, int til)
  {
    fratilKontroll(til, fra, til);
    int v = fra, h = til - 1;
    while (v < h) bytt(a,v++,h--);
  }

  public static void snu(int[] a)
  {
    snu(a,0,a.length-1);
  }
}