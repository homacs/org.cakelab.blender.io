package org.cakelab.blender.model;

import java.io.IOException;

import org.cakelab.blender.model.DNAArray.DNAArrayIterator;

public class DNAPointerMutable<T> extends DNAPointer<T> {

	public DNAPointerMutable(DNAPointer<T> pointer) {
		super(pointer);
	}

	public DNAPointerMutable(DNAPointer<T> pointer, long address) {
		super(pointer, address);
	}

	/**
	 * 
	 * {@link #add(int)} is different to {@link #plus(int)}.
	 * While {@link #add(int)} modifies the address in-place
	 * {@link #plus(int)} returns new instance with the result
	 * of the addition.
	 * 
	 * 
	 * Equivalent to 
	 * <pre>
	 * int* p;
	 * (p += increment);
	 * </pre>
	 * Value of the pointer will be changed in place.
	 * 
	 * @param increment
	 * @return
	 */
	public DNAPointerMutable<T> add(int increment) {
		__dna__address += targetSize;
		return this;
	}

	/**
	 * {@link #add(int)} is different to {@link #plus(int)}.
	 * While {@link #add(int)} modifies the address in-place
	 * {@link #plus(int)} returns new instance with the result
	 * of the addition.
	 * 
	 * Allows (almost) equivalent handling as operator.
	 * <pre>
	 * int *p, *a;
	 * a = p = .. ;
	 * a = (p+1)+1;
	 * </pre>
	 * same as
	 * <pre>
	 * DNAPointerMutable<Integer> p, a;
	 * p = a = .. ;
	 * p = (p.plus(1)).plus(1);
	 * </pre>
	 * where the post condition
	 * <pre>
	 *  post condition: (p != a)
	 * </pre>
	 * holds.
	 * 
	 * @param value
	 * @return new instance of this pointer with an address+=targetSize
	 * @throws IOException
	 */
	public DNAPointerMutable<T> plus(int value) throws IOException {
		return new  DNAPointerMutable<T>(this, __dna__address + targetSize);
	}

	
	/**
	 * Equivalent to
	 * 
	 *<pre>
	 * int* p;
	 * int* a = ..;
	 * p = a;
	 *</pre>
	 * @param address
	 * @throws IOException
	 */
	public void assign(long address) throws IOException {
		__dna__address = address;
		if (!isValid()) {
			__dna__block = __dna__blockMap.getBlock(address);
		}
	}

	/**
	 * Equivalent to
	 * 
	 *<pre>
	 * int* p;
	 * int* a = ..;
	 * p = a;
	 *</pre>
	 * @param address
	 * @throws IOException
	 */
	public void assign(DNAPointer<T> address) throws IOException {
		assign(address.__dna__address);
	}

	/**
	 * Pointer comparison. 
	 * 
	 * This method provides pointer comparison functionality.
	 * It allows comparison to all objects derived from {@link DNAFacet}
	 * including pointers, arrays and iterators of both.
	 * @param obj
	 * @return
	 */
	@SuppressWarnings("rawtypes")
	@Override
	public boolean equals(Object obj) {
		if (obj == null) return false;
		if (obj instanceof DNAArrayIterator) {
			return __dna__address == ((DNAArrayIterator)obj).getCurrentAddress();
		}
		if (obj instanceof DNAFacet) {
			return ((DNAFacet) obj).__dna__address == __dna__address;
		}
		return false;
	}

	@Override
	public int hashCode() {
		return (int)((__dna__address>>32) | (__dna__address));
	}



}
