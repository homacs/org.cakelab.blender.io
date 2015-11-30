package org.cakelab.blender.generator.type;

import org.cakelab.blender.model.DNAArray;
import org.cakelab.blender.model.DNAPointer;




public class JavaType {

	public enum JTypeType {
		TYPE_FUNCTION_POINTER,
		TYPE_ARRAY, TYPE_SCALAR, TYPE_OBJECT;
	}
	
	
	JTypeType typetype;
	String name;
	JavaType referencedType;

	public JavaType(CType ctype) {
		switch(ctype.typetype) {
		case TYPE_ARRAY:
			mapArray(ctype);
			break;
		case TYPE_FUNCTION_POINTER:
			mapFunctionPointer(ctype);
			break;
		case TYPE_POINTER:
			mapPointer(ctype);
			break;
		case TYPE_SCALAR:
			mapScalar(ctype);
			break;
		case TYPE_STRUCT:
			mapStruct(ctype);
			break;
		case TYPE_VOID:
			mapVoid(ctype);
			break;
		default:
			break;
		}
	}



	private void mapVoid(CType ctype) {
		this.typetype = JTypeType.TYPE_OBJECT;
		name = Object.class.getSimpleName();
	}



	private void mapStruct(CType ctype) {
		this.typetype = JTypeType.TYPE_OBJECT;
		if (ctype.size == 0) {
			name = Object.class.getSimpleName();
		} else {
			name = Renaming.mapStruct2Class(ctype.name);
		}
	}



	private void mapScalar(CType ctype) {
		this.typetype = JTypeType.TYPE_SCALAR;
		
		if (ctype.name.equals("char")) {
			name = "byte";
		} else if (ctype.name.equals("short") || ctype.name.equals("ushort")) {
			name = "short";
		} else if (ctype.name.equals("int") || ctype.name.equals("unsigned int")) {
			name = "int";
		} else if (ctype.name.equals("long") 
				|| ctype.name.equals("ulong")
				|| ctype.name.equals("int64_t")
				|| ctype.name.equals("uint64_t")
				) {
			name = "long";
		} else if (ctype.name.equals("float")) {
			name = "float";
		} else if (ctype.name.equals("double")) {
			name = "double";
		} else {
			throw new Error("unexpected type name " + ctype.name);
		}
	}



	private void mapPointer(CType ctype) {
		this.typetype = JTypeType.TYPE_OBJECT;
		this.name = DNAPointer.class.getSimpleName();
		this.referencedType = new JavaType(ctype.referencedType);
	}



	private void mapFunctionPointer(CType ctype) {
		this.typetype = JTypeType.TYPE_FUNCTION_POINTER;
		this.name = long.class.getSimpleName();
	}



	private void mapArray(CType ctype) {
		this.typetype = JTypeType.TYPE_OBJECT;
		this.name = DNAArray.class.getSimpleName();
		this.referencedType = new JavaType(ctype.referencedType);
	}



	public String getName() {
		if (typetype == JTypeType.TYPE_ARRAY) {
			String basicType = getArrayBasicType().name;
			String suffix = getArrayTypeSuffix();
			return basicType + suffix;
		} else {
			return name;
		}
	}



	private String getArrayTypeSuffix() {
		String suffix = "[]";
		JavaType inner = referencedType;
		while (inner.typetype == JTypeType.TYPE_ARRAY) {
			inner = inner.referencedType;
			suffix += "[]";
		}
		return suffix;
	}



	JavaType getArrayBasicType() {
		JavaType inner = referencedType;
		while (inner.typetype == JTypeType.TYPE_ARRAY) {
			inner = inner.referencedType;
		}
		
		return inner;
	}



	public JTypeType getTypetype() {
		return typetype;
	}



	public JavaType getReferencedType() {
		return referencedType;
	}




}
