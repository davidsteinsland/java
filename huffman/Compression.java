import bitio.*;
import java.io.*;
import java.net.URL;

public class Compression
{
	/**
	 * Usage:
	 * java Compression <method> <Url> <file>
	 * method = [lzw, huffman]
	 */
	public static void main (String[] args)
		throws IOException
	{
		if ( args.length < 3 )
			usage();
		
		String method = args[0];
		
		if ( ! method.equalsIgnoreCase ("lzw") && ! method.equalsIgnoreCase ("huffman"))
			usage();
		
		long t = 0;
		
		if ( method.equalsIgnoreCase ("lzw"))
		{
			t = System.currentTimeMillis();
			LZW.komprimer2 (args[1], args[2]);
			t = System.currentTimeMillis() - t;
			
			System.out.println ("LZW.komprimer2():");
			System.out.println (t + " ms");
		}
		else
		{
			t = System.currentTimeMillis();
			Huffman.compress (args[1], args[2]);
			t = System.currentTimeMillis() - t;
			
			System.out.println ("Huffman.compress():");
			System.out.println (t + " ms");
		}
	}
	
	private static void usage()
	{
		System.err.println ("java Compression lzw|huffman <url> <file>");
		System.exit(1);
	}
}