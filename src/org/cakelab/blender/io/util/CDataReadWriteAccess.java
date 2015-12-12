package org.cakelab.blender.io.util;

import java.io.Closeable;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import org.cakelab.blender.io.Encoding;
import org.cakelab.blender.io.FileHeader.PointerSize;


public abstract class CDataReadWriteAccess implements Closeable {

	private int pointerSize;

	
	
	protected CDataReadWriteAccess(int pointerSize) {
		this.pointerSize = pointerSize;
	}


	public static CDataReadWriteAccess create(RandomAccessFile in, Encoding encoding) {
		CDataReadWriteAccess stream = null;
		if (encoding.getByteOrder() == ByteOrder.BIG_ENDIAN) {
			stream = new BigEndianCFileRW(in, encoding.getAddressWidth());
		} else {
			stream = new LittleEndianFileRW(in, encoding.getAddressWidth());
		}
		return stream;
	}

	public static CDataReadWriteAccess create(byte[] data, long baseAddress, Encoding encoding) {
		ByteBuffer buffer = ByteBuffer.wrap(data);
		buffer.order(encoding.getByteOrder());
		return new CBufferReadWrite(buffer, baseAddress, encoding.getAddressWidth());
	}

	
	public final int getPointerSize() {
		return pointerSize;
	}

	
	/* ********************************************
	 *     abstract interface part
	 */

	public boolean readBoolean() throws IOException {
		return readByte() != 0;
	}

	public void writeBoolean(boolean value) throws IOException {
		writeByte(value?1:0);
	}

	public abstract byte readByte() throws IOException;

	public abstract void writeByte(int value) throws IOException;

	public abstract short readShort() throws IOException;

	public abstract void writeShort(short value) throws IOException;

	public abstract int readInt() throws IOException;

	public abstract void writeInt(int value) throws IOException;

	public final long readLong() throws IOException {
		int size = getPointerSize();
		if (size == PointerSize.POINTER_SIZE_32BIT.getSize()) {
			return readInt();
		} else if (size == PointerSize.POINTER_SIZE_64BIT.getSize()) {
			return readInt64();
		} else throw new IOException("undefined pointer size");
	}

	public final void writeLong(long value) throws IOException {
		int size = getPointerSize();
		if (size == PointerSize.POINTER_SIZE_32BIT.getSize()) {
			writeInt((int)value);
		} else if (size == PointerSize.POINTER_SIZE_64BIT.getSize()) {
			writeInt64(value);
		} else throw new IOException("undefined pointer size");
	}

	public abstract long readInt64() throws IOException;

	public abstract void writeInt64(long value) throws IOException;

	public abstract float readFloat() throws IOException;

	public abstract void writeFloat(float value) throws IOException;

	public abstract double readDouble() throws IOException;

	public abstract void writeDouble(double value) throws IOException;

	public final void readFully(byte[] b) throws IOException {
		readFully(b, 0, b.length);
	}

	public final void writeFully(byte[] b) throws IOException {
		writeFully(b, 0, b.length);
	}

	public void readFully(byte[] b, int off, int len) throws IOException {
		len += off;
		for (int i = off; i < len; i++) {
			b[i] = (byte) readByte();
		}
	}

	public void writeFully(byte[] b, int off, int len) throws IOException {
		len += off;
		for (int i = off; i < len; i++) {
			writeByte(b[i]);
		}
	}

	public void readFully(short[] b, int off, int len) throws IOException {
		len += off;
		for (int i = off; i < len; i++) {
			b[i] = readShort();
		}
	}
	
	public void writeFully(short[] b, int off, int len) throws IOException {
		len += off;
		for (int i = off; i < len; i++) {
			writeShort(b[i]);
		}
	}
	
	public void readFully(int[] b, int off, int len) throws IOException {
		len += off;
		for (int i = off; i < len; i++) {
			b[i] = readInt();
		}
	}
	
	public void writeFully(int[] b, int off, int len) throws IOException {
		len += off;
		for (int i = off; i < len; i++) {
			writeInt(b[i]);
		}
	}
	
	public void readFully(long[] b, int off, int len) throws IOException {
		len += off;
		for (int i = off; i < len; i++) {
			b[i] = readLong();
		}
	}
	
	public void writeFully(long[] b, int off, int len) throws IOException {
		len += off;
		for (int i = off; i < len; i++) {
			writeLong(b[i]);
		}
	}
	
	public void readFullyInt64(long[] b, int off, int len) throws IOException {
		len += off;
		for (int i = off; i < len; i++) {
			b[i] = readInt64();
		}
	}
	
	public void writeFullyInt64(long[] b, int off, int len) throws IOException {
		len += off;
		for (int i = off; i < len; i++) {
			writeInt64(b[i]);
		}
	}
	
	public void readFully(float[] b, int off, int len) throws IOException {
		len += off;
		for (int i = off; i < len; i++) {
			b[i] = readFloat();
		}
	}
	
	public void writeFully(float[] b, int off, int len) throws IOException {
		len += off;
		for (int i = off; i < len; i++) {
			writeFloat(b[i]);
		}
	}
	
	public void readFully(double[] b, int off, int len) throws IOException {
		len += off;
		for (int i = off; i < len; i++) {
			b[i] = readDouble();
		}
	}
	
	public void writeFully(double[] b, int off, int len) throws IOException {
		len += off;
		for (int i = off; i < len; i++) {
			writeDouble(b[i]);
		}
	}
	
	
	/**
	 * Inserts padding at a given offset to fit a given alignment during reading or 
	 * writing in streams.
	 * In case of writing, the stream can't just skip past the end 
	 * and needs to actually write. The parameter 'extend' tells this
	 * method whether it is allowed to extend past the end or not.
	 * 
	 * @param alignment Requrested aligment
	 * @param extend Extend past boundary (write mode)
	 * @throws IOException
	 */
	public abstract void padding(int alignment, boolean extend) throws IOException;

	/**
	 * Same as {@link #padding(int, boolean)} with 'extend == false'.
	 * 
	 * @param alignment Requrested aligment
	 * @throws IOException
	 */
	public abstract void padding(int alignment) throws IOException;

	public abstract long skip(long n) throws IOException;

	public abstract int available() throws IOException;

	public abstract void offset(long offset) throws IOException;

	public abstract long offset() throws IOException;


	public abstract ByteOrder getByteOrder();

	

}
