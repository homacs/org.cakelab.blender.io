package org.cakelab.blender.file.util;

import java.io.IOException;
import java.io.RandomAccessFile;

public class BigEndianCFileDataInputStream extends CDataFileInputStream {


	public BigEndianCFileDataInputStream(RandomAccessFile in, int pointerSize) {
		super(in, pointerSize);
	}

	@Override
	public double readDouble() throws IOException {
		return in.readDouble();
	}

	@Override
	public float readFloat() throws IOException {
		return in.readFloat();
	}

	@Override
	public int readInt() throws IOException {
		return in.readInt();
	}

	@Override
	public long readInt64() throws IOException {
		return in.readLong();
	}

	@Override
	public short readShort() throws IOException {
		return in.readShort();
	}

	@Override
	public long offset() throws IOException {
		return in.getFilePointer();
	}

}
