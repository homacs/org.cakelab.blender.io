package org.cakelab.blender.io.util;

import java.io.IOException;
import java.io.RandomAccessFile;

public abstract class CDataFileRWAccess extends CDataReadWriteAccess {

	protected RandomAccessFile in;


	protected CDataFileRWAccess(RandomAccessFile in, int pointerSize) {
		super(pointerSize);
		this.in = in;
	}

	@Override
	public final long skip(long n) throws IOException {
        long pos;
        long len;
        long newpos;

        if (n <= 0) {
            return 0;
        }
        pos = in.getFilePointer();
        len = in.length();
        newpos = pos + n;
        if (newpos > len) {
            newpos = len;
        }
        in.seek(newpos);

        /* return the actual number of bytes skipped */
        return (newpos - pos);
	}

	@Override
	public final int available() throws IOException {
		return (int) (in.length() - in.getFilePointer());
	}

	@Override
	public final void readFully(byte[] b, int off, int len)
			throws IOException {
		in.readFully(b, off, len);
	}

	@Override
	public final void writeFully(byte[] b, int off, int len)
			throws IOException {
		in.write(b, off, len);
	}

	@Override
	public final boolean readBoolean() throws IOException {
		return in.readBoolean();
	}

	@Override
	public final void writeBoolean(boolean value) throws IOException {
		in.writeBoolean(value);
	}

	@Override
	public final byte readByte() throws IOException {
		return in.readByte();
	}

	@Override
	public final void writeByte(int value) throws IOException {
		in.writeByte(value);
	}

	@Override
	public final void offset(long offset) throws IOException {
		in.seek(offset);
	}

	@Override
	public long offset() throws IOException {
		return in.getFilePointer();
	}
	
	@Override
	public void padding(int alignment) throws IOException {
		long pos = offset();
		long misalignment = pos%alignment;
		if (misalignment > 0) {
			skip(alignment-misalignment);
		}
	}


	public void close() throws IOException {
		in.close();
	}
}
