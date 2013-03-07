package bitio;

import java.util.*;
import java.io.*;
import java.net.URL;
import hjelpeklasser.*;

public class Huffman                          // klasse for komprimering
{
  public static String[] ascii =
  {"NUL","SOH","STX","ETX","EOT","ENQ","ACK","BEL","BS","HT","LF",
   "VT","FF","CR","SO","SI","DLE","DC1","DC2","DC3","DC4","NAK",
   "SYN","ETB","CAN","EM","SUB","ESC","FS","GS","RS","US"};

  private static class Node                   // en basisklasse
  {
    private int frekvens;                     // nodens frekvens
    private Node venstre;                     // pekere til venstre barn
    private Node høyre;                       // pekere til høyre barn
    private Node() {}                         // standardkonstruktør

    private Node(int frekvens, Node v, Node h)   // konstruktør
    {
      this.frekvens = frekvens;
      venstre = v;
      høyre = h;
    }

  }  // slutt på class Node

  private static class BladNode extends Node  // en subklasse
  {
    private char tegn;                        // bladnodens tegn
    private BladNode(char tegn, int frekvens)     // konstruktør
    {
      super(frekvens, null, null);  // kaller basisklassens konstruktør
      this.tegn = tegn;
    }

  }  // slutt på class BladNode

  private static class FrekvensKomparator implements Comparator<Node>
  {
    public int compare(Node p, Node q)  // minst frekvens kommer først
    {
      return p.frekvens - q.frekvens;   // se Oppgave 4
    }
  }

  private static Node byggHuffmanTre(int[] frekvens)
  {
    Comparator<Node> c = new FrekvensKomparator();  // en komparator
    PrioritetsKø<Node> kø = new HeapPrioritetsKø<>(c);

    for (int i = 0; i < frekvens.length; i++)
      if (frekvens[i] > 0)          // dette tegnet skal være med
        kø.leggInn(new BladNode((char)i, frekvens[i]));

    if (kø.antall() < 2)            // må ha minst to noder
      throw new IllegalArgumentException("Det er for få tegn!");

    while (kø.antall() > 1)
    {
      Node v = kø.taUt();                  // blir venstre barn
      Node h = kø.taUt();                  // blir høyre barn
      int sum = v.frekvens + h.frekvens;   // summen av frekvensene

      kø.leggInn(new Node(sum, v, h));     // legger noden inn i køen
    }

    return kø.taUt();                      // roten i treet
  }

  private static void finnBitkoder(Node p, String kode, String[] koder)
  {
    if (p instanceof BladNode) koder[((BladNode)p).tegn] = kode;
    else
    {
      finnBitkoder(p.venstre, kode + '0', koder);  // 0 til venstre
      finnBitkoder(p.høyre, kode + '1', koder);    // 1 til høyre
    }
  }

  public static String[] stringBitkoder(int[] frekvens)
  {
    Node rot = byggHuffmanTre(frekvens);               // bygger treet

    String[] bitkoder = new String[frekvens.length];   // en kodetabell
    finnBitkoder(rot,"",bitkoder);                     // lager bitkodene

    return bitkoder;    // returnerer tabellen
  }

  public static int[] stringFrekvens(String tekst)
  {
    int[] frekvens = new int[256];

    for (int i = 0; i < tekst.length(); i++)
      frekvens[tekst.charAt(i)]++;

    return frekvens;
  }

  public static int[] streamFrekvens(InputStream inn) throws IOException
  {
    int[] frekvens = new int[256];

    int tegn = 0;
    while ((tegn = inn.read()) != -1) frekvens[tegn]++;
    inn.close();

    return frekvens;
  }

  private static void finnLengder(Node p, int lengde, int[] lengder)
  {
    if (p.venstre == null)                          // p er en bladnode
    {
      lengder[((BladNode)p).tegn]  = lengde;        // tegnets lengde
    }
    else
    {
      finnLengder(p.venstre, lengde + 1, lengder);  // lengde øker med 1
      finnLengder(p.høyre, lengde + 1, lengder);    // lengde øker med 1
    }
  }

  public static int[] finnBitkoder(int[] lengder)
  {
    int[] blader = new int[32];  // antall tegn av hver lengde

    for (int lengde : lengder)
      if (lengde < 32) blader[lengde]++;    // teller opp
      else throw new IllegalStateException("Bitkodelengde > 31!");

    int[] pos = new int[32];  // posisjonen til første bladnode

    for (int k = 31; k > 0; k--) pos[k - 1] = (pos[k] + blader[k])/2;

    int[] bitkoder = new int[lengder.length];

    for (int i = 0; i < bitkoder.length; i++)
      if (lengder[i] > 0) bitkoder[i] = pos[lengder[i]]++;

    return bitkoder;
  }

  public static int antBinSiffer(int k)  // antall binære siffer
  {
    return k == 0 ? 0 : 32 - Integer.numberOfLeadingZeros(k);
  }

  public static void
  komprimer(String fraUrl, String tilFil) throws IOException
  {
    InputStream inn = new BufferedInputStream
      ((new URL(fraUrl)).openStream());          // åpner inn-filen

    int[] frekvens = streamFrekvens(inn);        // frekvenstabellen
    Node rot = byggHuffmanTre(frekvens);         // bygger Huffmantreet

    int[] lengder = new int[frekvens.length];
    finnLengder(rot, 0, lengder);                // bitkodelengdene

    int[] bitkoder = finnBitkoder(lengder);      // bitkodene

    int vaktpost = Tabell.maks(lengder);         // vaktposttegnet
    int k = antBinSiffer(lengder[vaktpost]);     // antall siffer

    BitOutputStream ut =
      new BitOutputStream(tilFil);               // ut-filen

    ut.writeBits(k, 3);                          // maks antall siffer

    for (int lengde : lengder)                   // tegn og lengder
    {
      if (lengde == 0) ut.write0Bit();           // ikke med hvis 0
      else
        ut.writeBits(lengde | 1 << k, k + 1);    // 1 + lengde
    }

    int s = antBinSiffer(frekvens[vaktpost]);    // antall siffer
    ut.writeBits(s, 5);                          // skriver ut
    ut.writeBits(frekvens[vaktpost], s);         // vaktpostens frekvens

    inn = new BufferedInputStream
      ((new URL(fraUrl)).openStream());          // åpner på nytt

    int tegn = 0;
    while ((tegn = inn.read()) != -1)   // leser ett og ett tegn
    {
      ut.writeBits(bitkoder[tegn], lengder[tegn]);  // skriver bitkoden
    }

    ut.writeBits(bitkoder[vaktpost], lengder[vaktpost]);  // vaktposten

    inn.close();     // lukker inn-filen

    ut.close();      // lukker ut-filen
  }

  private static Node byggKanoniskTre(int[] lengder)
  {
    int[] bitkoder = finnBitkoder(lengder);   // bitkodene
    Node rot = new Node();                    // rotnoden

    for (int i = 0; i < lengder.length; i++)  // går gjennom tabellen
    {
      if (lengder[i] > 0)                     // denne skal være med
      {
        int n = bitkoder[i];                  // bitkoden til tegnet i
        int k = (1 << lengder[i]) >> 1;       // posisjonen til første bit
        Node p = rot;                         // starter i roten

        while (k > 1)                         // alle unntatt siste bit
        {
          if ((k & n) == 0)                   // biten på plass k
          {
            if (p.venstre == null) p.venstre = new Node();
            p = p.venstre;
          }
          else
          {
            if (p.høyre == null) p.høyre = new Node();
            p = p.høyre;
          }
          k >>= 1;   // flytter k en posisjon mot høyre
        }
        // lager bladnoden til slutt
        if ((n & 1) == 0) p.venstre = new BladNode((char)i,0);
        else p.høyre = new BladNode((char)i,0);
      }
    }

    return rot;  // roten til det kanoniske treet
  }

  public static void
  dekomprimer1(String fraUrl, OutputStream ut) throws IOException
  {
    BitInputStream inn =
      new BitInputStream((new URL(fraUrl)).openStream());  // åpner filen

    int k = inn.readBits(3);                  // antall biter i lengdene
    int[] lengder = new int[256];             // 256 mulige tegn

    for (int i = 0; i < lengder.length; i++)
    {
      if (inn.readBit() == 1)
      {
        lengder[i] = inn.readBits(k);
      }
    }

    int vaktpost = Tabell.maks(lengder);      // tegnet med størst lengde

    int s = inn.readBits(5);       // antall siffer i vaktpostens frekvens
    int vaktpostfrekvens = inn.readBits(s) + 1;

    Node rot = byggKanoniskTre(lengder);   // bygger treet
    int frekvens = 0;   // opptellingsvariabel

    for (Node p = rot; ; p = rot)
    {
      while (p.venstre != null)  // p er ikke en bladnode
        p = inn.readBit() == 0 ? p.venstre : p.høyre;

      if (((BladNode)p).tegn == vaktpost)
      {
        if (++frekvens == vaktpostfrekvens) break;  // ferdig
      }

      ut.write(((BladNode)p).tegn);  // skriver ut
    }

    ut.close();
    inn.close();    // lukker inn-filen
  }

  public static void
  dekomprimer1(String fraUrl, String tilFil) throws IOException
  {
    dekomprimer1(fraUrl, new BitOutputStream(tilFil));
  }

  public static byte[] lagTegntabell(int[] lengder, int[] tilbake, int n)
  {
    int[] bitkoder = finnBitkoder(lengder);    // finner bitkodene

    byte[] tegntabell = new byte[1 << n];      // en byte-tabell

    for (int i = 0; i < lengder.length; i++)   // går gjennom tabellen
      if (lengder[i] > 0)                      // tegn nr. i er med
      {
        int d = n - lengder[i];                // d er lengdeforskjellen
        tilbake[i] = d;                        // antall tilbake
        int fra = bitkoder[i] << d;            // starten på tegn nr. i
        int til = fra + (1 << d);              // slutten på tegn nr. i

        for (int j = fra; j < til; j++)        // fyller ut intervallet
          tegntabell[j] = (byte)i;             // med tegn nr. i
    }
    return tegntabell;
  }

  public static void
  dekomprimer2(String fraUrl, OutputStream ut) throws IOException
  {
    BitInputStream inn =
      new BitInputStream((new URL(fraUrl)).openStream());  // åpner filen

    int k = inn.readBits(3);                  // antall biter i lengdene
    int[] lengder = new int[256];             // 256 mulige tegn

    for (int i = 0; i < lengder.length; i++)
    {
      if (inn.readBit() == 1)
      {
        lengder[i] = inn.readBits(k);
      }
    }

    int vaktpost = Tabell.maks(lengder);      // tegnet med størst lengde

    int s = inn.readBits(5);       // antall siffer i vaktpostens frekvens
    int vaktpostfrekvens = inn.readBits(s) + 1;

    int n = lengder[vaktpost];                  // lengden til vaktposten
    int[] tilbake = new int[lengder.length];    // for tilbakelegging

    byte[] tegntabell = lagTegntabell(lengder, tilbake, n);

    int frekvens = 0;   // forekomster av vaktposttegnet

    for(;;)
    {
      int tegn = tegntabell[inn.readBits(n)] & 255;    // finner et tegn
      if (tegn == vaktpost)
      {
        if (++frekvens == vaktpostfrekvens) break;
      }

      ut.write(tegn);                           // skriver ut tegnet

      inn.unreadBits(tilbake[tegn]);            // legger biter tilbake
    }

    ut.close();   // lukker ut-filen
    inn.close();    // lukker inn-filen
  }

  public static void
  dekomprimer2(String fraUrl, String tilFil) throws IOException
  {
    dekomprimer2(fraUrl, new BitOutputStream(tilFil));
  }

  public static int[] treHøyde(int[] blader, int nivå)
  {
    int n = blader.length;         // n er antall nivåer i treet (0 til n-1)
    int[] noder = new int[n];      // antall noder på hvert nivå (0 til n-1)
    noder[n-1] = blader[n-1];      // kun bladnoder på nederste nivå

    for (int k = n - 1; k > nivå; k--)           // n-1 er nederste nivå
      noder[k - 1] = noder[k]/2 + blader[k-1];   // antall noder på nivå k-1

    int maks = noder[Tabell.maks(noder)];  // maks antall noder på et nivå

    int[] høyder = new int[maks];

    for (int i = n - 2; i >= nivå; i--)
    {
      int k = noder[i] - blader[i];  // antall indre noder på nivå i

      for (int j = 0; j < k; j++)
      {
        høyder[j] = Math.max(høyder[2*j],høyder[2*j+1]) + 1;
      }
      for (int j = k; j < noder[i+1]; j++) høyder[j] = 0;
    }

    int[] h = new int[noder[nivå] - blader[nivå]];
    System.arraycopy(høyder,0,h,0,h.length);

    return h;
  }

  public static void
  dekomprimer3(String fraUrl, OutputStream ut) throws IOException
  {
    BitInputStream inn =
      new BitInputStream((new URL(fraUrl)).openStream());  // åpner filen

    int k = inn.readBits(3);                  // antall biter i lengdene
    int[] lengder = new int[256];             // 256 mulige tegn

    for (int i = 0; i < lengder.length; i++)
    {
      if (inn.readBit() == 1)
      {
        lengder[i] = inn.readBits(k);
      }
    }

    int vaktpost = Tabell.maks(lengder);      // tegnet med størst lengde

    int s = inn.readBits(5);       // antall siffer i vaktpostens frekvens
    int vaktpostfrekvens = inn.readBits(s) + 1;

    int n = lengder[vaktpost];                // lengden til vaktposten
    int[] blader = new int[n + 1];            // n er nederste nivå

    for (int lengde : lengder)
      if (lengde > 0) blader[lengde]++;       // finner antallet på hvert nivå

    int[] bitkoder = finnBitkoder(lengder);   // finner bitkodene

    int m = (n + 1)/2;                      // det midterste nivået i treet
    byte[] tegntabell = new byte[1 << m];   // en byte-tabell

    int[] høyder = treHøyde(blader, m);     // de indre nodene på nivå m
    int grense = høyder.length;             // skiller indre noder og blader

    for (int i = 0; i < grense; i++)
    {
      tegntabell[i] = (byte)høyder[i];        // høydene først i tegntabellen
    }

    int[] tilbake = new int[lengder.length];  // for tilbakelegging

    byte[][] tegntabeller = new byte[grense][];    // en to-dimensjonal tabell

    for (int i = 0; i < grense; i++)
    {
      tegntabeller[i] = new byte[1 << høyder[i]];  // størrelse 1 << høyder[i]
    }

    for (int i = 0; i < lengder.length; i++)    // går gjennom alle lengdene
    {
      int lengde = lengder[i];                  // hjelpevariabel

      if (lengde > 0)                           // tegnet i skal være med
      {
        if (lengde <= m)                        // den store tabellen
        {
          int d = m - lengde;                   // lengdeforskjellen
          tilbake[i] = d;                       // antall tilbake

          int fra = bitkoder[i] << d;           // starten på tegn nr. i
          int til = fra + (1 << d);             // slutten på tegn nr. i

          for (int j = fra; j < til; j++)
            tegntabell[j] = (byte)i;            // fyller ut    
        }
        else                                    // de små tabellene
        {
          int kode = bitkoder[i];    // bitkoden til tegnet med i som asciiverdi
          int d1 = lengde - m;       // differensen mellom lengde og m

          int kode1 = kode >> d1;               // de m første bitene i kode
          int kode2 = kode & ((1 << d1) - 1);   // de d1 siste bitene i kode

          byte[] b = tegntabeller[kode1];       // finner rett tabell

          int d2 = tegntabell[kode1] - d1;      // differensen mellom høyden og d1
          tilbake[i] = d2;                      // antall tilbake

          int fra = kode2 << d2;                // starten på tegn i
          int til = fra + (1 << d2);            // slutten på tegn i

          for (int j = fra; j < til; j++) b[j] = (byte)i;  // fyller ut          
        }
      }
    }

    int frekvens = 0;   // forekomster av vaktposttegnet

    for(;;)
    {
      int lest = inn.readBits(m);               // leser m biter
      int tall = tegntabell[lest] & 255;        // slår opp i tegntabellen

      if (lest < grense)                        // lest gir en indre node
      {
        byte[] b = tegntabeller[lest];          // finner rett tabell
        lest = inn.readBits(tall);              // leser flere biter
        tall = b[lest] & 255;                   // slår opp i tabellen
      }

      // tall er nå ascii-verdien til et tegn

      if (tall == vaktpost)
      {
        if (++frekvens == vaktpostfrekvens) break;
      }

      ut.write(tall);                           // skriver ut tegnet

      inn.unreadBits(tilbake[tall]);            // legger biter tilbake
    }

    ut.close();    // lukker ut-filen
    inn.close();    // lukker inn-filen
  }

  public static void
  dekomprimer3(String fraUrl, String tilFil) throws IOException
  {
    dekomprimer3(fraUrl, new BitOutputStream(tilFil));
  }

}