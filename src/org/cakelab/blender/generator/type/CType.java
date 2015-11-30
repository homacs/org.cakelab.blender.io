package org.cakelab.blender.generator.type;

import org.cakelab.blender.file.dna.BlendField;


public class CType {

	public enum CTypeType {
		TYPE_POINTER,
		TYPE_FUNCTION_POINTER,
		TYPE_ARRAY,
		TYPE_SCALAR,
		TYPE_STRUCT,
		TYPE_VOID
	}
	
	int arrayLength;
	/** This is the type of component in case of an array or the 
	 * target type in case of a pointer. */
	CType referencedType;
	CTypeType typetype;
	String name;
	String signature;
	/** This is the size as read from StructDNA.
	 * If the type is a pointer, this size contains the size of the 
	 * basic type and not the pointer.
	 * If the type is unknown, than it's size is unknown as well 
	 * and the size equals 0. */
	int size;

	public CType(String typename, short size, String fieldSignatureName) {
		this.name = typename;
		this.size = size;
		this.signature = typename + " " + fieldSignatureName;
		if (fieldSignatureName.contains("(")) {
			getFunctionPointerType(typename, size, fieldSignatureName);
		} else if (fieldSignatureName.endsWith("]")) {
			getArrayType(typename, size, fieldSignatureName);
		} else if (fieldSignatureName.startsWith("*")) {
			getPointerType(typename, size, fieldSignatureName);
		} else {
			getRegularType(typename, size, fieldSignatureName);
		}
	}

	
	public CType(BlendField field) {
		this(field.getType().getName(), field.getType().getSize(), field.getSignatureName());
	}


	private void getRegularType(String typename, short size, String fieldName) {
		if (isScalar(typename)) {
			typetype = CTypeType.TYPE_SCALAR;
		} else if (typename.equals("void")) {
			typetype = CTypeType.TYPE_VOID;
		} else {
			typetype = CTypeType.TYPE_STRUCT;
		}
	}


	private void getFunctionPointerType(String typename, short size,
			String fieldName) {
		typetype = CTypeType.TYPE_FUNCTION_POINTER;
	}


	private void getPointerType(String typename, short size, String fieldName) {
		typetype = CTypeType.TYPE_POINTER;
		fieldName = fieldName.substring(1);
		referencedType = new CType(typename, size, fieldName);
	}


	public void getArrayType(String typename, short size, String fieldName) {
		typetype = CTypeType.TYPE_ARRAY;
		
		int start = fieldName.indexOf('[');
		if (start < 0) throw new Error("inconsistent type declaration: " + fieldName);
		
		int end = fieldName.indexOf(']');
		String substr = fieldName.substring(start + 1, end);
		arrayLength = Integer.valueOf(substr.trim());
		fieldName = fieldName.substring(0, start) + fieldName.substring(end+1);
		
		referencedType = new CType(typename, size, fieldName);
	}
	

	private boolean isScalar(String typeName) {
		return (typeName.equals("char")) 
				|| (typeName.equals("short"))
				|| (typeName.equals("ushort"))
				|| (typeName.equals("int"))
				|| (typeName.equals("uint"))
				|| (typeName.equals("long"))
				|| (typeName.equals("ulong"))
				|| (typeName.equals("int64_t"))
				|| (typeName.equals("uint64_t"))
				|| (typeName.equals("float"))
				|| (typeName.equals("double"))
				;
	}


	public int getArrayLength() {
		return arrayLength;
	}


	public CType getReferencedType() {
		return referencedType;
	}


	public CTypeType getTypetype() {
		return typetype;
	}


	public String getName() {
		return name;
	}


	public String getSignature() {
		return signature;
	}


	public int getSize() {
		return size;
	}


	public long sizeof(long pointersize) {
		return sizeof(this, pointersize);
	}
	
	private long sizeof(CType ctype, long pointersize) {
		switch(ctype.getTypetype()) {
		case TYPE_ARRAY:
			long totalLength = ctype.getArrayLength();
			for (ctype = ctype.getReferencedType(); 
				 ctype.getTypetype() == CType.CTypeType.TYPE_ARRAY; 
				 ctype = ctype.getReferencedType()) 
			{
				totalLength *= ctype.getArrayLength();
			}
			return sizeof(ctype, pointersize) * totalLength;
		case TYPE_FUNCTION_POINTER:
		case TYPE_POINTER:
			return pointersize;
		case TYPE_SCALAR:
		case TYPE_STRUCT:
			return ctype.getSize();
		case TYPE_VOID:
		default:
			throw new IllegalArgumentException("sizeof can't determine size of '" + ctype.getName() + "'");
		}
	}
	
}
