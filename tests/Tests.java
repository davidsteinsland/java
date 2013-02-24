public class Tests
{
	public static void main (String[] args)
	{
		long time = 0;
		int[] a = new int[100000];
		for (int i = 0; i < a.length; i++)
			a[i] = i;
		
		int[] times = new int[3];
		
		for (int i = 0; i < 10; i++)
		{
			time = t();
			max (a);
			time = t() - time;
			out ("max(a): " + time + " ms\t");
			times[0] += time;
			
			time = t();
			max2 (a);
			time = t() - time;
			out ("max2(a): " + time + " ms\t");
			times[1] += time;
			
			time = t();
			max3 (a);
			time = t() - time;
			out ("max3(a): " + time + " ms\n");
			times[2] += time;
		}
		
	}
	
	private static void out (String s)
	{
		System.out.print (s);
	}
	
	private static long t()
	{
		return System.currentTimeMillis();
	}
	
	private static void max (int[] a)
	{
		int i = 0,
			max = a[0];
		for (int j = 0; j < a.length; j++)
		{
			if (a[j] > max)
			{
				i = j;
				max = a[j];
			}
		}
	}
	
	private static void max2 (int[] a)
	{
		int i = 0,
			max = a[0];
		for (int j = 0, len = a.length; j < len; j++)
		{
			if (a[j] > max)
			{
				i = j;
				max = a[j];
			}
		}
	}
	
	private static void max3 (int[] a)
	{
		int[] max = new int[]{0, a[0]};
		
		for (int i = 0; i < a.length; i++)
			if (a[i] > max[1])
				max = new int[]{i, a[i]};
	}
}