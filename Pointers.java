/**
 * Just a proof of concept, or whatever.
 * We don't talk that much about pointers in Java,
 * but it could be interesting to show what the difference
 * between Java and C/C++ is.
 */
class IntObj
{
	int value;
}

public class Pointers
{
	public static void main (String[] args)
	{
		IntObj a; // allocate the pointers a and b
		IntObj b; // (but not the IntObj pointees)

		a = new IntObj(); // allocate an IntObj pointee
						  // and set a to point to it
		a.value = 25; // Dereference a to store 25 in its pointee
		b.value = 10; // CRASH! b does not have a pointee yet

		b = a; // pointer assignment; b points to a's pointee
		b.value = 8; // dereference b to store 8 in its (shared) pointee
	}

	/**
	 * The C version of the above code:
	 */
	/*
	int *a,
		*b;
	
	a = (int*)malloc (sizeof (int));
	*a = 25;
	*b = 10; // CRASH!

	b = a;
	*b = 8;
	*/
}

// vim: set ts=4 sw=4 tw=150 noet :
