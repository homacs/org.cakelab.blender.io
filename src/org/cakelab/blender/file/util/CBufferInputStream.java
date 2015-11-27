package org.cakelab.blender.file.util;

import java.io.IOException;
import java.nio.ByteBuffer;

public class CBufferInputStream extends CDataReadAccess {

	private ByteBuffer rawData;
	private long address;

	public CBufferInputStream(ByteBuffer rawData, long address, int pointerSize) {
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
	public void close() throws IOException {
		rawData = null;
	}

	@Override
	public byte readByte() throws IOException {
		return rawData.get();
	}

}
