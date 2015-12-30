package org.cakelab.blender.nio;

/**
 * This class provides methods to deal with unsigned long values in Java.
 * 
 * Please note that it is just a helper class and not an equivalent to {@link Long}.
 * 
 * @author homac
 *
 */
public class UnsignedLong {
	
	public static final long MIN_VALUE = 0x0L;
	public static final long MAX_VALUE = 0xFFFFFFFFFFFFFFFFL;

	
	public static int compare(long ul1, long ul2) {
		if (ul1<0 || ul2<0) {
			int result = Long.compare(ul1>>>1, ul2>>>1);
			return result != 0 ? result : Integer.compare((int)ul1&1, (int)ul2&1);
		} else {
			return Long.compare(ul1, ul2);
		}
	}

	/**
	 * @param ul1
	 * @param ul2
	 * @return ul1 - ul2
	 */
	public static long minus(long ul1, long ul2) {
		return ul1 - ul2;
	}

	/**
	 * 
	 * @param ul1
	 * @param ul2
	 * @return ul1 + ul2
	 */
	public static long plus(long ul1, long ul2) {
		return ul1 + ul2;
	}
	
	/**
	 * 
	 * @param ul1
	 * @param ul2
	 * @return ul1 - ul2
	 */
	public static boolean le(long ul1, long ul2) {
		return compare(ul1, ul2) <= 0;
	}
	
	/**
	 * 
	 * @param ul1
	 * @param ul2
	 * @return ul1 < ul2
	 */
	public static boolean lt(long ul1, long ul2) {
		return compare(ul1, ul2) < 0;
	}
	
	/**
	 * 
	 * @param ul1
	 * @param ul2
	 * @return ul1 >= ul2
	 */
	public static boolean ge(long ul1, long ul2) {
		return compare(ul1, ul2) >= 0;
	}
	
	/**
	 * 
	 * @param ul1
	 * @param ul2
	 * @return ul1 > ul2
	 */
	public static boolean gt(long ul1, long ul2) {
		return compare(ul1, ul2) > 0;
	}
	
	/**
	 * @param ul1
	 * @param ul2
	 * @return ul1 == ul2
	 */
	public static boolean eq(long ul1, long ul2) {
		return ul1 == ul2;
	}


	
}
