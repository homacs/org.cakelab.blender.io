package org.cakelab.blender.io.util;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class CBufferReadWrite extends CDataReadWriteAccess {

	private ByteBuffer rawData;
	private long address;

	public CBufferReadWrite(ByteBuffer rawData, long address, int pointerSize) {
		super(pointerSize);
		this.rawData = rawData;
		this.address = address;
	}

	@Override
	public short readShort() throws IOException {
		return rawData.getShort();
	}

	
	@Override
	public int readInt() throws IOException {
		return rawData.getInt();
	}

	@Override
	public long readInt64() throws IOException {
		return rawData.getLong();
	}

	@Override
	public float readFloat() throws IOException {
		return rawData.getFloat();
	}

	@Override
	public double readDouble() throws IOException {
		return rawData.getDouble();
	}


	@Override
	public long offset() throws IOException {
		return rawData.position();
	}

	@Override
	public void offset(long offset) throws IOException {
		rawData.position((int) offset);
	}

	@Override
	public long skip(long n) throws IOException {
		rawData.position((int) (rawData.position() + n));
		return n;
	}

	@Override
	public int available() throws IOException {
		return rawData.limit() - rawData.position();
	}

	@Override
	public void padding(int alignment) throws IOException {
		long pos = address + rawData.position();
		long misalignment = pos%alignment;
		if (misalignment > 0) {
			skip(alignment-misalignment);
		}
	}

	@Override
	public void padding(int alignment, boolean extend) throws IOException {
		if (extend)	throw new IllegalArgumentException("cannot extend underlying buffer");
		else padding(alignment);
	}

	@Override
	public void close() throws IOException {
		rawData = null;
	}

	@Override
	public byte readByte() throws IOException {
		return rawData.get();
	}

	@Override
	public void readFully(byte[] b, int off, int len) throws IOException {
		rawData.get(b, off, len);
	}

	@Override
	public void writeByte(int value) throws IOException {
		rawData.put((byte) value);
	}

	@Override
	public void writeShort(short value) throws IOException {
		rawData.putShort(value);
	}

	@Override
	public void writeInt(int value) throws IOException {
		rawData.putInt(value);
	}

	@Override
	public void writeInt64(long value) throws IOException {
		rawData.putLong(value);
	}

	@Override
	public void writeFloat(float value) throws IOException {
		rawData.putFloat(value);
	}

	@Override
	public void writeDouble(double value) throws IOException {
		rawData.putDouble(value);
	}

	@Override
	public ByteOrder getByteOrder() {
		return rawData.order();
	}

	/**
	 * provides access to the native data buffer.
	 * 
	 * This is supposed to be used by internal methods only, which 
	 * know how to handle the data.
	 * 
	 * @return
	 */
	public byte[] getBytes() {
		return rawData.array();
	}

	
}
