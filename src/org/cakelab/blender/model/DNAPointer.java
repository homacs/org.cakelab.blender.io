package org.cakelab.blender.model;
import java.io.IOException;
import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;

import org.cakelab.blender.file.block.BlockMap;

/**
 * This class covers the functionality of a pointer in C:
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
 * Thus an object of <code>DNAPointer&lt;DNAPointer&lt;Integer&gt;&gt;</code>
 * is the equivalent representation of <code>int**</code>.<br/>
 * 
 * 
 * <h3>Pointer Arithmetics</h3>
 * 
 * TODO: document pointer arithmetics
 * 
 * A pointer supports referencing (see {@link #get()}) and basic algebra
 * (see {@link #add(int)}). 
 * <h4>Referencing</h4>
 * Referencing the target <em>object</em> through
 * {@link #get()} returns that objects (Java) value. 
 * <ul>
 * <li>
 * If the target object
 * is of complex type (class), the value returned is a reference on an 
 * instance of a class derived from {@link DNAFacet}. That means, you get
 * access by reference: all modifications to the objects data will be 
 * reflected in the memory backing the object. This applies also to 
 * {@link DNAArray} objects but it applies not to instances of 
 * {@link DNAPointer} itself (see below).
 * </li>
 * <li>
 * If the target object is a scalar, then the value is the value read from 
 * the target address. That means, modifications to that value will not 
 * be reflected in the memory location of its origination.
 * </li>
 * </ul>
 * An object of type {@link DNAPointer} is a reference but treated as a scalar.
 * That means, if you receive a pointer from a method (e.g. from a facet, array 
 * or another pointer) than it is disconnected from its own memory location. Any
 * modifications to the pointer are not reflected in its original memory location.
 * To assign a new address to its original memory location, you have to use the 
 * set method of the object, which provided you the pointer.<br/>
 * <h4>Example</h4>
 * <pre>
 * DNAPointer<Link> next = link.getNext(); // retrieve address
 * Link anotherLink = .. ;                 // link we received elsewhere
 * next.set(anotherLink);                  // alter pointer
 * link.setNext(next);                     // assign new address to link.next
 * </pre>
 * 
 * <h4>Basic Algebra</h4>
 * Typical pointer arithmetics are incrementing and decrementing the address
 * by the size of the target type. This actually reflects the same 
 * functionality provided by array types. This functionality is provided
 * through the {@link #add(int)} and {@link #next()} method. {@link #add(int)} 
 * increments the pointers address by the size of its target type and 
 * {@link #next()} is just a convenient replacement for <code>add(+1)</code>.
 * <h5>Example</h5>
 * <pre>
 * // iterating over a null terminated list of materials
 * for (DNAPointer<Material> pmat = .. ; 
 *      !pmat.isNull();                 // null check
 *      pmat.add(+1))                   // inc address by sizeof(Material)
 * {
 *   Material mat = pmat.get();
 * }
 * </pre>
 * This functionality of course requires that the pointer is of the correct
 * type (same as in C). Pointer arithmetics to a <code>void*</code> 
 * (which is mapped to <code>DNAPointer&lt;Object&gt;</code> is permitted.
 * Thus, you have to cast the pointer to the correct target type first 
 * (see section below).
 * 
 * 
 * <h3>Type Casts</h3>
 * Type casts are supported through the methods {@link #cast(Class)} and  
 * {@link #cast(Class[])}. Both take a parameter which describes the new
 * target type, the pointer will be casted to. Since template parameters
 * are not available at runtime, type casts of pointers with multiple
 * indirections and pointers on arrays require you to provide even the 
 * types of the targeted types. For example, if you have a pointer
 * on a pointer of type Link, than the first pointer is of type pointer
 * on pointer and the second pointer is of type pointer on Link.
 * <h4>Example</h4>
 * <pre>
 * DNAPointer<DNAPointer<Link> pplink = .. ;
 * DNAPointer<DNAPointer<Scene> ppscene;
 * ppscene = pplink.cast(new Class[]{Pointer.class, Scene.class};
 * DNAPointer<Scene> pscene = ppscene.get();
 * Scene scene = pscene.get();
 * </pre>
 * 
 * <h3>Array Conversion</h3>
 * The method {@link #toArray(int)} returns an array with copies
 * of the values received from the address the pointer points to.
 * 
 * Since it is not possible to use scalar types (such as int, double etc.)
 * as template parameter, there are several different flavours of 
 * {@link #toArray(byte[], int, int)} and {@link #toByteArray(int)} 
 * for all scalar types. Please note, that there are two special methods
 * for int64, since long can refer to integer of 4 or 8 byte depending
 * on the data received from blender file. Refer to the originating 
 * facet, you received the pointer from, to determine its actual type.
 * 
 * @author homac
 *
 * @param <T> Target type of the pointer.
 */
public class DNAPointer<T> extends DNAFacet {
	@SuppressWarnings("rawtypes")
	public static final Constraint<DNAPointer> CONSTRAINT_POINTER_IS_VALID = new Constraint<DNAPointer>() {
		@Override
		public boolean satisfied(DNAPointer obj) {
			return obj.isValid();
		}
	};
	@SuppressWarnings("rawtypes")
	public static final Constraint<DNAPointer> CONSTRAINT_POINTER_NOT_NULL = new Constraint<DNAPointer>() {
		@Override
		public boolean satisfied(DNAPointer obj) {
			return !obj.isNull();
		}
	};
	/**
	 * Type of the target the pointer is able to address.
	 */
	protected Class<?>[] targetTypeList;
	protected long targetSize;
	private Constructor<T> constructor;
	
	
	DNAPointer(DNAPointer<T> other, long targetAddress) {
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
	public DNAPointer(DNAPointer<T> other) {
		this(other, other.__dna__address);
	}
	
	
	@SuppressWarnings("unchecked")
	public DNAPointer(long targetAddress, Class<?>[] targetTypes, BlockMap memory) {
		super(targetAddress, memory);
		this.targetTypeList = (Class<T>[]) targetTypes;
		this.targetSize = __dna__sizeof(targetTypes[0]);
	}
	
	/**
	 * @return Copy of the value the pointer points to.
	 * @throws IOException
	 */
	public T get() throws IOException {
		return __get(__dna__address);
	}
	
	public T __get(long address) throws IOException {
		if (targetSize == 0) throw new ClassCastException("Target type is unspecified (i.e. void*). Use cast() to specify its type first.");
		if (isPrimitive(targetTypeList[0])) {
			return getScalar(address);
		} else if (targetTypeList[0].isArray()){
			// TODO: array abstraction (not used in dna)
			throw new ClassCastException("unexpected case where pointer points on array.");
		} else {
			return (T) getDNAFacet(address);
		}
	}
	
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
	 * This returns the address this pointer points to.
	 * 
	 * @return
	 * @throws IOException address the pointer points to
	 */
	public long getAddress() throws IOException {
		return __dna__address;
	}
	
	/**
	 * Checks whether the address of the pointer equals null.
	 * @return
	 */
	public boolean isNull() {
		return __dna__address == 0;
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
		return !isNull() && (__dna__block != null && __dna__block.contains(__dna__address)); 
	}
	
	/**
	 * Type cast for pointers with just one indirection.
	 * 
	 * Casts the pointer to a different targetType.
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
	public <U> DNAPointer<U> cast(Class<U> type) {
		return new DNAPointer<U>(__dna__address, new Class<?>[]{type}, __dna__blockMap);
	}
	
	/**
	 * Type cast for pointers with multiple levels of indirection 
	 * (pointer on pointer). 
	 * Casts the pointer to a different targetType.
	 * <pre>
	 * DNAPointer&lt;DNAPointer&lt;ListBase&gt;&gt; p; 
	 * ..
	 * DNAPointer&lt;DNAPointer&lt;Scene&gt;&gt; pscene = p.cast(Scene.class);
	 * </pre>
	 * 
	 * <h4>Attention!</h4>
	 * This is an even more dangerous and error prone method than 
	 * {@link DNAPointer#cast(Class)} since you can do even more nasty stuff.
	 * 
	 * @param type
	 * @return
	 */
	public <U> DNAPointer<U> cast(Class<U>[] types) {
		return new DNAPointer<U>(__dna__address, types, __dna__blockMap);
	}

	/**
	 * Type cast for pointers with multiple levels of indirection 
	 * (pointer on pointer). 
	 * Casts the pointer to a different targetType.
	 * <pre>
	 * DNAPointer&lt;DNAPointer&lt;ListBase&gt;&gt; p; 
	 * ..
	 * DNAPointer&lt;DNAPointer&lt;Scene&gt;&gt; pscene = p.cast(Scene.class);
	 * </pre>
	 * 
	 * <h4>Attention!</h4>
	 * This is an even more dangerous and error prone method than 
	 * {@link DNAPointer#cast(Class)} since you can do even more nasty stuff.
	 * 
	 * @param type
	 * @return
	 * @throws IOException 
	 */
	public DNAArray<T> cast(DNAArray<T> type) throws IOException {
		if (this.getClass().equals(type)) {
			return (DNAArray<T>)this;
		} else {
			throw new IOException("not implemented");
		}
	}

	/**
	 * Converts the data referenced by the pointer into an Java array 
	 * of the given length.
	 * 
	 * @param length
	 * @return
	 * @throws IOException
	 */
	@SuppressWarnings("unchecked")
	public T[] toArray(int length) throws IOException {
		T[] arr = (T[])Array.newInstance(targetTypeList[0], length);
		long address = __dna__address;
		for (int i = 0; i < length; i++) {
			arr[i] = __get(address);
			address += targetSize;
		}
		return arr;
	}
	

	public DNAArray<T> toDNAArray(int len) {
		return new DNAArray<T>(__dna__address, targetTypeList, new int[]{len}, __dna__blockMap);
	}
	
	public byte[] toArray(byte[] b, int off, int len)
			throws IOException {
		if (!targetTypeList[0].equals(Byte.class)) throw new ClassCastException("cannot cast " + targetTypeList[0].getSimpleName() + " to " + b.getClass().getSimpleName() + ". You have to cast the pointer first.");
		__dna__block.readFully(__dna__address, b, off, len);
		return b;
	}

	public byte[] toByteArray(int len)
			throws IOException {
		return toArray(new byte[len], 0, len);
	}


	public short[] toArray(short[] b, int off, int len) throws IOException {
		if (!targetTypeList[0].equals(Short.class)) throw new ClassCastException("cannot cast " + targetTypeList[0].getSimpleName() + " to " + b.getClass().getSimpleName() + ". You have to cast the pointer first.");
		__dna__block.readFully(__dna__address, b, off, len);
		return b;
	}

	public short[] toShortArray(int len)
			throws IOException {
		return toArray(new short[len], 0, len);
	}


	public int[] toArray(int[] b, int off, int len) throws IOException {
		if (!targetTypeList[0].equals(Integer.class)) throw new ClassCastException("cannot cast " + targetTypeList[0].getSimpleName() + " to " + b.getClass().getSimpleName() + ". You have to cast the pointer first.");
		__dna__block.readFully(__dna__address, b, off, len);
		return b;
	}

	public int[] toIntArray(int len)
			throws IOException {
		return toArray(new int[len], 0, len);
	}


	public long[] toArray(long[] b, int off, int len) throws IOException {
		if (!targetTypeList[0].equals(Long.class)) throw new ClassCastException("cannot cast " + targetTypeList[0].getSimpleName() + " to " + b.getClass().getSimpleName() + ". You have to cast the pointer first.");
		__dna__block.readFully(__dna__address, b, off, len);
		return b;
	}

	public long[] toLongArray(int len)
			throws IOException {
		return toArray(new long[len], 0, len);
	}


	public long[] toArrayInt64(long[] b, int off, int len) throws IOException {
		if (!targetTypeList[0].equals(int64.class)) throw new ClassCastException("cannot cast " + targetTypeList[0].getSimpleName() + " to " + b.getClass().getSimpleName() + ". You have to cast the pointer first.");
		__dna__block.readFullyInt64(__dna__address, b, off, len);
		return b;
	}

	public long[] toInt64Array(int len)
			throws IOException {
		return toArray(new long[len], 0, len);
	}


	public float[] toArray(float[] b, int off, int len) throws IOException {
		if (!targetTypeList[0].equals(Float.class)) throw new ClassCastException("cannot cast " + targetTypeList[0].getSimpleName() + " to " + b.getClass().getSimpleName() + ". You have to cast the pointer first.");
		__dna__block.readFully(__dna__address, b, off, len);
		return b;
	}

	public float[] toFloatArray(int len)
			throws IOException {
		return toArray(new float[len], 0, len);
	}


	public double[] toArray(double[] b, int off, int len) throws IOException {
		if (!targetTypeList[0].equals(Double.class)) throw new ClassCastException("cannot cast " + targetTypeList[0].getSimpleName() + " to " + b.getClass().getSimpleName() + ". You have to cast the pointer first.");
		__dna__block.readFully(__dna__address, b, off, len);
		return b;
	}
	
	public double[] toDoubleArray(int len)
			throws IOException {
		return toArray(new double[len], 0, len);
	}

	
	/* ************************************************** */
	//                    PROTECTED
	/* ************************************************** */


	@SuppressWarnings({ "unchecked", "rawtypes" })
	protected T getDNAFacet(long targetAddress) throws IOException {
		try {
			if (targetTypeList[0].equals(DNAPointer.class)) {
				long address = __dna__block.readLong(targetAddress);
				return (T) new DNAPointer(address, Arrays.copyOfRange(targetTypeList, 1, targetTypeList.length), __dna__blockMap);
			} else {
				return (T) DNAFacet.__dna__newInstance((Class<? extends DNAFacet>) targetTypeList[0], targetAddress, __dna__blockMap);
			}
		} catch (InstantiationException | IllegalAccessException
				| IllegalArgumentException | InvocationTargetException | NoSuchMethodException | SecurityException e) {
			throw new IOException(e);
		}
	}



	@SuppressWarnings("unchecked")
	protected T getScalar(long address) throws IOException {
		Object result = null;
		
		Class<?> type = targetTypeList[0];
		
		if (type.equals(Byte.class) || type.equals(byte.class)) {
			result = __dna__block.readByte(address);
		} else if (type.equals(Short.class) || type.equals(short.class)) {
			result = __dna__block.readShort(address);
		} else if (type.equals(Integer.class) || type.equals(int.class)) {
			result = __dna__block.readInt(address);
		} else if (type.equals(Long.class) || type.equals(long.class)) {
			result = __dna__block.readLong(address);
		} else if (type.equals(int64.class)) {
			result = __dna__block.readInt64(address);
		} else if (type.equals(Float.class) || type.equals(float.class)) {
			result = __dna__block.readFloat(address);
		} else if (type.equals(Double.class) || type.equals(double.class)) {
			result = __dna__block.readDouble(address);
		} else {
			throw new ClassCastException("unrecognized scalar type: " + type.getName());
		}
		return (T)result;
	}

	

	public DNAPointerMutable<T> mutable() {
		return new DNAPointerMutable<T>(this);
	}

}
