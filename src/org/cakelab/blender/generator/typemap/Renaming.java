package org.cakelab.blender.generator.typemap;

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
public class Renaming {

	public static String mapStruct2Class(String name) {
		if(name.equals("Object")) return "BlenderObject";
		else return name;
	}

}
