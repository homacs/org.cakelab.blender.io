package org.cakelab.blender.file.block;

import java.io.IOException;

import org.cakelab.blender.file.util.CDataReadAccess;


public class Block implements Comparable<Long> {
	/** the header as read from the file */
	public BlockHeader header;
	
	/** raw data in a byte order aware buffer. */
	public CDataReadAccess data;
	
	public Block(BlockHeader header, CDataReadAccess data) {
		this.header = header;
		this.data = data;
	}
	
	@Override
	public int compareTo(Long address) {
		long diff = (header.getAddress() - address);
		if (diff > 0) {
			return +1;
		} else if (diff < 0)  {
			return -1;
		} else return 0;
	}

	private void address(long address) throws IOException {
		data.offset(address - header.address);
	}


	public void close() throws IOException {
		data.close();
	}

	public boolean readBoolean(long address) throws IOException {
		address(address);
		return data.readBoolean();
	}

	public byte readByte(long address) throws IOException {
		address(address);
		return data.readByte();
	}

	public short readShort(long address) throws IOException {
		address(address);
		return data.readShort();
	}

	public int readInt(long address) throws IOException {
		address(address);
		return data.readInt();
	}

	public long readLong(long address) throws IOException {
		address(address);
		return data.readLong();
	}

	public long readInt64(long address) throws IOException {
		address(address);
		return data.readInt64();
	}

	public float readFloat(long address) throws IOException {
		address(address);
		return data.readFloat();
	}

	public double readDouble(long address) throws IOException {
		address(address);
		return data.readDouble();
	}

	public void readFully(long address, byte[] b) throws IOException {
		address(address);
		data.readFully(b);
	}

	public void readFully(long address, byte[] b, int off, int len) throws IOException {
		address(address);
		data.readFully(b, off, len);
	}

	public boolean contains(long address) {
		return address >= this.header.address && address < this.header.address + this.header.size;
	}

	public void readFully(long address, short[] b, int off, int len) throws IOException {
		address(address);
		data.readFully(b, off, len);
	}

	public void readFully(long address, int[] b, int off, int len) throws IOException {
		address(address);
		data.readFully(b, off, len);
	}

	public void readFully(long address, long[] b, int off, int len) throws IOException {
		address(address);
		data.readFully(b, off, len);
	}

	public void readFullyInt64(long address, long[] b, int off, int len) throws IOException {
		address(address);
		data.readFullyInt64(b, off, len);
	}

	public void readFully(long address, float[] b, int off, int len) throws IOException {
		address(address);
		data.readFully(b, off, len);
	}

	public void readFully(long address, double[] b, int off, int len) throws IOException {
		address(address);
		data.readFully(b, off, len);
	}
	
}
