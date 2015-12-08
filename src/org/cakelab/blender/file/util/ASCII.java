package org.cakelab.blender.file.util;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.Arrays;

public class ASCII {
	private static final Charset CHARSET = Charset.forName("ASCII");

	public static String toString(byte[] ascii, int start, int len, boolean removeControlCodes) {
		String str = new String(ascii, start, len, CHARSET);
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

	public static String toString(byte[] str) {
		return toString(str, 0, str.length, false);
	}

	public static byte[] valueOf(String str) {
		byte[] result = null;
		ByteBuffer encoded = CHARSET.encode(str);
		result = new byte[encoded.limit()];
		encoded.get(result);
		return result;
	}

	public static String readZeroTerminatedString(CDataReadWriteAccess in, boolean removeControlCodes) throws IOException {
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

	public static String readZeroTerminatedString(CDataReadWriteAccess in) throws IOException {
		return readZeroTerminatedString(in, false);
	}

}
