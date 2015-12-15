package org.cakelab.blender.metac;

import org.cakelab.blender.io.Encoding;

public class CType {

	// TODO: ZZZ rename to CKind
	public enum CKind {
		TYPE_POINTER,
		TYPE_FUNCTION_POINTER,
		TYPE_ARRAY,
		TYPE_SCALAR,
		TYPE_STRUCT,
		TYPE_VOID
	}

	protected int arrayLength;
	/** This is the type of component in case of an array or the 
	 * target type in case of a pointer. */
	protected CType referencedType;
	protected CKind kind;
	protected String signature;

	public int size32;
	public int size64;
	
	public CType(String typesig, CKind kind) {
		this.signature = typesig;
		this.kind = kind;
	}

	public CType(String typesig, CKind kind,
			int size32, int size64) {
		this(typesig, kind);
		this.size32 = size32;
		this.size64 = size64;
	}

	public int getArrayLength() {
		return arrayLength;
	}

	public CType getReferencedType() {
		return referencedType;
	}

	public CKind getKind() {
		return kind;
	}

	public String getSignature() {
		return signature;
	}


	/**
	 * @return total number of basic data elements of an array or array of arrays with fixed length.
	 */
	public int getTotalNumArrayElems() {
		
		int totalLength = 1;
		for (CType array = this; array.kind == CKind.TYPE_ARRAY; array = array.getReferencedType()) {
			totalLength *= array.arrayLength;
		}
		return totalLength;
	}

	public int sizeof(int addressWidth) {
		switch(addressWidth) {
		case Encoding.ADDR_WIDTH_32BIT:
			return size32;
		case Encoding.ADDR_WIDTH_64BIT:
			return size64;
		default:
			throw new IllegalArgumentException("addressWidth must be one of Encoding.ADDR_WIDTH_32BIT or Encoding.ADDR_WIDTH_64BIT");
		}
	}
	
}
