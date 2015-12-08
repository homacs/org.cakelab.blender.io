package org.cakelab.blender.generator.code;

public class HtmlEncoder {
	public static String encode(String s) {
		return s.replace("<", "&lt;").replace(">", "&gt;");
	}
}
