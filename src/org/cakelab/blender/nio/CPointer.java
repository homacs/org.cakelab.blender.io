package org.cakelab.blender.nio;
import java.io.IOException;
import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;

import org.cakelab.blender.io.block.Block;
import org.cakelab.blender.io.block.BlockTable;
import org.cakelab.blender.nio.CArrayFacade.CArrayFacadeIterator;

/**
 * Objects of this class represent a C pointer in Java. It provides
 * the following main functionalities:
 * <ul>
 * <li>pointer arithmetics</li>
 * <li>type casting</li>
 * </ul>
 * <h3>Pointer Type</h3>
 * A pointer in C is defined by its target type and the level
 * of indirection. For example <code>int** pint</code> is
 * said to be a pointer on a pointer of type <code>int</code>. 
 * Thus, the target type is <code>int</code> and the level of 
 * indirection is <code>2</code>.<br/>
 * In Java Blend a pointer is mapped to this template class where
 * the target type is provided through the template parameter <code>T</code>.
 * Thus an object of <code>CPointer&lt;CPointer&lt;Integer&gt;&gt;</code>
 * is the equivalent representation of <code>int**</code>.<br/>
 * 
 * <h3>CPointer Objects are Immutable</h3>
 * <p>
 * Objects of CPointer are immutable, that means you cannot change 
 * its address. Thus, CPointer objects behave like String's in Java:
 * Every 'modification' creates a copy of the object with that modification,
 * while the original objects stays unmodified. This has disadvantages in
 * performance, if you heavily depend on pointer arithmetics. Hence, there 
 * is another class called {@link CPointerMutable} which allows in-place
 * modification of pointers (similar to the relationship between String 
 * and StringBuffer).
 * </p>
 * <p>
 * To change the value of a pointer, which is stored in a block, you have to
 * use the setter method of the facade, array or pointer associated with the
 * block.
 * </p>
 * <h4>Examples</h4>
 * Consider a case where you have created a new object and you'd like to
 * attach it to the list of objects in a scene. In this case you need to 
 * assign the pointer to this object to the member variable id.next of the
 * previous object.
 * <pre>
 * BlenderObject newObject = ..; // instantiated a new object
 * ID id = object.getId();       // get id of previous object
 * 
 * // get a pointer on the new object
 * CPointer&lt;BlenderObject&gt; pNewObj = newObject.__io__addressof();
 * 
 * id.setNext(pNewObj);          // set id.next to point to new obj
 * </pre>
 * <p>
 * In the last line, the new address was written to the underlying block
 * associated with the previous object. 
 * </p>
 * <p>
 * In case you do not have a facade on the data in the block, you will
 * have either an array of pointers or a pointer of pointer. The following
 * example demonstrates it for an int* stored in a block.
 * </p>
 * <pre>
 * // We have a pointer refering to a pointer stored in a block
 * CPointer&lt;CPointer&lt;Integer&gt;&gt; ppint = ..; 
 * 
 * // And we have received a new address for that pointer from elsewhere
 * CPointer&lt;Integer&gt; pint = ..;
 * 
 * // To assign the new address to the pointer in the block we use the setter.
 * ppint.set(pint);
 * </pre>
 * 
 * <h3>Pointer Arithmetics</h3>
 * A pointer supports referencing (see {@link #get()}) and basic algebra
 * (see {@link #add(int)}). For advanced pointer arithmetics see 
 * {@link CPointerMutable}.
 * <h4>Referencing</h4>
 * Referencing the target <em>object</em> through
 * {@link #get()} returns that objects (Java) value. 
 * <ul>
 * <li>
 * If the target object
 * is of complex type (a class), the value returned is a reference on an 
 * instance of a class derived from {@link CFacade}. That means, you get
 * access by reference: all modifications to the objects data will be 
 * reflected in the memory backing the object. This applies also to 
 * {@link CArrayFacade} objects but it applies not to instances of 
 * {@link CPointer} itself (see below).
 * </li>
 * <li>
 * If the target object is a scalar, then the value is the value read from 
 * the target address. That means, modifications to that value will not 
 * be reflected in the memory location of its origination.
 * </li>
 * </ul>
 * An object of type {@link CPointer} is a reference but treated as a scalar.
 * That means, if you receive a pointer from a method (e.g. from a facade, array 
 * or another pointer) than it is a copy - disconnected from its own memory location. Any
 * modification to the pointer is not reflected in its original memory location.
 * To assign a new address to its original memory location, you have to use the 
 * set method of the object, which provided you the pointer.<br/>
 * <h4>Example</h4>
 * <pre>
 * 
 * 
 * CPointer<Link> next = link.getNext(); // retrieve address
 * Link anotherLink = .. ;                 // link we received elsewhere
 * link.setNext(anotherLink.__io__addressof());  // assign new address to link.next
 * </pre>
 * <p>
 * See also {@link CPointerMutable} and {@link CFacade#__io__addressof()}.
 * </p>
 * 
 * 
 * <h4>Basic Algebra</h4>
 * <p>
 * Typical pointer arithmetics are incrementing and decrementing the address
 * by the size of the target type. This actually reflects the same 
 * functionality provided by array types. CPointer provides different ways
 * to achieve this functionality: Conversion to an array (see Section 
 * Array Conversion below) and the method {@link #plus(int)}.
 * </p>
 * <p>Array conversion provides you either a Java array type or an iterable CArrayFacade
 * which is pretty straight forward to handle. The method {@link #plus(int)} increments
 * the address by the given increment multiplied by the size of the pointer 
 * target type. Thus you can use the pointer like an iterator as in the example below.
 * </p>
 * 
 * <h5>Example</h5>
 * <pre>
 * // iterating over a null terminated list of materials
 * for (CPointer<Material> pmat = .. ; 
 *      !pmat.isNull();                 // null check
 *      pmat = pmat.plus(+1))            // inc address by sizeof(Material)
 * {
 *   Material mat = pmat.get();
 * }
 * </pre>
 * <p>
 * Please note, that the result of {@link #plus(int)} has to be assigned 
 * to pmat, since {@link CPointer} is immutable and {@link #plus(int)} does
 * not change the address of the pointer itself (see also {@link CPointerMutable}).
 * </p>
 * <p>
 * This functionality of course requires that the pointer is of the correct
 * type (same as in C). Pointer arithmetics to a <code>void*</code> 
 * (which is mapped to <code>CPointer&lt;Object&gt;</code> are permitted.
 * Thus, you have to cast the pointer to the correct target type first 
 * (see Section Type Casts below).
 * </p>
 * 
 * <h3>Type Casts</h3>
 * Type casts are supported through the methods {@link #cast(Class)} and  
 * {@link #cast(Class[])}. Both take a parameter which describes the new
 * target type of the casted pointer. Since template parameters
 * are not available at runtime, type casts of pointers with multiple
 * indirections and pointers on arrays require you to provide even the 
 * types of the targeted types. For example, if you have a pointer
 * on a pointer of type Link, than the first pointer is of type pointer
 * on pointer and the second pointer is of type pointer on Link.
 * <h4>Example</h4>
 * <pre>
 * CPointer&lt;CPointer&lt;Link&gt;&gt; pplink = .. ;
 * CPointer&lt;CPointer&lt;Scene&gt;&gt; ppscene;
 * ppscene = pplink.cast(new Class[]{Pointer.class, Scene.class};
 * CPointer&lt;Scene&gt; pscene = ppscene.get();
 * Scene scene = pscene.get();
 * </pre>
 * <p>This can get confusing but you will need to cast pointers 
 * with multiple levels of indirection rather rare or never.</p>
 * 
 * <h3>Array Conversion</h3>
 * <p>
 * The method {@link #toArray(int)} returns an array with copies
 * of the values received from the address the pointer points to.
 * </p>
 * <p>
 * Since it is not possible to use scalar types (such as int, double etc.)
 * as template parameter, there are several different flavours of 
 * {@link #toArray(byte[], int, int)} and {@link #toByteArray(int)} 
 * for all scalar types. Please note, that there are two special methods
 * for int64, since long can refer to integer of 4 or 8 byte depending
 * on the data received from blender file. Refer to the originating 
 * facade, you received the pointer from, to determine its actual type.
 * </p>
 * @author homac
 *
 * @param <T> Target type of the pointer.
 */
public class CPointer<T> extends CFacade {
	// TODO: ZZZ could need fromArray for multidimensional arrays too
	/**
	 * Type of the target the pointer is able to address.
	 */
	protected Class<?>[] targetTypeList;
	protected long targetSize;
	
	/** In case this pointer references instances of structs, we cache
	 * a reference on the constructor to instantiate instances. */
	private Constructor<T> constructor;
	
	/**
	 * Copy constructor which allows assigning another address.
	 * <h3>Preconditions:</h3>
	 * targetAddress has to be in the block which is currently assigned to 
	 * the 'other' pointer.
	 * @param other Pointer which will be copied.
	 * @param targetAddress Address, the new pointer will point to.
	 */
	CPointer(CPointer<T> other, long targetAddress) {
		super(other, targetAddress);
		this.targetTypeList = other.targetTypeList;
		this.targetSize = other.targetSize;
		this.constructor = other.constructor;
	}

	/**
	 * Copy constructor. It creates another instance of the 
	 * 'other' pointer.
	 * 
	 * @param other Pointer to be copied.
	 */
	public CPointer(CPointer<T> other) {
		this(other, other.__io__address);
	}
	
	/**
	 * Common constructor for pointers.
	 * 
	 * @param targetAddress Address the pointer will point to.
	 * @param targetTypes Type of the pointer (please read class documentation)
	 * @param block Block, which contains the targetAddress.
	 * @param memory Associated block table, which contains memory for the targetAddress.
	 */
	@SuppressWarnings("unchecked")
	public CPointer(long targetAddress, Class<?>[] targetTypes, Block block, BlockTable memory) {
		super(targetAddress, block, memory);
		this.targetTypeList = (Class<T>[]) targetTypes;
		this.targetSize = __io__sizeof(targetTypes[0]);
	}
	
	/**
	 * This method returns the value, the pointer points to.
	 * Whether the returned value is a reference or a copy depends
	 * on its type. 
	 * 
	 * If this is a null pointer, the value returned will be null. 
	 * In case this pointer references a scalar type, you should check
	 * {@link #isNull()} before calling {@link #get()}.
	 * 
	 * <ul>
	 * <li>Scalar types will be returned by value.</li>
	 * <li>Pointers will be returned by value.</li>
	 * <li>Arrays and structs will be returned by reference.</li>
	 * </ul>
	 * 
	 * @return Returns the value the pointer points to.
	 * @throws IOException
	 */
	public T get() throws IOException {
		if (isNull()) return null;
		else return __get(__io__address);
	}
	
	protected T __get(long address) throws IOException {
		if (targetSize == 0) throw new ClassCastException("Target type is unspecified (i.e. void*). Use cast() to specify its type first.");
		if (isPrimitive(targetTypeList[0])) {
			return getScalar(address);
		} else if (targetTypeList[0].equals(CArrayFacade.class)){
			throw new ClassCastException("Impossible type declaration containing a pointer on an array (Cannot be declared in C).");
		} else {
			return (T) getCFacade(address);
		}
	}
	
	/**
	 * Sets the value the pointer points to.
	 * 
	 * This performs a (shallow) copy of the underlying
	 * data. If the referenced value is an instance of a struct 
	 * or array this method will perform a copy of the memory 
	 * region inside the associated block of the source instance ('value').
	 * If the data is a scalar or a pointer this method will write its
	 * value to the address where this pointer refers to.
	 * 
	 * @param value Value to be copied to the address this pointer points to.
	 * @throws IOException
	 */
	public void set(T value) throws IOException {
		__set(__io__address, value);
	}

	/**
	 * Copies the given value to the given address.
	 * See also {@link #set(Object)}.
	 * @param address to write the value to.
	 * @param value to be copied.
	 * @throws IOException
	 */
	protected void __set(long address, T value) throws IOException {
		if (isPrimitive(targetTypeList[0])) {
			setScalar(address, value);
		} else if (targetTypeList[0].equals(CPointer.class)) {
			CPointer<?> p = (CPointer<?>) value;
			long referenced_address = (p == null) ? 0 : p.__io__address;
			__io__block.writeLong(address, referenced_address);
		} else {
			// object or array
			
			if (__io__equals((CFacade)value, address)) {
				// This is a reference on the object, which is already stored in our block.
				// Thus, any changes made to the members of the facade are already 
				// reflected in referenced memory region.
			} else if (__io__same__encoding(this, (CFacade)value)) {
				// we can perform a low level copy
				__io__native__copy(__io__block, address, (CFacade)value);
			} else {
				// we have to reinterpret data to convert to different encoding
				__io__generic__copy((CFacade)__get(address));
			}
		}
	}
	

	/**
	 * This returns the address this pointer points to.
	 * 
	 * @return
	 * @throws IOException address the pointer points to
	 */
	public long getAddress() throws IOException {
		return __io__address;
	}
	
	/**
	 * Checks whether the address of the pointer equals null.
	 * @return
	 */
	public boolean isNull() {
		return __io__address == 0;
	}

	/**
	 * Tells whether the pointer points to actual data or in 
	 * a region of memory, which does not exist.
	 * Thus, it is a bit more than a null pointer check ({@link #isNull()} 
	 * and it is more expensive performance wise.<br/>
	 * 
	 * This method is mainly intended for debugging purposes.
	 * 
	 * @return true, if you can access the memory
	 */
	public boolean isValid() {
		return !isNull() && (__io__block != null && __io__block.contains(__io__address)); 
	}
	
	/**
	 * Type cast for pointers with just one indirection.
	 * 
	 * Creates a new pointer of a different targetType.
	 * <pre>
	 * Pointer&lt;ListBase&gt; p; 
	 * ..
	 * Pointer &lt;Scene&gt; pscene = p.cast(Scene.class);
	 * </pre>
	 * 
	 * <h4>Attention!</h4>
	 * This is a very dangerous and error prone method since you can
	 * cast to anything. But you will need it several times.
	 * 
	 * @param type
	 * @return
	 */
	public <U> CPointer<U> cast(Class<U> type) {
		return new CPointer<U>(__io__address, new Class<?>[]{type}, __io__block, __io__blockTable);
	}
	
	/**
	 * Type cast for pointers with multiple levels of indirection 
	 * (pointer on pointer). 
	 * 
	 * Creates a new pointer of a different targetType.
	 * <pre>
	 * CPointer&lt;CPointer&lt;ListBase&gt;&gt; p; 
	 * ..
	 * CPointer&lt;CPointer&lt;Scene&gt;&gt; pscene = p.cast(new Class<?>[]{CPointer.class, Scene.class});
	 * </pre>
	 * 
	 * <h4>Attention!</h4>
	 * This is an even more dangerous and error prone method than 
	 * {@link CPointer#cast(Class)} since you can do even more nasty stuff.
	 * 
	 * @param type
	 * @return
	 */
	public <U> CPointer<U> cast(Class<?>[] types) {
		return new CPointer<U>(__io__address, types, __io__block, __io__blockTable);
	}

	/**
	 * Type cast a pointer to an array. Since arrays require a length value, this
	 * method will work only on pointers, which are arrays already 
	 * (i.e. (<code>p <b>instanceof</b> CArrayFacade) == true</code>)). 
	 * 
	 * The parameter is needed only to differentiate it from other 
	 * <code>cast</code> methods. Thus, you can use it like this:
	 * <pre>
	 * CPointer&lt;Float&gt; p = null;
	 * CArrayFacade&lt;Float&gt; array = null;
	 * array = p.cast(array);
	 * </pre>
	 * 
	 * To create a new array facade for a given pointer you have to use the method 
	 * {@link #toCArrayFacade(int)}.
	 * 
	 * @param type
	 * @return pointer casted to CArrayFacade
	 * @throws IOException 
	 */
	public CArrayFacade<T> cast(CArrayFacade<T> type) throws IOException {
		// TODO: erase if possible
		if (this instanceof CArrayFacade) {
			return (CArrayFacade<T>)this;
		} else {
			throw new IOException("pointer does not point to an array");
		}
	}

	
	
	/**
	 * Converts the data referenced by the pointer into a Java array 
	 * of the given length.
	 * 
	 * @param length of the new Java array instance.
	 * @return New Java array instance or null if the pointer is null ({@link CPointer#isNull()}).
	 * @throws IOException
	 */
	@SuppressWarnings("unchecked")
	public T[] toArray(int length) throws IOException {
		if (isNull()) return null;
		
		T[] arr = (T[])Array.newInstance(targetTypeList[0], length);
		long address = __io__address;
		for (int i = 0; i < length; i++) {
			arr[i] = __get(address);
			address += targetSize;
		}
		return arr;
	}
	

	/**
	 * Converts the data referenced by the pointer into an array with 
	 * a CArrayFacade of the given length.
	 * 
	 * @param length
	 * @return
	 * @throws IOException
	 */
	public CArrayFacade<T> toCArrayFacade(int len) {
		return new CArrayFacade<T>(__io__address, targetTypeList, new int[]{len}, __io__block, __io__blockTable);
	}
	
	/**
	 * Copies 'len' bytes from the memory referenced by this pointer 
	 * to the given 'data' array starting at index 'off'.
	 * 
	 * @param data Array, to copy the data to.
	 * @param off Start index in the data array.
	 * @param len Amount of elements to be copied to data.
	 * @return The array provided with param 'data'
	 * @throws IOException
	 */
	public byte[] toArray(byte[] data, int off, int len)
			throws IOException {
		if (!targetTypeList[0].equals(Byte.class)) throw new ClassCastException("cannot cast " + targetTypeList[0].getSimpleName() + " to " + data.getClass().getSimpleName() + ". You have to cast the pointer first.");
		__io__block.readFully(__io__address, data, off, len);
		return data;
	}

	/**
	 * Copies 'len' elements from from the given 'data' array starting at index 'off'
	 * to the memory referenced by this pointer.
	 * 
	 * @param data Array, to copy the data from.
	 * @param off Start index in the data array.
	 * @param len Amount of elements to be copied from 'data'.
	 * @throws IOException
	 */
	public void fromArray(byte[] data, int off, int len) throws IOException {
		if (!targetTypeList[0].equals(Byte.class)) throw new ClassCastException("cannot cast " + data.getClass().getSimpleName() + " to " + targetTypeList[0].getSimpleName() + ". You have to cast the pointer first.");
		__io__block.writeFully(__io__address, data, off, len);
	}

	/**
	 * Converts the data referenced by the pointer into a Java array 
	 * of the given length.
	 * 
	 * @param length of the new Java array instance.
	 * @return New Java array instance.
	 * @throws IOException
	 */
	public byte[] toByteArray(int len)
			throws IOException {
		return toArray(new byte[len], 0, len);
	}

	/**
	 * Copyies all elements from from the given 'data' array 
	 * to the memory referenced by this pointer.
	 * 
	 * @param data Array, to copy the data from.
	 * @throws IOException
	 */
	public void fromArray(byte[] data) throws IOException {
		fromArray(data, 0, data.length);
	}
	

	/**
	 * Copyies 'len' elements from the memory referenced by this pointer 
	 * to the given 'data' array starting at index 'off'.
	 * 
	 * @param data Array, to copy the data to.
	 * @param off Start index in the data array.
	 * @param len Amount of elements to be copied to data.
	 * @return The array provided with param 'data'
	 * @throws IOException
	 */
	public short[] toArray(short[] data, int off, int len) throws IOException {
		if (!targetTypeList[0].equals(Short.class)) throw new ClassCastException("cannot cast " + targetTypeList[0].getSimpleName() + " to " + data.getClass().getSimpleName() + ". You have to cast the pointer first.");
		__io__block.readFully(__io__address, data, off, len);
		return data;
	}
	
	/**
	 * Copyies 'len' elements from from the given 'data' array starting at index 'off'
	 * to the memory referenced by this pointer.
	 * 
	 * @param data Array, to copy the data from.
	 * @param off Start index in the data array.
	 * @param len Amount of elements to be copied from 'data'.
	 * @throws IOException
	 */
	public void fromArray(short[] data, int off, int len) throws IOException {
		if (!targetTypeList[0].equals(Short.class)) throw new ClassCastException("cannot cast " + data.getClass().getSimpleName() + " to " + targetTypeList[0].getSimpleName() + ". You have to cast the pointer first.");
		__io__block.writeFully(__io__address, data, off, len);
	}
	
	/**
	 * Converts the data referenced by the pointer into a Java array 
	 * of the given length.
	 * 
	 * @param length of the new Java array instance.
	 * @return New Java array instance.
	 * @throws IOException
	 */
	public short[] toShortArray(int len)
			throws IOException {
		return toArray(new short[len], 0, len);
	}

	/**
	 * Copyies all elements from from the given 'data' array 
	 * to the memory referenced by this pointer.
	 * 
	 * @param data Array, to copy the data from.
	 * @throws IOException
	 */
	public void fromArray(short[] data) throws IOException {
		fromArray(data, 0, data.length);
	}
	

	/**
	 * Copyies 'len' elements from the memory referenced by this pointer 
	 * to the given 'data' array starting at index 'off'.
	 * 
	 * @param data Array, to copy the data to.
	 * @param off Start index in the data array.
	 * @param len Amount of elements to be copied to data.
	 * @return The array provided with param 'data'
	 * @throws IOException
	 */
	public int[] toArray(int[] data, int off, int len) throws IOException {
		if (!targetTypeList[0].equals(Integer.class)) throw new ClassCastException("cannot cast " + targetTypeList[0].getSimpleName() + " to " + data.getClass().getSimpleName() + ". You have to cast the pointer first.");
		__io__block.readFully(__io__address, data, off, len);
		return data;
	}

	/**
	 * Converts the data referenced by the pointer into a Java array 
	 * of the given length.
	 * 
	 * @param length of the new Java array instance.
	 * @return New Java array instance.
	 * @throws IOException
	 */
	public int[] toIntArray(int len)
			throws IOException {
		return toArray(new int[len], 0, len);
	}

	/**
	 * Copyies 'len' elements from from the given 'data' array starting at index 'off'
	 * to the memory referenced by this pointer.
	 * 
	 * @param data Array, to copy the data from.
	 * @param off Start index in the data array.
	 * @param len Amount of elements to be copied from 'data'.
	 * @throws IOException
	 */
	public void fromArray(int[] data, int off, int len) throws IOException {
		if (!targetTypeList[0].equals(Integer.class)) throw new ClassCastException("cannot cast " + data.getClass().getSimpleName() + " to " + targetTypeList[0].getSimpleName() + ". You have to cast the pointer first.");
		__io__block.writeFully(__io__address, data, off, len);
	}
	
	/**
	 * Copyies all elements from from the given 'data' array 
	 * to the memory referenced by this pointer.
	 * 
	 * @param data Array, to copy the data from.
	 * @throws IOException
	 */
	public void fromArray(int[] data) throws IOException {
		fromArray(data, 0, data.length);
	}
	

	/**
	 * Copyies 'len' elements from the memory referenced by this pointer 
	 * to the given 'data' array starting at index 'off'.
	 * 
	 * @param data Array, to copy the data to.
	 * @param off Start index in the data array.
	 * @param len Amount of elements to be copied to data.
	 * @return The array provided with param 'data'
	 * @throws IOException
	 */
	public long[] toArray(long[] data, int off, int len) throws IOException {
		if (!targetTypeList[0].equals(Long.class)) throw new ClassCastException("cannot cast " + targetTypeList[0].getSimpleName() + " to " + data.getClass().getSimpleName() + ". You have to cast the pointer first.");
		__io__block.readFully(__io__address, data, off, len);
		return data;
	}

	/**
	 * Converts the data referenced by the pointer into a Java array 
	 * of the given length.
	 * 
	 * @param length of the new Java array instance.
	 * @return New Java array instance.
	 * @throws IOException
	 */
	public long[] toLongArray(int len)
			throws IOException {
		return toArray(new long[len], 0, len);
	}

	/**
	 * Copyies 'len' elements from from the given 'data' array starting at index 'off'
	 * to the memory referenced by this pointer.
	 * 
	 * @param data Array, to copy the data from.
	 * @param off Start index in the data array.
	 * @param len Amount of elements to be copied from 'data'.
	 * @throws IOException
	 */
	public void fromArray(long[] data, int off, int len) throws IOException {
		if (!targetTypeList[0].equals(Long.class)) throw new ClassCastException("cannot cast " + data.getClass().getSimpleName() + " to " + targetTypeList[0].getSimpleName() + ". You have to cast the pointer first.");
		__io__block.writeFully(__io__address, data, off, len);
	}
	
	/**
	 * Copyies all elements from from the given 'data' array 
	 * to the memory referenced by this pointer.
	 * 
	 * @param data Array, to copy the data from.
	 * @throws IOException
	 */
	public void fromArray(long[] data) throws IOException {
		fromArray(data, 0, data.length);
	}
	

	/**
	 * Copyies 'len' elements from the memory referenced by this pointer 
	 * to the given 'data' array starting at index 'off'.
	 * 
	 * @param data Array, to copy the data to.
	 * @param off Start index in the data array.
	 * @param len Amount of elements to be copied to data.
	 * @return The array provided with param 'data'
	 * @throws IOException
	 */
	public long[] toArrayInt64(long[] data, int off, int len) throws IOException {
		if (!targetTypeList[0].equals(int64.class)) throw new ClassCastException("cannot cast " + targetTypeList[0].getSimpleName() + " to " + data.getClass().getSimpleName() + ". You have to cast the pointer first.");
		__io__block.readFullyInt64(__io__address, data, off, len);
		return data;
	}

	/**
	 * Converts the data referenced by the pointer into a Java array 
	 * of the given length.
	 * 
	 * @param length of the new Java array instance.
	 * @return New Java array instance.
	 * @throws IOException
	 */
	public long[] toInt64Array(int len)
			throws IOException {
		return toArray(new long[len], 0, len);
	}

	/**
	 * Copyies 'len' elements from from the given 'data' array starting at index 'off'
	 * to the memory referenced by this pointer.
	 * 
	 * @param data Array, to copy the data from.
	 * @param off Start index in the data array.
	 * @param len Amount of elements to be copied from 'data'.
	 * @throws IOException
	 */
	public void fromInt64Array(long[] data, int off, int len) throws IOException {
		if (!targetTypeList[0].equals(int64.class)) throw new ClassCastException("cannot cast " + data.getClass().getSimpleName() + " to " + targetTypeList[0].getSimpleName() + ". You have to cast the pointer first.");
		__io__block.writeFullyInt64(__io__address, data, off, len);
	}
	
	/**
	 * Copyies all elements from from the given 'data' array 
	 * to the memory referenced by this pointer.
	 * 
	 * @param data Array, to copy the data from.
	 * @throws IOException
	 */
	public void fromInt64Array(long[] data) throws IOException {
		fromArray(data, 0, data.length);
	}
	


	/**
	 * Copyies 'len' elements from the memory referenced by this pointer 
	 * to the given 'data' array starting at index 'off'.
	 * 
	 * @param data Array, to copy the data to.
	 * @param off Start index in the data array.
	 * @param len Amount of elements to be copied to data.
	 * @return The array provided with param 'data'
	 * @throws IOException
	 */
	public float[] toArray(float[] data, int off, int len) throws IOException {
		if (!targetTypeList[0].equals(Float.class)) throw new ClassCastException("cannot cast " + targetTypeList[0].getSimpleName() + " to " + data.getClass().getSimpleName() + ". You have to cast the pointer first.");
		__io__block.readFully(__io__address, data, off, len);
		return data;
	}

	/**
	 * Converts the data referenced by the pointer into a Java array 
	 * of the given length.
	 * 
	 * @param length of the new Java array instance.
	 * @return New Java array instance.
	 * @throws IOException
	 */
	public float[] toFloatArray(int len)
			throws IOException {
		return toArray(new float[len], 0, len);
	}

	/**
	 * Copyies 'len' elements from from the given 'data' array starting at index 'off'
	 * to the memory referenced by this pointer.
	 * 
	 * @param data Array, to copy the data from.
	 * @param off Start index in the data array.
	 * @param len Amount of elements to be copied from 'data'.
	 * @throws IOException
	 */
	public void fromArray(float[] data, int off, int len) throws IOException {
		if (!targetTypeList[0].equals(Float.class)) throw new ClassCastException("cannot cast " + data.getClass().getSimpleName() + " to " + targetTypeList[0].getSimpleName() + ". You have to cast the pointer first.");
		__io__block.writeFully(__io__address, data, off, len);
	}
	
	/**
	 * Copyies all elements from from the given 'data' array 
	 * to the memory referenced by this pointer.
	 * 
	 * @param data Array, to copy the data from.
	 * @throws IOException
	 */
	public void fromArray(float[] data) throws IOException {
		fromArray(data, 0, data.length);
	}
	


	/**
	 * Copyies 'len' elements from the memory referenced by this pointer 
	 * to the given 'data' array starting at index 'off'.
	 * 
	 * @param data Array, to copy the data to.
	 * @param off Start index in the data array.
	 * @param len Amount of elements to be copied to data.
	 * @return The array provided with param 'data'
	 * @throws IOException
	 */
	public double[] toArray(double[] data, int off, int len) throws IOException {
		if (!targetTypeList[0].equals(Double.class)) throw new ClassCastException("cannot cast " + targetTypeList[0].getSimpleName() + " to " + data.getClass().getSimpleName() + ". You have to cast the pointer first.");
		__io__block.readFully(__io__address, data, off, len);
		return data;
	}
	
	/**
	 * Converts the data referenced by the pointer into a Java array 
	 * of the given length.
	 * 
	 * @param length of the new Java array instance.
	 * @return New Java array instance.
	 * @throws IOException
	 */
	public double[] toDoubleArray(int len)
			throws IOException {
		return toArray(new double[len], 0, len);
	}

	/**
	 * Copyies 'len' elements from from the given 'data' array starting at index 'off'
	 * to the memory referenced by this pointer.
	 * 
	 * @param data Array, to copy the data from.
	 * @param off Start index in the data array.
	 * @param len Amount of elements to be copied from 'data'.
	 * @throws IOException
	 */
	public void fromArray(double[] data, int off, int len) throws IOException {
		if (!targetTypeList[0].equals(Double.class)) throw new ClassCastException("cannot cast " + data.getClass().getSimpleName() + " to " + targetTypeList[0].getSimpleName() + ". You have to cast the pointer first.");
		__io__block.writeFully(__io__address, data, off, len);
	}
	
	/**
	 * Copyies all elements from from the given 'data' array 
	 * to the memory referenced by this pointer.
	 * 
	 * @param data Array, to copy the data from.
	 * @throws IOException
	 */
	public void fromArray(double[] data) throws IOException {
		fromArray(data, 0, data.length);
	}
	


	/**
	 * Creates a mutable pointer which allows to change its address in-place.
	 * @see CPointerMutable
	 * @return
	 */
	public CPointerMutable<T> mutable() {
		return new CPointerMutable<T>(this);
	}

	
	/**
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
	 * @param value
	 * @return new instance of this pointer with an address+=targetSize
	 * @throws IOException
	 */
	public CPointer<T> plus(int value) throws IOException {
		return new  CPointer<T>(this, __io__address + targetSize);
	}

	/**
	 * XXX: test whether the pointer references the same file?
	 * 
	 * This method provides pointer comparison functionality.
	 * 
	 * It allows comparison to all objects derived from {@link CFacade}
	 * including pointers, arrays and iterators of both.
	 * @param obj
	 * @return
	 */
	@SuppressWarnings("rawtypes")
	@Override
	public boolean equals(Object obj) {
		if (obj == null) return false;
		if (obj instanceof CArrayFacadeIterator) {
			return __io__address == ((CArrayFacadeIterator)obj).getCurrentAddress();
		}
		if (obj instanceof CFacade) {
			return ((CFacade) obj).__io__address == __io__address;
		}
		return false;
	}


	/** 
	 * XXX: consider file, which contains the data?
	 */
	@Override
	public int hashCode() {
		return (int)((__io__address>>32) ^ (__io__address));
	}

	

	/**
	 * Determines if the given type is a primitive type (scalar).
	 * 
	 * Note: Works only for types supported by Java Blend.
	 * 
	 * @param type
	 * @return True if its a primitive type.
	 */
	protected boolean isPrimitive(Class<?> type) {
		
		return type.isPrimitive() 
				|| type.equals(int64.class)
				|| type.equals(Byte.class)
				|| type.equals(Short.class)
				|| type.equals(Integer.class)
				|| type.equals(Long.class)
				|| type.equals(Float.class)
				|| type.equals(Double.class)
				;
	}


	/**
	 * Returns a facade or pointer on the given targetAddress, depending on the
	 * type of this pointer.
	 * 
	 * Type of the facade is specified by the target type of 
	 * the pointer.
	 * <h3>Precoditions:</h3>
	 * <ul>
	 * <li>targetAddress has to be in range of the associated block of this pointer.</li>
	 * <li>Type of this pointer has to be either a pointer or a struct 
	 * (i.e. derived from {@link CFacade}).</li>
	 * </ul>
	 * 
	 * @param targetAddress Address 
	 * @return facade or pointer on the given address
	 * @throws IOException
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	protected T getCFacade(long targetAddress) throws IOException {
		try {
			if (targetTypeList[0].equals(CPointer.class)) {
				// pointer on pointer
				long address = __io__block.readLong(targetAddress);
				Class<?>[] type = Arrays.copyOfRange(targetTypeList, 1, targetTypeList.length);
				Block block = __io__blockTable.getBlock(address, type);
				return (T) new CPointer(address, type, block, __io__blockTable);
			} else {
				if (isNull()) return null;
				// pointer on struct
				if (constructor == null) {
					constructor = (Constructor<T>) targetTypeList[0].getDeclaredConstructor(long.class, Block.class, BlockTable.class);
				}
				return (T) CFacade.__io__newInstance(constructor, (Class<? extends CFacade>) targetTypeList[0], targetAddress, __io__block, __io__blockTable);
			}
		} catch (InstantiationException | IllegalAccessException
				| IllegalArgumentException | InvocationTargetException | NoSuchMethodException | SecurityException e) {
			throw new IOException(e);
		}
	}


	/**
	 * Returns a copy of the scalar from memory referenced by the given address.
	 * 
	 * <h3>Precoditions:</h3>
	 * <ul>
	 * <li>targetAddress has to be in range of the associated block of this pointer.</li>
	 * <li>Type of this pointer has to scalar (i.e. int, double, etc.).</li>
	 * </ul>
	 * @param address
	 * @return
	 * @throws IOException
	 */
	@SuppressWarnings("unchecked")
	protected T getScalar(long address) throws IOException {
		Object result = null;
		
		Class<?> type = targetTypeList[0];
		
		if (type.equals(Byte.class) || type.equals(byte.class)) {
			result = __io__block.readByte(address);
		} else if (type.equals(Short.class) || type.equals(short.class)) {
			result = __io__block.readShort(address);
		} else if (type.equals(Integer.class) || type.equals(int.class)) {
			result = __io__block.readInt(address);
		} else if (type.equals(Long.class) || type.equals(long.class)) {
			result = __io__block.readLong(address);
		} else if (type.equals(int64.class)) {
			result = __io__block.readInt64(address);
		} else if (type.equals(Float.class) || type.equals(float.class)) {
			result = __io__block.readFloat(address);
		} else if (type.equals(Double.class) || type.equals(double.class)) {
			result = __io__block.readDouble(address);
		} else {
			throw new ClassCastException("unrecognized scalar type: " + type.getName());
		}
		return (T)result;
	}

	
	protected void setScalar(long address, T elem) throws IOException {
		Class<?> type = targetTypeList[0];
		
		if (type.equals(Byte.class)) {
			__io__block.writeByte(address, (Byte) elem);
		} else if (type.equals(Short.class)) {
			__io__block.writeShort(address, (Short) elem);
		} else if (type.equals(Integer.class)) {
			__io__block.writeInt(address, (Integer) elem);
		} else if (type.equals(Long.class)) {
			__io__block.writeLong(address, (Long) elem);
		} else if (type.equals(int64.class)) {
			__io__block.writeInt64(address, (Long) elem);
		} else if (type.equals(Float.class)) {
			__io__block.writeFloat(address, (Float) elem);
		} else if (type.equals(Double.class)) {
			__io__block.writeDouble(address, (Double) elem);
		} else {
			throw new ClassCastException("unrecognized scalar type: " + type.getName());
		}
	}

	
}
