import bitio.*;
import java.io.*;
import java.net.URL;

public class HuffmanTest
{
	public static void main (String[] args)
	{
		int[] freq = new int[256];
		freq['A'] = 12;		freq['E'] = 28;
		freq['B'] = 7;		freq['F'] = 9;
		freq['C'] = 3;		freq['G'] = 5;
		freq['D'] = 14;		freq['H'] = 22;
		
		String[] bitcodes = Huffman.bitcodes(freq);
		
		for (int i = 0; i < freq.length; i++)
			if ( freq[i] > 0 )
				System.out.print ((char)i + " = " + bitcodes[i] + " ");
		
		System.out.println ("\n===================\n");
		
		String text = "aaaaiiiooaaaaaaiiiiiooooaaaaaaaaiiiiiiiooooooh";
		freq = Huffman.frequency (text);
		bitcodes = Huffman.bitcodes(freq);
		int numBits = 0;
		for (int i = 0; i < freq.length; i++)
			if ( freq[i] > 0 )
			{
				numBits += freq[i] * bitcodes[i].length();
				System.out.print ((char)i + " = " + bitcodes[i] + " ");
			}
		
		System.out.println ("\nCompressed: " + numBits + " bits");
		System.out.println ( "Original size: " + text.length() * 8 + " bits");
		System.out.println ("Diff: " + (text.length() - (numBits/8)) + " bytes");
		
		System.out.println ("\n===================\n");
		String url = "http://www.iu.hio.no/~ulfu/appolonius/kap1/3/kap13.html";
		try (InputStream in = new java.io.BufferedInputStream((new java.net.URL(url)).openStream()))
		{
			freq = Huffman.frequency (in);
			bitcodes = Huffman.bitcodes(freq);
			
			for (int i = 0; i < bitcodes.length; i++)
				if (bitcodes[i] != null)
					System.out.printf ("%-3s = %s %d\n",
					(i < 32) ? Huffman.ascii[i] : "" + (char)i,
					bitcodes[i], freq[i]);
			
		}
		catch (IOException e)
		{
			System.err.println ( e.getMessage() );
		}
		
		System.out.println ("\n===================\n");
		
		String s = "ABBCCCDDDDDEEEEEEEEFFFFFFFFFFFFFGGGGGGGGGGGGGGGGGGGGG";
		try (InputStream in = new java.io.ByteArrayInputStream(s.getBytes()))
		{
			bitcodes = Huffman.bitcodes(Huffman.frequency(in));

			for (int i = 0; i < bitcodes.length; i++)
			if (bitcodes[i] != null)
				System.out.print((char)i + " = " + bitcodes[i] + "  ");
		}
		catch (IOException e)
		{
			System.err.println ( e.getMessage() );
		}
		
		System.out.println ("\n===================\n");
		text = "This is a test!";
		try (InputStream in = new ByteArrayInputStream (text.getBytes());
			ByteArrayOutputStream out = new ByteArrayOutputStream ())
		{
			System.out.println ( Huffman.compress (in, out) );
		}
		catch (IOException e)
		{
			System.err.println ( e.getMessage() );
		}
		
		System.out.println ("\n===================\n");
		
		String fromUrl = "file:///C:/Users/David/Java/huffman/org.txt";
		try
		{
			Huffman.compress (fromUrl, "org.huf");
		}
		catch (IOException e)
		{
			System.err.println ( e.getMessage() );
		}
		
		fromUrl = "file:///C:/Users/David/Java/huffman/org.huf";
		try
		{
			long t = System.currentTimeMillis();
			Huffman.decompress (fromUrl, "out.txt");
			System.out.println ("Time: " + (System.currentTimeMillis() - t) + " ms");
			
			t = System.currentTimeMillis();
			Huffman.decompressEfficient (fromUrl, "out2.txt");
			System.out.println ("Time: " + (System.currentTimeMillis() - t) + " ms");
			
			t = System.currentTimeMillis();
			Huffman.decompressMoreEfficient (fromUrl, "out3.txt");
			System.out.println ("Time: " + (System.currentTimeMillis() - t) + " ms");
		}
		catch (IOException e)
		{
			System.err.println ( e.getMessage() );
		}
	}
}