package org.cakelab.blender.io;

import java.nio.ByteOrder;

/**
 * This class represents architecture dependent native data 
 * encodings considering address width and byte order. 
 * 
 * @author homac
 *
 */
public class Encoding {
	
	public static final int ADDR_WIDTH_64BIT = 8;
	public static final int ADDR_WIDTH_32BIT = 4;
	private static final int BIG_ENDIAN = 1<<16;
	private static final int LITTLE_ENDIAN = 0;
	
	public static final Encoding LITTLE_ENDIAN_64BIT = new Encoding(ByteOrder.LITTLE_ENDIAN, ADDR_WIDTH_64BIT);
	public static final Encoding BIG_ENDIAN_64BIT = new Encoding(ByteOrder.BIG_ENDIAN, ADDR_WIDTH_64BIT);
	public static final Encoding LITTLE_ENDIAN_32BIT = new Encoding(ByteOrder.LITTLE_ENDIAN, ADDR_WIDTH_32BIT);
	public static final Encoding BIG_ENDIAN_32BIT = new Encoding(ByteOrder.BIG_ENDIAN, ADDR_WIDTH_32BIT);
	
	public static final int LITTLE_ENDIAN_64BIT_ID = LITTLE_ENDIAN | ADDR_WIDTH_64BIT;
	public static final int LITTLE_ENDIAN_32BIT_ID = LITTLE_ENDIAN | ADDR_WIDTH_32BIT;
	public static final int BIG_ENDIAN_64BIT_ID    = BIG_ENDIAN | ADDR_WIDTH_64BIT;
	public static final int BIG_ENDIAN_32BIT_ID    = BIG_ENDIAN | ADDR_WIDTH_32BIT;
	
	
	public static final Encoding JAVA_NATIVE = BIG_ENDIAN_64BIT;
	
	
	private final ByteOrder byteOrder;
	private final int addressWidth;
	private final int id;

	private Encoding(ByteOrder byteOrder, int addrWidth) {
		this.byteOrder = byteOrder;
		this.addressWidth = addrWidth;
		this.id = getId(byteOrder, addrWidth);
	}

	public int id() {
		return id;
	}
	
	public ByteOrder getByteOrder() {
		return byteOrder;
	}

	public int getAddressWidth() {
		return addressWidth;
	}

	private static final int getId(ByteOrder byteOrder, int addressWidth) {
		return ((byteOrder == ByteOrder.BIG_ENDIAN)? BIG_ENDIAN : LITTLE_ENDIAN) | addressWidth;
	}

	public static Encoding get(ByteOrder byteOrder, int addressWidth) {
		switch (getId(byteOrder, addressWidth)) {
		case LITTLE_ENDIAN_64BIT_ID:
			return LITTLE_ENDIAN_64BIT;
		case BIG_ENDIAN_64BIT_ID:
			return BIG_ENDIAN_64BIT;
		case LITTLE_ENDIAN_32BIT_ID:
			return LITTLE_ENDIAN_32BIT;
		case BIG_ENDIAN_32BIT_ID:
			return BIG_ENDIAN_32BIT;
		default:
			return null;
		}
	}

	/**
	 * Determines the encoding used by the current system.
	 * @return
	 */
	public static Encoding nativeEncoding() {
		// unfortuantely there is no reliable way to detect, whether it is a 
		// 64 or 32 bit architecture. So, we just assume that the operating system 
		// actually is 64bit if property "os.arch" tells so, even if we know, that
		// it can even be 64bit if it does not.
		if (System.getProperty("os.arch").contains("64")) {
			return get(ByteOrder.nativeOrder(), ADDR_WIDTH_64BIT);
		} else {
			return get(ByteOrder.nativeOrder(), ADDR_WIDTH_32BIT);
		}
	}


}
