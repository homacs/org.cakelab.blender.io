package org.cakelab.blender.io.block;

import java.io.IOException;
import java.nio.ByteOrder;

import org.cakelab.blender.io.util.CBufferReadWrite;
import org.cakelab.blender.io.util.CDataReadWriteAccess;
import org.cakelab.blender.nio.UnsignedLong;

/**
 * An instance of this class provides access to data in a block
 * of a {@link BlenderFile}.
 * 
 * @author homac
 *
 */
public class Block implements Comparable<Long> {
	/** link to prev block in file */
	Block next;
	/** link to next block in file */
	Block prev;
	
	/** the header as read from the file */
	public BlockHeader header;
	
	/** raw data in a byte order aware buffer. */
	public CDataReadWriteAccess data;
	
	
	public Block(BlockHeader header, CDataReadWriteAccess data) {
		this.header = header;
		this.data = data;
	}
	
	
	Block() {}


	@Override
	public int compareTo(Long address) {
		return UnsignedLong.compare(header.address, address);
	}

	private void offset(long address) throws IOException {
		data.offset(address - header.address);
	}


	public void close() throws IOException {
		data.close();
	}

	public boolean readBoolean(long address) throws IOException {
		offset(address);
		return data.readBoolean();
	}

	public void writeBoolean(long address, boolean value) throws IOException {
		offset(address);
		data.writeBoolean(value);
	}

	public byte readByte(long address) throws IOException {
		offset(address);
		return data.readByte();
	}

	public void writeByte(long address, byte value) throws IOException {
		offset(address);
		data.writeByte(value);
	}

	public short readShort(long address) throws IOException {
		offset(address);
		return data.readShort();
	}

	public void writeShort(long address, short value) throws IOException {
		offset(address);
		data.writeShort(value);
	}

	public int readInt(long address) throws IOException {
		offset(address);
		return data.readInt();
	}

	public void writeInt(long address, int value) throws IOException {
		offset(address);
		data.writeInt(value);
	}

	public long readLong(long address) throws IOException {
		offset(address);
		return data.readLong();
	}

	public void writeLong(long address, long value) throws IOException {
		offset(address);
		data.writeLong(value);
	}

	public long readInt64(long address) throws IOException {
		offset(address);
		return data.readInt64();
	}

	public void writeInt64(long address, long value) throws IOException {
		offset(address);
		data.writeInt64(value);
	}

	public float readFloat(long address) throws IOException {
		offset(address);
		return data.readFloat();
	}

	public void writeFloat(long address, float value) throws IOException {
		offset(address);
		data.writeFloat(value);
	}

	public double readDouble(long address) throws IOException {
		offset(address);
		return data.readDouble();
	}

	public void writeDouble(long address, double value) throws IOException {
		offset(address);
		data.writeDouble(value);
	}

	public void readFully(long address, byte[] b) throws IOException {
		offset(address);
		data.readFully(b);
	}

	public void writeFully(long address, byte[] b) throws IOException {
		offset(address);
		data.writeFully(b);
	}

	public void readFully(long address, byte[] b, int off, int len) throws IOException {
		offset(address);
		data.readFully(b, off, len);
	}

	public void writeFully(long address, byte[] b, int off, int len) throws IOException {
		offset(address);
		data.writeFully(b, off, len);
	}

	public boolean contains(long address) {
		return address >= this.header.address && address < this.header.address + this.header.size;
	}

	public void readFully(long address, short[] b, int off, int len) throws IOException {
		offset(address);
		data.readFully(b, off, len);
	}

	public void writeFully(long address, short[] b, int off, int len) throws IOException {
		offset(address);
		data.writeFully(b, off, len);
	}

	public void readFully(long address, int[] b, int off, int len) throws IOException {
		offset(address);
		data.readFully(b, off, len);
	}

	public void writeFully(long address, int[] b, int off, int len) throws IOException {
		offset(address);
		data.writeFully(b, off, len);
	}

	public void readFully(long address, long[] b, int off, int len) throws IOException {
		offset(address);
		data.readFully(b, off, len);
	}

	public void writeFully(long address, long[] b, int off, int len) throws IOException {
		offset(address);
		data.writeFully(b, off, len);
	}

	public void readFullyInt64(long address, long[] b, int off, int len) throws IOException {
		offset(address);
		data.readFullyInt64(b, off, len);
	}

	public void writeFullyInt64(long address, long[] b, int off, int len) throws IOException {
		offset(address);
		data.writeFullyInt64(b, off, len);
	}

	public void readFully(long address, float[] b, int off, int len) throws IOException {
		offset(address);
		data.readFully(b, off, len);
	}

	public void writeFully(long address, float[] b, int off, int len) throws IOException {
		offset(address);
		data.writeFully(b, off, len);
	}

	public void readFully(long address, double[] b, int off, int len) throws IOException {
		offset(address);
		data.readFully(b, off, len);
	}

	public void writeFully(long address, double[] b, int off, int len) throws IOException {
		offset(address);
		data.writeFully(b, off, len);
	}

	public ByteOrder getByteOrder() {
		return data.getByteOrder();
	}

	public void flush(CDataReadWriteAccess io) throws IOException {
		if (data instanceof CBufferReadWrite) {
			// in-memory buffer
			header.write(io);
			io.writeFully(((CBufferReadWrite)data).getBytes());
		} else if (io == data) {
			// data io is direct file access (data on disk is up-to-date)
			// Update the header only (just in case)
			header.write(io);
			io.skip(header.size);
		} else if (io.getByteOrder().equals(data.getByteOrder()) && io.getPointerSize() == data.getPointerSize()) {
			// copy to another file
			header.write(io);
			byte[] buf = new byte[header.size];
			data.readFully(buf);
			io.writeFully(buf);
		} else {
			throw new IOException("error: attempt to write a block with different encoding to another file");
		}

	}

}
