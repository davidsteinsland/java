import bitio.*;

public class AdHuffmanTest
{
	public static void main(String[] args)
	{
		// Utskrift: (0,0) (1,9) (2,5,E) (3,5) (4,4) (5,3,D) (6,2,A) (7,2,F) . . .
		AdHuffman.skrivTre("EDEAEEFECDAFDB");
		
		String fraUrl = "http://www.iu.hio.no/~ulfu/appolonius/kap1/3/kap13.html";
		AdHuffman.komprimer(fraUrl,"ut.txt");
	}
}