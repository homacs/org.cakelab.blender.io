package org.cakelab.blender.nio;

import java.io.IOException;
/**
 * This class is the mutable variant of {@link CPointer}.
 * <p>
 * Mutable pointers allow  in-place modification of the pointers 
 * address and thereby advanced pointer arithmetics 
 * (better runtime performance). Please note, that modifications 
 * to the address will not be reflected in the memory region this
 * pointer originated from.
 * </p>
 * <p>
 * You receive a mutable variant of a pointer either by using 
 * one of the copy constructors {@link #CPointerMutable(CPointer)}
 * or {@link #CPointerMutable(CPointer, long)} or by calling
 * the method {@link CPointer#mutable()}.
 * </p>
 * <h3>Pointer Arithmetics</h3>
 * <p>
 * Read documentation of {@link CPointer} first, to understand this
 * section.
 * </p><p>
 * {@link CPointerMutable} inherits the methods of {@link CPointer}. 
 * Since references to objects of {@link CPointerMutable} can be 
 * assigned to {@link CPointer}, the method {@link #plus(int)} has
 * to behave exactly like {@link CPointer#plus(int)} and return a
 * new instance of {@link CPointerMutable}.
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
 * {@link #assign(CPointer)}. 
 * </p>
 * @author homac
 *
 * @param <T> target type of the pointer.
 */
public class CPointerMutable<T> extends CPointer<T> {

	/**
	 * Constructor to turn a pointer into a mutable pointer.
	 * @param pointer
	 */
	public CPointerMutable(CPointer<T> pointer) {
		super(pointer);
	}

	public CPointerMutable(CPointer<T> pointer, long address) {
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
	public CPointerMutable<T> add(int increment) {
		__io__address += targetSize;
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
	 * CPointerMutable<Integer> p, a;
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
	public CPointerMutable<T> plus(int value) throws IOException {
		return new  CPointerMutable<T>(this, __io__address + targetSize);
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
		__io__address = address;
		if (!isValid()) {
			// XXX: can we leave it like that?
			__io__block = __io__blockTable.getBlock(address, -1);
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
	public void assign(CPointer<T> address) throws IOException {
		assign(address.__io__address);
	}

	/**
	 * Returns the value of the address.
	 * @return
	 */
	public long value() {
		return __io__address;
	}
	
}
