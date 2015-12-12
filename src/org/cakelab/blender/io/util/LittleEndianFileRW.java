package org.cakelab.blender.io.util;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteOrder;

public class LittleEndianFileRW extends CDataFileRWAccess {


	public LittleEndianFileRW(RandomAccessFile in, int pointerSize) {
		super(in, pointerSize);
	}
	
	@Override
	public final short readShort() throws IOException {
		return swapShort(io.readShort());
	}

	@Override
	public final void writeShort(short value) throws IOException {
		io.writeShort(swapShort(value));
	}

	@Override
	public final int readInt() throws IOException {
		return swapInteger(io.readInt());
	}

	@Override
	public final void writeInt(int value) throws IOException {
		io.writeInt(swapInteger(value));
	}

	@Override
	public final long readInt64() throws IOException {
		return swapLong(io.readLong());
	}

	@Override
	public final void writeInt64(long value) throws IOException {
		io.writeLong(swapLong(value));
	}

	@Override
	public final float readFloat() throws IOException {
		return swapFloat(io.readFloat());
	}

	@Override
	public final void writeFloat(float value) throws IOException {
		io.writeFloat(swapFloat(value));
	}

	@Override
	public final double readDouble() throws IOException {
		return swapDouble(io.readDouble());
	}

	@Override
	public final void writeDouble(double value) throws IOException {
		io.writeDouble(swapDouble(value));
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

	@Override
	public ByteOrder getByteOrder() {
		return ByteOrder.LITTLE_ENDIAN;
	}

}
