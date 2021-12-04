package org.cakelab.blender.generator;

import java.util.HashSet;
import java.util.Set;

public class Mangeling {
	
	
	private static final String MANGLE_PREFIX = "_";
	
	/** List of Java keywords which do not exist in C++ */
	private static final Set<String> conflictingKeywords = new HashSet<String>();

	static {
		initKeywordTable();
	}
	
	
	public static String mangle(String name) {
		if (needsMangling(name)) {
			return MANGLE_PREFIX + name;
		}
		return name;
	}
	
	private static boolean needsMangling(String name) {
		return conflictingKeywords.contains(name);
	}


	private static void initKeywordTable() {
		final String[] keywords = new String[] {
				"final",
				"transient",
				"finally",
				"boolean",
				"Boolean",
				"byte",
				"import",
				"package",
				"interface",
				"annotation",
		};
		
		for (String keyword : keywords) {
			conflictingKeywords.add(keyword);
		}
	}

}
