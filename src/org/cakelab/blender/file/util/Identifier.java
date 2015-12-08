package org.cakelab.blender.file.util;

import java.io.IOException;
import java.util.Arrays;

import org.cakelab.blender.file.util.ASCII;

/**
 * This class implements an abstraction layer to 4 byte
 * character identifiers used for block codes and markers
 * in StructDNA.
 * 
 * It provides convenient methods to read a code from file
 * and compare it to some code you expect.
 * 
 * @author homac
 *
 */
public class Identifier {
	byte[] code = new byte[4];
	
	public Identifier() {}
	
	/**
	 * This constructor creates an identifier using the 
	 * characters in the given string interpreted as ASCII.
	 * 
	 * @param strCode
	 */
	public Identifier(String strCode) {
		code = ASCII.valueOf(strCode);
	}

	public Identifier(byte[] code) {
		this.code = code;
	}

	/**
	 * This method reads the code from the give input stream.
	 * 
	 * @param in
	 * @throws IOException
	 */
	public void read(CDataReadWriteAccess in) throws IOException {
		in.readFully(code);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + Arrays.hashCode(code);
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Identifier other = (Identifier) obj;
		if (!Arrays.equals(code, other.code))
			return false;
		return true;
	}

	public String toString() {
		return ASCII.toString(code, true);
	}

	/**
	 * This reads a code from the input stream and compares it 
	 * to the 'expected' value. A mismatch is considered as an 
	 * I/O error and treated with an IOException.
	 * 
	 * @param in
	 * @param expected
	 * @throws IOException
	 */
	public void consume(CDataReadWriteAccess in, Identifier expected) throws IOException {
		Identifier ident = new Identifier();
		ident.read(in);
		if (!ident.equals(expected)) throw new IOException("input did not match expected identifier '" + expected + "'");
	}

	public String getDataString() {
		return Arrays.toString(code);
	}
}
