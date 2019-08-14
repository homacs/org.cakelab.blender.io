package org.cakelab.blender.io;

import java.io.IOException;
import java.nio.ByteOrder;

import org.cakelab.blender.io.util.CDataReadWriteAccess;
import org.cakelab.blender.io.util.CStringUtils;

/**
 * The first 12 bytes of every blend-file is the file-header. 
 * The file-header has information on Blender (version-number) 
 * and the PC the blend-file was saved on (pointer-size and endianness). 
 * This is required as all data inside the blend-file is ordered 
 * in that way, because no translation or transformation is done 
 * during saving. 
 */
public class FileHeader {

	public static enum Endianess {
		LITTLE_ENDIAN('v'),
		BIG_ENDIAN('V');

		char code;
		private Endianess(char code) {
			this.code = code;
		}
		public static Endianess decode(int code) {
			char c = (char)code;
			if (c == LITTLE_ENDIAN.code) return LITTLE_ENDIAN;
			else if (c == BIG_ENDIAN.code) return BIG_ENDIAN;
			else throw new IllegalArgumentException("undefined endianess code '" + code + "'");
		}
		public static Endianess from(ByteOrder byteOrder) {
			if (byteOrder == ByteOrder.BIG_ENDIAN) {
				return BIG_ENDIAN;
			} else {
				return LITTLE_ENDIAN;
			}
		}
	}
	public static enum PointerSize {
		
		POINTER_SIZE_32BIT('_', 4),
		POINTER_SIZE_64BIT('-', 8);
		
		final char code;
		private final int size;
		
		private PointerSize(char code, int size) {
			this.code = code;
			this.size = size;
		}
		public static PointerSize decode(int code) {
			char c = (char)code;
			if (c == POINTER_SIZE_32BIT.code) return POINTER_SIZE_32BIT;
			else if (c == POINTER_SIZE_64BIT.code) return POINTER_SIZE_64BIT;
			else throw new IllegalArgumentException("undefined pointer size code '"+ code +"'");
		}
		public int getSize() {
			return size;
		}
		public static PointerSize from(int pointerSize) {
			if (pointerSize == 8) {
				return POINTER_SIZE_64BIT;
			} else {
				return POINTER_SIZE_32BIT;
			}
		}
	}
	public static class Version {
		int major;
		int minor;
		/** code == major*100 + minor */
		int code;

		public Version(int v) {
			major = v/100;
			minor = v%100;
			code = v;
		}
		
		/**
		 * Create a version instance from a version string.
		 * Expected format: '%1d.%2d' (printf format string)
		 * @param verstr
		 * @throws NumberFormatException on any format violation
		 */
		public Version(String verstr) throws NumberFormatException {
			final String formatExceptionMsg = "Unsupported version string format in '" + verstr + "'";
			
			String[] parts = verstr.split("\\.");
			if (parts.length < 2) throw new NumberFormatException(formatExceptionMsg);
			
			try {
				major = Integer.valueOf(parts[0]);
				minor = Integer.valueOf(parts[1]);
			} catch (NumberFormatException e) {
				throw new NumberFormatException(formatExceptionMsg);
			}
			
			if (major >= 10 || minor >= 100) {
				throw new NumberFormatException(formatExceptionMsg);
			}
			code = major*100 + minor;
		}

		public String toString() {
			return "" + major + '.' + (minor < 10 ? "0"+minor : minor);
		}

		public static Version read(CDataReadWriteAccess in) throws IOException {
			byte[] str = new byte[3];
			in.readFully(str);
			int v = Integer.valueOf(CStringUtils.toString(str));
			return new Version(v);
		}

		public void write(CDataReadWriteAccess io) throws IOException {
			io.writeFully(Integer.toString(code).getBytes(CStringUtils.ASCII));
		}
		
		/**
		 * @return returns version code := major*100 + minor
		 */
		public int getCode() {
			return code;
		}

		public int getMajor() {
			return major;
		}

		public int getMinor() {
			return minor;
		}

	}
	
	/** File identifier (always "BLENDER" (ASCII)).*/
	public static final String BLENDER_MAGIC = "BLENDER";
	/** Size of a pointer; all pointers in the file are stored in this 
	 * format. '_' means 4 bytes or 32 bit and '-' means 
	 * 8 bytes or 64 bits. */
	PointerSize pointerSize;
	/** Type of byte ordering used; 'v' means little endian and 
	 * 'V' means big endian. */
	Endianess endianess;
	/** Version of Blender the file was created in; "248" means version 2.48 */
	Version version;
	
	public void read(CDataReadWriteAccess in) throws IOException {
		byte[] magic = new byte[BLENDER_MAGIC.length()];
		in.readFully(magic);
		
		if (!CStringUtils.toString(magic).equals(BLENDER_MAGIC)) {
			throw new IOException("not a blender file");
		}
		pointerSize = PointerSize.decode(in.readByte());
		endianess = Endianess.decode(in.readByte());
		version = Version.read(in);
	}

	public void write(CDataReadWriteAccess io) throws IOException {
		io.writeFully(BLENDER_MAGIC.getBytes(CStringUtils.ASCII));
		
		io.writeByte(pointerSize.code);
		io.writeByte(endianess.code);
		version.write(io);
	}
	

	public String toString() {
		return BLENDER_MAGIC + pointerSize.code + endianess.code + version.code;
	}

	public ByteOrder getByteOrder() {
		return endianess.equals(Endianess.BIG_ENDIAN) ? ByteOrder.BIG_ENDIAN : ByteOrder.LITTLE_ENDIAN;
	}

	public int getPointerSize() {
		return pointerSize.getSize();
	}
	/** Blender version, this file was created in. */
	public Version getVersion() {
		return version;
	}
}
