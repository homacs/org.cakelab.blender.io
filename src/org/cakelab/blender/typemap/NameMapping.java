package org.cakelab.blender.typemap;

import java.util.HashSet;
import java.util.Set;

/**
 * This class implements mapping of blender struct names
 * different names in case of collisions.
 * The only case currently considered is the struct name 
 * "Object" which is mapped to "BlenderObject".
 * 
 * Please note, that this is not the place to add mappings
 * like "char*" to "String". It considers renaming of structs
 * only.
 * 
 * @author homac
 *
 */
public class NameMapping {
	
	
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

	public static String mapStruct2Class(String name) {
		if(name.equals("Object")) return "BlenderObject";
		else return name;
	}
	
	public static String mapClass2Struct(String name) {
		if(name.equals("BlenderObject")) return "Object";
		else return name;
	}

	public static String getFieldDescriptorName(String name) {
		return "__DNA__FIELD__" + name;
	}
	
	public static String toGetterMethodName(String memberName) {
		return "get" + toCamelCase(memberName);
	}

	public static String toSetterMethodName(String memberName) {
		return "set" + toCamelCase(memberName);
	}

	public static String toCamelCase(String name) {
		return "" + Character.toTitleCase(name.charAt(0)) + name.substring(1);
	}

	public static String toFirstLowerCase(String name) {
		return "" + Character.toLowerCase(name.charAt(0)) + name.substring(1);
	}



}
