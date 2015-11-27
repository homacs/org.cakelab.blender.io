package org.cakelab.blender.file.util;

import java.io.IOException;
import java.io.RandomAccessFile;

public class LittleEndianDataInputStream extends CDataFileInputStream {


	public LittleEndianDataInputStream(RandomAccessFile in, int pointerSize) {
		super(in, pointerSize);
	}
	
	@Override
	public final short readShort() throws IOException {
		return swapShort(in.readShort());
	}

	@Override
	public final int readInt() throws IOException {
		return swapInteger(in.readInt());
	}

	@Override
	public final long readInt64() throws IOException {
		return swapLong(in.readLong());
	}

	@Override
	public final float readFloat() throws IOException {
		return swapFloat(in.readFloat());
	}

	@Override
	public final double readDouble() throws IOException {
		return swapDouble(in.readDouble());
	}


	

    /**
     * Converts a "short" value between endian systems.
     * @param value value to convert
     * @return the converted value
     */
    public static short swapShort(short value) {
        return (short) ( ( ( ( value >> 0 ) & 0xff ) << 8 ) +
            ( ( ( value >> 8 ) & 0xff ) << 0 ) );
    }

    /**
     * Converts a "int" value between endian systems.
     * @param value value to convert
     * @return the converted value
     */
    public static int swapInteger(int value) {
        return
            ( ( ( value >> 0 ) & 0xff ) << 24 ) +
            ( ( ( value >> 8 ) & 0xff ) << 16 ) +
            ( ( ( value >> 16 ) & 0xff ) << 8 ) +
            ( ( ( value >> 24 ) & 0xff ) << 0 );
    }

    /**
     * Converts a "long" value between endian systems.
     * @param value value to convert
     * @return the converted value
     */
    public static long swapLong(long value) {
        return
            ( ( ( value >> 0 ) & 0xff ) << 56 ) +
            ( ( ( value >> 8 ) & 0xff ) << 48 ) +
            ( ( ( value >> 16 ) & 0xff ) << 40 ) +
            ( ( ( value >> 24 ) & 0xff ) << 32 ) +
            ( ( ( value >> 32 ) & 0xff ) << 24 ) +
            ( ( ( value >> 40 ) & 0xff ) << 16 ) +
            ( ( ( value >> 48 ) & 0xff ) << 8 ) +
            ( ( ( value >> 56 ) & 0xff ) << 0 );
    }

    /**
     * Converts a "float" value between endian systems.
     * @param value value to convert
     * @return the converted value
     */
    public static float swapFloat(float value) {
        return Float.intBitsToFloat( swapInteger( Float.floatToIntBits( value ) ) );
    }

    /**
     * Converts a "double" value between endian systems.
     * @param value value to convert
     * @return the converted value
     */
    public static double swapDouble(double value) {
        return Double.longBitsToDouble( swapLong( Double.doubleToLongBits( value ) ) );
    }

}
