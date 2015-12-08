package org.cakelab.blender.model;

import java.io.IOException;
/**
 * <p>
 * This class is the mutable variant of {@link DNAPointer}. It allows
 * in-place modification of the pointers address and theirby advanced
 * pointer arithmetics (better runtime performance).
 * </p>
 * <p>
 * You receive a mutable variant of a pointer either by using 
 * one of the copy constructors {@link #DNAPointerMutable(DNAPointer)}
 * or {@link #DNAPointerMutable(DNAPointer, long)} or by calling
 * the method {@link DNAPointer#mutable()}.
 * </p>
 * <h3>Pointer Arithmetics</h3>
 * <p>
 * Read documentation of {@link DNAPointer} first, to understand this
 * section.
 * </p><p>
 * {@link DNAPointerMutable} inherits the methods of {@link DNAPointer}. 
 * Since references to objects of {@link DNAPointerMutable} can be 
 * assigned to {@link DNAPointer}, the method {@link #plus(int)} has
 * to behave exactly like {@link DNAPointer#plus(int)} and return a
 * new instance of {@link DNAPointerMutable}.
 * </p>
 * <p>
 * The method {@link #add(int)} now provides the functionality of 
 * {@link #plus(int)} with in-place modification, meaning the address 
 * of the pointer object, on which the method was called, will be 
 * changed afterwards.
 * </p>
 * <p>
 * Furthermore, mutable pointers support direct modification of their
 * address by use of the methods {@link #assign(long)} and 
 * {@link #assign(DNAPointer)}. 
 * </p>
 * @author homac
 *
 * @param <T> target type of the pointer.
 */
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
	 * {@link #plus(int)} returns a new instance with the result
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
	 * @param value value to be added to the address, multiplied by sizeof(targetType).
	 * @return new instance of this pointer with an address+=(targetSize*value)
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
	 * Returns the value of the address.
	 * @return
	 */
	public long value() {
		return __dna__address;
	}
	
}
