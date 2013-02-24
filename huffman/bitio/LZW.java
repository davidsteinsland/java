package bitio;

import hjelpeklasser.*;
import java.io.*;
import java.net.URL;
import java.util.*;

public class LZW
{
	private static final int LZW17 = 17;
	private static final int NYTT_BITFORMAT = 256;
	private static final int FØRSTE_KODE = 257;
	private static final int MAKSKODE = (1 << LZW17) - 1;
	
	public static void komprimerTilKonsoll(String melding)
	{
		char c = melding.charAt(0);
		String s = "" + c;
		int kode = c;
		int nestekode = 256;

		Map<String,Integer> ordbok = new TreeMap<>();

		for (int i = 1; i < melding.length(); i++)
		{
			c = melding.charAt(i);

			Integer ordkode = ordbok.get(s + c);
			if (ordkode == null)
			{
				System.out.println(kode + " " + Integer.toBinaryString(kode));
				ordbok.put(s + c, nestekode);
				nestekode++;
				s = "" + c;
				kode = c;
			}
			else
			{
				kode = ordkode;
				s = s + c;
			}
		}
		System.out.println(kode);

	}

	public static void dekomprimerFraTabell(int[] a)
	{
		int kode = a[0];
		int nestekode = 256;

		String denne  = "" + (char)kode;
		String forrige = null;
		char c;

		System.out.print(denne);

		Map<Integer,String> ordbok = new TreeMap<>();

		for (int i = 1; i < a.length; i++)
		{
			kode = a[i];
			forrige = denne;

			if (kode < nestekode)
			{
				denne = kode < 256 ? "" + (char)kode : ordbok.get(kode);
				c = denne.charAt(0);
			}	
			else
			{
				c = forrige.charAt(0);
				denne = forrige + c;
			}

			System.out.print(denne);
			ordbok.put(nestekode,forrige + c);
			nestekode++;
		}
	}

	public static void komprimerSlow(String fraUrl, String tilFil)
		throws IOException
	{
		int bitformat = 9;
		int bitGrense = 512;

		BitOutputStream ut = BitOutputStream.toFile(tilFil);
		InputStream inn = new BufferedInputStream((new URL(fraUrl)).openStream());

		int c = inn.read();
		if (c == -1)
			return;

		Map<String,Integer> ordbok = new HashMap<>();

		String s = "" + (char)c;
		int kode = c;
		int nesteKode = FØRSTE_KODE;

		Integer ikode = null;

		while ((c = inn.read()) != -1)
		{
			ikode = ordbok.get(s + (char)c);

			if (ikode == null)
			{
				if (kode >= bitGrense)
				{
					ut.writeBits(NYTT_BITFORMAT,bitformat);
					bitformat++;
					bitGrense *= 2;
				}

				ut.writeBits(kode,bitformat);

				if (nesteKode < MAKSKODE)
				{
					ordbok.put(s + (char)c, nesteKode);
					nesteKode++;
				}

				s = "" + (char)c;
				kode = c;
			}
			else
			{
				kode = ikode;
				s += (char)c;
			}
		}

		if (kode >= bitGrense)
		{
			ut.writeBits(NYTT_BITFORMAT,bitformat);
			bitformat++;
		}

		ut.writeBits(kode,bitformat);

		ut.close();
		inn.close();
	}

	public static void dekomprimer(String fraUrl, String tilFil)
		throws IOException
	{
		int bitformat = 9;

		BitInputStream inn = new BitInputStream((new URL(fraUrl)).openStream());

		BufferedWriter ut = new BufferedWriter(new FileWriter(tilFil));

		int kode = inn.readBits(bitformat);
		if (kode == -1)
			return;

		Map<Integer,String> ordbok = new HashMap<>();

		int nesteKode = FØRSTE_KODE;

		String denne  = "" + (char)kode;
		String forrige = null;

		char c;

		ut.write(denne);

		while ((kode = inn.readBits(bitformat)) != -1)
		{
			if (kode == NYTT_BITFORMAT)
			{
				bitformat++;
				kode = inn.readBits(bitformat);
			}

			forrige = denne;

			if (kode < nesteKode)
			{
				if (kode < 256)
					denne = "" + (char)kode;
				else
					denne = ordbok.get(kode);

				c = denne.charAt(0);
			}
			else
			{
				c = forrige.charAt(0);
				denne = forrige + c;
			}

			ut.write(denne);


			ordbok.put(nesteKode++,forrige + c);

		}

		inn.close();
		ut.close();
	}







	private static class Node implements Comparable<Node>
	{
		private byte tegn;
		private int forelder;

		private Node(byte tegn, int forelder)
		{
			this.tegn = tegn;
			this.forelder = forelder;
		}

		public int hashCode()
		{
			return (tegn << 7) ^ forelder;
		}

		public boolean equals(Object o)
		{
			if (this == o)
				return true;
			if (!(o instanceof Node))
				return false;
			Node p = (Node)o;
			return forelder == p.forelder && tegn == p.tegn;
		}

		public int compareTo(Node p)
		{
			if (forelder < p.forelder)
				return -1;
			else if (forelder > p.forelder)
				return 1;
			else
				return tegn - p.tegn;
		}
	}

	public static void komprimer(String fraUrl, String tilFil)
		throws IOException
	{
		int bitformat = 9;
		int bitGrense = 512;

		BitOutputStream ut = BitOutputStream.toFile(tilFil);
		InputStream inn = new BufferedInputStream((new URL(fraUrl)).openStream());

		int c = inn.read();
		if (c == -1)
			return;

		Map<Node,Integer> ordbok = new TreeMap<>();

		int kode = c;
		int nesteKode = FØRSTE_KODE;

		while ((c = inn.read()) != -1)
		{
			Node p = new Node((byte)c,kode);

			Integer ikode = ordbok.get(p);

			if (ikode == null)
			{
				if (kode >= bitGrense)
				{
					ut.writeBits(NYTT_BITFORMAT,bitformat);
					bitformat++;
					bitGrense *= 2;
				}

				ut.writeBits(kode,bitformat);

				if (nesteKode < MAKSKODE)
				{
					ordbok.put(p,nesteKode);
					nesteKode++;
				}

				kode = c;
			}
			else
			{
				kode = ikode;
			}
		}

		if (kode >= bitGrense)
		{
			ut.writeBits(NYTT_BITFORMAT,bitformat);
			bitformat++;
		}

		ut.writeBits(kode,bitformat);

		ut.close();
		inn.close();

	}

	public static void dekomprimer2(String fraUrl, String tilFil)
		throws IOException
	{
		BitInputStream inn = new BitInputStream((new URL(fraUrl)).openStream());
	  
		BufferedWriter ut = new BufferedWriter(new FileWriter(tilFil));

		int bitformat = 9;

		int kode = inn.readBits(bitformat);
		if (kode == -1) return;

		Map<Integer,Node> ordbok = new HashMap<>();

		for (int i = 0; i < 256; i++)
			ordbok.put(i,new Node((byte)i,-1));

		Stakk<Integer> s = new TabellStakk<>();

		int nesteKode = FØRSTE_KODE;
		int forelder = 0;
		int forrige = kode;
		int c = kode;

		ut.write(c);

		while ((kode = inn.readBits(bitformat)) != -1)
		{
			if (kode == NYTT_BITFORMAT)
			{
				bitformat++;
				kode = inn.readBits(bitformat);
			}

			if (kode < nesteKode)
			{
				forelder = kode;
			}
			else
			{
				forelder = forrige;
				s.leggInn(c);
			}
		  
			while (forelder != -1)
			{
				Node p = ordbok.get(forelder);
				s.leggInn((int)p.tegn);
				forelder = p.forelder;
			}

			c = s.kikk();

			while (!s.tom())
				ut.write(s.taUt());

			if (nesteKode < MAKSKODE)
			{
				ordbok.put(nesteKode,new Node((byte)c,forrige));
				nesteKode++;
			}
		  
			forrige = kode;
		}

		inn.close();
		ut.close();
	}
}