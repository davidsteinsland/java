import bitio.*;

public class AdHuffmanTest
{
	public static void main(String[] args)
		throws Exception
	{
		// Utskrift: (0,0) (1,9) (2,5,E) (3,5) (4,4) (5,3,D) (6,2,A) (7,2,F) . . .
		AdHuffman.skrivTre("EDEAEEFECDAFDB");
		
		String fraUrl = "http://www.iu.hio.no/~ulfu/appolonius/kap1/3/kap13.html";
		AdHuffman.komprimer(fraUrl,"ut.txt");
		
		fraUrl = "file:///C:/Users/david/java/huffman/base64_test.txt";
		AdHuffman.komprimer(fraUrl,"base64_huffman.txt");
	}
}