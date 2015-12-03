package org.cakelab.blender.file.util;

import java.io.Closeable;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import org.cakelab.blender.file.FileHeader.PointerSize;


public abstract class CDataReadAccess implements Closeable {

	private int pointerSize;

	
	
	protected CDataReadAccess(int pointerSize) {
		this.pointerSize = pointerSize;
	}


	public static CDataReadAccess create(RandomAccessFile in, ByteOrder byteOrder, int pointerSize) {
		CDataReadAccess stream = null;
		if (byteOrder == ByteOrder.BIG_ENDIAN) {
			stream = new BigEndianCFileDataInputStream(in, pointerSize);
		} else {
			stream = new LittleEndianDataInputStream(in, pointerSize);
		}
		return stream;
	}

	public static CDataReadAccess create(ByteBuffer rawData, long baseAddress, int pointerSize) {
		return new CBufferInputStream(rawData, baseAddress, pointerSize);
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

	public abstract byte readByte() throws IOException;

	public abstract  short readShort() throws IOException;

	public abstract  int readInt() throws IOException;


	public final long readLong() throws IOException {
		int size = getPointerSize();
		if (size == PointerSize.POINTER_SIZE_32BIT.getSize()) {
			return readInt();
		} else if (size == PointerSize.POINTER_SIZE_64BIT.getSize()) {
			return readInt64();
		} else throw new IOException("undefined pointer size");
	}

	public abstract long readInt64() throws IOException;

	public abstract  float readFloat() throws IOException;

	public abstract  double readDouble() throws IOException;

	public final void readFully(byte[] b) throws IOException {
		readFully(b, 0, b.length);
	}

	public void readFully(byte[] b, int off, int len) throws IOException {
		len += off;
		for (int i = off; i < len; i++) {
			b[i] = (byte) readByte();
		}
	}

	public void readFully(short[] b, int off, int len) throws IOException {
		len += off;
		for (int i = off; i < len; i++) {
			b[i] = readShort();
		}
	}
	
	public void readFully(int[] b, int off, int len) throws IOException {
		len += off;
		for (int i = off; i < len; i++) {
			b[i] = readInt();
		}
	}
	
	public void readFully(long[] b, int off, int len) throws IOException {
		len += off;
		for (int i = off; i < len; i++) {
			b[i] = readLong();
		}
	}
	
	public void readFullyInt64(long[] b, int off, int len) throws IOException {
		len += off;
		for (int i = off; i < len; i++) {
			b[i] = readInt64();
		}
	}
	
	public void readFully(float[] b, int off, int len) throws IOException {
		len += off;
		for (int i = off; i < len; i++) {
			b[i] = readFloat();
		}
	}
	
	public void readFully(double[] b, int off, int len) throws IOException {
		len += off;
		for (int i = off; i < len; i++) {
			b[i] = readDouble();
		}
	}
	
	
	public abstract void padding(int alignment) throws IOException;

	public abstract long skip(long n) throws IOException;

	public abstract int available() throws IOException;

	public abstract void offset(long offset) throws IOException;

	public abstract long offset() throws IOException;

	

}
