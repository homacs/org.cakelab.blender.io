package org.cakelab.blender.model;
import java.io.IOException;
import java.lang.reflect.Array;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;

import org.cakelab.blender.file.block.BlockMap;


public class DNAPointer<T> extends DNAFacet {
	private Class<?>[] targetTypes;
	private long targetAddress;
	private long targetSize;
	

	@SuppressWarnings("unchecked")
	public DNAPointer(long targetAddress, Class<?>[] targetTypes, BlockMap memory) {
		super(targetAddress, memory);
		this.targetAddress = targetAddress;
		this.targetTypes = (Class<T>[]) targetTypes;
		this.targetSize = __dna__sizeof(targetTypes[0]);
	}
	
	/**
	 * @return Copy of the value the pointer points to.
	 * @throws IOException
	 */
	public T get() throws IOException {
		return __get(targetAddress);
	}
	
	public T __get(long address) throws IOException {
		if (targetSize == 0) throw new IOException("Target type is unspecified (i.e. void*). Use cast() to specify its type first.");
		if (isPrimitive(targetTypes[0])) {
			return getScalar(address);
		} else if (targetTypes[0].isArray()){
			// TODO: array abstraction (not used in dna)
			throw new IOException("unexpected case where pointer points on array.");
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


	@SuppressWarnings({ "unchecked", "rawtypes" })
	protected T getDNAFacet(long targetAddress) throws IOException {
		try {
			if (targetTypes[0].equals(DNAPointer.class)) {
				long address = __dna__block.readLong(targetAddress);
				return (T) new DNAPointer(address, Arrays.copyOfRange(targetTypes, 1, targetTypes.length), __dna__blockMap);
			} else {
				return (T) DNAFacet.__dna__newInstance((Class<? extends DNAFacet>) targetTypes[0], targetAddress, __dna__blockMap);
			}
		} catch (InstantiationException | IllegalAccessException
				| IllegalArgumentException | InvocationTargetException | NoSuchMethodException | SecurityException e) {
			throw new IOException(e);
		}
	}



	@SuppressWarnings("unchecked")
	protected T getScalar(long address) throws IOException {
		Object result = null;
		
		Class<?> type = targetTypes[0];
		
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
			throw new IOException("unrecognized scalar type: " + type.getName());
		}
		return (T)result;
	}


	/**
	 * 
	 * @return
	 * @throws IOException address the pointer points to
	 */
	public long getAddress() throws IOException {
		return targetAddress;
	}
	
	public boolean isNull() {
		return targetAddress == 0;
	}

	/**
	 * Tells whether the pointer points to actual data or in 
	 * a region of memory, which was not saved in the file.
	 * 
	 * Thus, it is a bit more than a null pointer check and it 
	 * is more expensive performance wise.
	 * @return true, if you can access the memory
	 */
	public boolean isValid() {
		return !isNull() && (__dna__block != null && __dna__block.contains(targetAddress)); 
	}
	
	/**
	 * C pointer arithmetics. 
	 * <pre>
	 * int* p;
	 * p++; // equivalent to p.next();
	 * </pre>
	 */
	public void next() {
		targetAddress += targetSize;
	}

	/**
	 * resets the pointer to its original address.
	 */
	public void reset() {
		targetAddress = __dna__address;
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
		return new DNAPointer<U>(targetAddress, new Class<?>[]{type}, __dna__blockMap);
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
		return new DNAPointer<U>(targetAddress, types, __dna__blockMap);
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
		T[] arr = (T[])Array.newInstance(targetTypes[0], length);
		long address = targetAddress;
		for (int i = 0; i < length; i++) {
			arr[i] = __get(address);
			address += targetSize;
		}
		return arr;
	}

}
