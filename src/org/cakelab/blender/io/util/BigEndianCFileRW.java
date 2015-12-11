package org.cakelab.blender.io.util;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteOrder;

public class BigEndianCFileRW extends CDataFileRWAccess {


	private RandomAccessFile in;

	public BigEndianCFileRW(RandomAccessFile in, int pointerSize) {
		super(in, pointerSize);
		this.in = in;
	}

	@Override
	public short readShort() throws IOException {
		return in.readShort();
	}

	@Override
	public final void writeShort(short v) throws IOException {
		in.writeShort(v);
	}

	@Override
	public int readInt() throws IOException {
		return in.readInt();
	}

	@Override
	public final void writeInt(int v) throws IOException {
		in.writeInt(v);
	}

	@Override
	public long readInt64() throws IOException {
		return in.readLong();
	}

	@Override
	public void writeInt64(long value) throws IOException {
		in.writeLong(value);
	}

	@Override
	public float readFloat() throws IOException {
		return in.readFloat();
	}

	@Override
	public final void writeFloat(float v) throws IOException {
		in.writeFloat(v);
	}

	@Override
	public double readDouble() throws IOException {
		return in.readDouble();
	}

	@Override
	public final void writeDouble(double v) throws IOException {
		in.writeDouble(v);
	}

	@Override
	public long offset() throws IOException {
		return in.getFilePointer();
	}

	@Override
	public ByteOrder getByteOrder() {
		return ByteOrder.BIG_ENDIAN;
	}

	
}
