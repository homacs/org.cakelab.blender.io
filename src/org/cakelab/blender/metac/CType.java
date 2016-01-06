package org.cakelab.blender.metac;

import org.cakelab.blender.io.Encoding;

/**
 * Stores type information.
 * @see CMetaModel
 * @author homac
 *
 */
public class CType {

	/**
	 * Provides information about the kind of data type.
	 * 
	 * @author homac
	 *
	 */
	public enum CKind {
		TYPE_POINTER,
		TYPE_FUNCTION_POINTER,
		TYPE_ARRAY,
		TYPE_SCALAR,
		TYPE_STRUCT,
		TYPE_VOID
	}

	/**
	 * length of array (if type is an array)
	 */
	protected int arrayLength;
	/** This is the type of component in case of an array or the 
	 * target type in case of a pointer. */
	protected CType referencedType;
	/** Kind of that type. */
	protected CKind kind;
	/** full C signature of that type, e.g. int**[12] */
	protected String signature;

	/**
	 * Size on 32bit architecture.
	 */
	public int size32;
	/**
	 * Size on 64bit architecture.
	 */
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

	/**
	 * In case of a multidimensional array, this will 
	 * return the length of the array of the first 
	 * dimension. The length of the array of the next 
	 * dimension can be determined by 
	 * getReferencedType().getArrayLength()
	 * and so forth.
	 * @return
	 */
	public int getArrayLength() {
		return arrayLength;
	}

	/** 
	 * In case of a pointer, this will return the type of
	 * elements, the pointer points to.
	 * In case of an array, this will return the arrays 
	 * component type.
	 * @return referenced type.
	 */
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

	/**
	 * Returns the overall size of this type considering specifica such as array lengths.
	 * @param addressWidth
	 * @return
	 */
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
