package bitio;

import java.io.*;
import java.net.URL;
import java.util.Arrays;


public class AdHuffman     // adaptiv Huffman
{
  private static final int EOC = 256;   // End of Compression

  public static String[] ascii =
  {"NUL","SOH","STX","ETX","EOT","ENQ","ACK","BEL","BS","HT","LF",
   "VT","FF","CR","SO","SI","DLE","DC1","DC2","DC3","DC4","NAK",
   "SYN","ETB","CAN","EM","SUB","ESC","FS","GS","RS","US"};

  private final static class Node          // en indre nodeklasse
  {
    private int frekvens;                  // nodens frekvens
    private int c;                         // nodens tegn
    private int nummer;                    // nodens nummer
    private Node forelder;                 // peker til forelder
    private Node venstre = null;           // peker til venstre barn
    private Node høyre = null;             // peker til høyre barn

    private Node(int frekvens, int c, int nummer, Node forelder)
    {
      this.frekvens = frekvens;
      this.c = c;
      this.nummer = nummer;
      this.forelder = forelder;
    }

    @Override
    public String toString()
    {
      String s = "(" + nummer + "," + frekvens;
      if (c >= 0)  // bladnoder har c = -1
      {
        if (c < 32) s += "," + ascii[32];
        else s += "," + (char)c;
      }
      return s + ")";
    }

  }  // Node

  private Node rot, NULL;         // pekere til rotnoden og nullnoden
  private Node[] noder;           // nodetabell for nodene
  private Node[] tegn;            // nodetabell for tegn
  int antall = 0;                 // antall noder i treet

  public AdHuffman()              // kontruktør - lager et tomt tre
  {
    rot = NULL = new Node(0, -1, 0, null);   // rotnoden er lik nullnoden
    noder = new Node[2 * 8 + 1];             // plass til 17 noder (8 tegn)
    noder[antall++] = rot;                   // roten legges i posisjon 0
    tegn = new Node[257];                    // alle ascii-verdier + EOC
  }

  private static void bytt(Node p, Node q, Node[] noder)
  {
    Node f = p.forelder, g = q.forelder;  // finner foreldrene

    if (p == f.venstre) f.venstre = q;    // f får q som barn
    else f.høyre = q;

    if (q == g.høyre) g.høyre = p;        // g får p som barn
    else g.venstre = p;

    p.forelder = g;                // p får g som forelder
    q.forelder = f;                // q får f som forelder

    noder[q.nummer] = p;           // p flyttes til plassen til q
    noder[p.nummer] = q;           // q flyttes til plasen til p

    int nummer = p.nummer;         // p og q bytter nummer
    p.nummer = q.nummer;
    q.nummer = nummer;
  }

  private Node nyttTegn(int c)                    // et nytt tegn
  {
    if (antall == noder.length)                   // er tabellen full?
    {
      noder = Arrays.copyOf(noder,2*antall - 1);  // dobler
    }

    Node p = NULL;                          // p settes lik nullnoden

    p.høyre = new Node(1,c,antall,p);       // ny node som høyre barn
    tegn[c] = p.høyre;                      // noden inn i tegn-tabellen
    noder[antall++] = p.høyre;              // noden inn i nodetabellen

    p.venstre = new Node(0,-1,antall,p);    // ny node som venstre barn
    noder[antall++] = p.venstre;            // noden inn i nodetabellen

    NULL = p.venstre;                       // ny nullnode

    if (p == rot) return p;                 // returnerer roten

    p.frekvens = 1;                         // frekvens lik 1
    return p.forelder;
  }

  private void oppdater(int c)
  {
    Node p = tegn[c];                  // slår opp i tegntabellen
    if (p == null) p = nyttTegn(c);    // er det et nytt tegn?

    while (p != rot)                   // går fra p og opp mot roten
    {
      // sammenligner p med noden rett foran
      if (noder[p.nummer - 1].frekvens == p.frekvens)
      {
        int k = p.nummer - 1;          // leter videre mot venstre
        while (noder[k-1].frekvens == p.frekvens) k--;

        Node q = noder[k];                      // q er minst
        if (q != p.forelder) bytt(p,q,noder);   // p og q bytter plass
      }

      p.frekvens++;                             // øker frekvensen
      p = p.forelder;                           // går til forelderen
    }
  }

  public static void skrivTre(String melding)
  {
    AdHuffman h = new AdHuffman();             // lager et tomt tre

    char[] tegn = melding.toCharArray();       // gjør om til en tegntabell
    for (char c : tegn) h.oppdater(c);         // bygger opp treet

    for (int i = 0; i < h.antall; i++)         // skriver ut nodene
      System.out.print(h.noder[i] + " ");
  }

  private void skrivBitkode(int c, BitOutputStream ut) throws IOException
  {
    int biter = 0, lengde = 0;
    Node blad = tegn[c];

    Node p = blad != null ? blad : NULL;  // p tegnnode eller nullnoden

    while (p != rot)    // går oppover mot roten
    {
      lengde++;
      biter >>>= 1;
      if (p.forelder.høyre == p) biter |= 0x80000000;
      p = p.forelder;
    }

    ut.writeLeftBits(biter,lengde);
    if (blad == null) ut.writeBits(c,9);  // tegnet med 9 biter
  }

  public static void
  komprimer(String fraUrl, String tilFil) throws IOException
  {
     InputStream inn =
      new BufferedInputStream((new URL(fraUrl)).openStream());  // inn

     BitOutputStream ut = new BitOutputStream(tilFil);          // ut

    AdHuffman h = new AdHuffman();   // oppretter et tomt Huffmantre

    int c;
    while ((c = inn.read()) != -1)   // leser til filslutt
    {
      h.skrivBitkode(c,ut);          // skriver ut bitkoden
      h.oppdater(c);                 // oppdaterer Huffmantreet
    }

    h.skrivBitkode(EOC,ut);          // Vaktpost: End of Compression

    inn.close(); ut.close();         // lukker filene
  }

  public static void
  dekomprimer(String fraUrl, String tilFil) throws IOException
  {
    BitInputStream inn =
      new BitInputStream((new URL(fraUrl)).openStream());      // inn

    OutputStream ut =
      new BufferedOutputStream(new FileOutputStream(tilFil));  // ut

    AdHuffman h = new AdHuffman();         // et tomt Huffmantre

    for (Node p = h.rot; ; p = h.rot)      // starter i roten
    {
      while (p.venstre != null)            // er p er en bladnode?
      {
        if (inn.readBit() == 0)
          p = p.venstre;                   // til venstre ved 0-bit
        else
          p = p.høyre;                     // til høyre ved 1-bit
      }

      int c = p.c;                         // tegnet i noden
      if (c == -1) c = inn.readBits(9);    // er p lik nullnoden?

      if (c == EOC) break;                 // End of Compression
      ut.write(c);                         // skriver ut tegnet
      h.oppdater(c);                       // oppdaterer Huffmantreet
    }

    inn.close(); ut.close();               // lukker filene
  }

}