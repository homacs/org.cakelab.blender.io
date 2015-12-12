package org.cakelab.blender.io.util;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.Arrays;

public class CStringUtils {
	public static final Charset ASCII = Charset.forName("ASCII");
	public static final Charset UTF8 = Charset.forName("UTF-8");

	public static String toString(byte[] ascii, int start, int len, boolean removeControlCodes) {
		String str = new String(ascii, start, len, ASCII);
		if (removeControlCodes) {
			char[] chars = str.toCharArray();
			for (int i = 0; i < chars.length; i++) {
				
				if (Character.isISOControl(chars[i])) {
					chars[i] = '.';
				}
			}
			str = new String (chars);
		}
		return str;
	}
	public static String toNullTerminatedString(byte[] ascii, int start, int len, boolean removeControlCodes) {
		int l = strlen(ascii, start, len);
		return toString(ascii, start, l, removeControlCodes);
	}
	public static String toString(byte[] str) {
		return toString(str, 0, str.length, false);
	}

	public static String toNullTerminatedString(byte[] str) {
		return toString(str, 0, strlen(str), false);
	}

	public static int strlen(byte[] str, int start, int maxlen) {
		int len;
		for (len = start; len < maxlen && str[len] != 0; len++);
		return len;
	}

	public static int strlen(byte[] str) {
		return strlen(str, 0, str.length);
	}

	public static byte[] valueOf(String str) {
		byte[] result = null;
		ByteBuffer encoded = ASCII.encode(str);
		result = new byte[encoded.limit()];
		encoded.get(result);
		return result;
	}

	public static String readNullTerminatedString(CDataReadWriteAccess in, boolean removeControlCodes) throws IOException {
		int len = 0;
		int capacity = 1024;
		byte[] buffer = new byte[capacity];
		byte b = in.readByte();
		while (b != 0) {
			buffer[len] = b;
			len++;
			if (len>=capacity) {
				capacity <<= 1;
				buffer = Arrays.copyOf(buffer, capacity);
			}
			b = in.readByte();
		}
		return toString(buffer, 0, len, removeControlCodes);
	}

	public static String toString(byte[] str, boolean removeControlCodes) {
		return toString(str, 0, str.length, removeControlCodes);
	}

	public static String toNullTerminatedString(byte[] str, boolean removeControlCodes) {
		return toString(str, 0, strlen(str), removeControlCodes);
	}

	public static String readNullTerminatedString(CDataReadWriteAccess in) throws IOException {
		return readNullTerminatedString(in, false);
	}
	
	public static void writeNullTerminatedString(String string, Charset charset,
			CDataReadWriteAccess io, boolean b) throws IOException {
		io.writeFully(string.getBytes(charset));
		io.writeByte(0);
	}
	public static String toNullTerminatedString(byte[] buf, Charset charset) {
		int len = strlen(buf);
		return new String(buf, 0, len, charset);
	}

}
