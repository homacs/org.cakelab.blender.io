package org.cakelab.blender.model;

public class UnsignedLong {
	
	public static final long MIN_VALUE = 0x0L;
	public static final long MAX_VALUE = 0xFFFFFFFFFFFFFFFFL;

	
	public static int compare(long l1, long l2) {
		if (l1<0 || l2<0) {
			int result = Long.compare(l1>>>1, l2>>>1);
			return result != 0 ? result : Integer.compare((int)l1&1, (int)l2&1);
		} else {
			return Long.compare(l1, l2);
		}
	}


	public static long minus(long higher, long lower) {
		return higher - lower;
	}

	public static long plus(long l1, long l2) {
		return l1 + l2;
	}
	

	public static boolean le(long l1, long l2) {
		return compare(l1,l2) <= 0;
	}
	
	public static boolean lt(long l1, long l2) {
		return compare(l1,l2) < 0;
	}
	
	public static boolean ge(long l1, long l2) {
		return compare(l1,l2) >= 0;
	}
	
	public static boolean gt(long l1, long l2) {
		return compare(l1,l2) > 0;
	}
	
	public static boolean eq(long l1, long l2) {
		return compare(l1,l2) == 0;
	}


	
}
