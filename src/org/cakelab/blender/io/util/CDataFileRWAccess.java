package org.cakelab.blender.io.util;

import java.io.IOException;
import java.io.RandomAccessFile;

public abstract class CDataFileRWAccess extends CDataReadWriteAccess {

	protected RandomAccessFile io;


	protected CDataFileRWAccess(RandomAccessFile in, int pointerSize) {
		super(pointerSize);
		this.io = in;
	}

	@Override
	public final long skip(long n) throws IOException {
        long pos;
        long len;
        long newpos;

        if (n <= 0) {
            return 0;
        }
        pos = io.getFilePointer();
        len = io.length();
        newpos = pos + n;
        if (newpos > len) throw new IOException("Skipping beyond file boundary.");
        io.seek(newpos);

        /* return the actual number of bytes skipped */
        return (newpos - pos);
	}

	@Override
	public final int available() throws IOException {
		return (int) (io.length() - io.getFilePointer());
	}

	@Override
	public final void readFully(byte[] b, int off, int len)
			throws IOException {
		io.readFully(b, off, len);
	}

	@Override
	public final void writeFully(byte[] b, int off, int len)
			throws IOException {
		io.write(b, off, len);
	}

	@Override
	public final boolean readBoolean() throws IOException {
		return io.readBoolean();
	}

	@Override
	public final void writeBoolean(boolean value) throws IOException {
		io.writeBoolean(value);
	}

	@Override
	public final byte readByte() throws IOException {
		return io.readByte();
	}

	@Override
	public final void writeByte(int value) throws IOException {
		io.writeByte(value);
	}

	@Override
	public final void offset(long offset) throws IOException {
		io.seek(offset);
	}

	@Override
	public long offset() throws IOException {
		return io.getFilePointer();
	}
	
	@Override
	public void padding(int alignment) throws IOException {
		padding(alignment, false);
	}

	@Override
	public void padding(int alignment, boolean extend) throws IOException {
		long pos = offset();
		long misalignment = pos%alignment;
		if (misalignment > 0) {
			long len = io.length();
			long correction = alignment-misalignment;
			if (pos + correction <= len) {
				skip(correction);
			} else if (extend) {
				offset(pos + (correction-1));
				writeByte(0);
			} else {
				throw new IOException("padding beyond file boundary without permission.");
			}
		}
	}

	public void close() throws IOException {
		io.close();
	}
}
