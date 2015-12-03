package org.cakelab.blender.model;

import java.io.IOException;
import java.lang.reflect.Array;
import java.nio.ReadOnlyBufferException;
import java.util.Arrays;
import java.util.Iterator;

import org.cakelab.blender.file.block.BlockMap;


/**
 * This is the facet class for fixed length arrays. 
 * <p>
 * Since arrays in C are interchangable with pointers, it inherits 
 * the capabilities of {@link DNAPointer}. This way, an array can 
 * always be assigned to a pointer variable. 
 * </p>
 * <p>
 * Arrays provide common array functionality and conversion of the 
 * underlying data to actual Java arrays.
 * </p>
 * @author homac
 *
 * @param <T> Component type of the array.
 */
public class DNAArray<T> extends DNAPointer<T> implements Iterable<T>{

	protected Class<?>[] targetTypeList;
	/**
	 * This is the first type in the type list, which is not an array.
	 * Thus, it is the type of the basic elements which are actually 
	 * stored in the array. Since this can be a pointer, we need the 
	 * information on what type of data the pointers point.
	 */
	protected Class<?> componentType;
	
	
	/**
	 * This list contains the length of each dimension of the array.
	 */
	protected int[] dimensions;
	/** 
	 * Element size is the size in bytes of one element of this array.
	 * This considers, that an element can be an array as well and 
	 * accounts the length of that (those) array(s). Thus, elementSize is
	 * exactly the size of one step from one element to the next.
	 */
	protected long componentSize;

	
	public DNAArray(DNAArray<T> other) {
		super(other);
		this.targetTypeList = other.targetTypeList;
		this.componentType = other.componentType;
		this.dimensions = other.dimensions;
		this.componentSize = other.componentSize;
	}
	
	public DNAArray(long baseAddress, Class<?>[] targetTypeList, int[] dimensions, BlockMap __blockMap) {
		super(baseAddress, Arrays.copyOfRange(targetTypeList, dimensions.length-1, targetTypeList.length), __blockMap);
		this.targetTypeList = targetTypeList;
		this.componentType = targetTypeList[dimensions.length-1];
		this.dimensions = dimensions;
		this.componentSize = calcElementSize();
	}
	
	
	public int length() {
		return dimensions[0];
	}

	public long sizeof() {
		return length() * componentSize;
	}

	long getAddress(int index) {
		return __dna__address + (index * componentSize);
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public T get(int index) throws IOException {
		long address = getAddress(index);
		if (dimensions.length > 1) {
			// array of arrays
			assert(targetTypeList[0].isArray());
			return (T) new DNAArray<T>(
					address,
					Arrays.copyOfRange(targetTypeList, 1, targetTypeList.length), 
					Arrays.copyOfRange(dimensions, 1, dimensions.length), 
					__dna__blockMap);
		} else if (componentType.equals(DNAPointer.class)) {
			// array of pointers
			long pointerAddress = __dna__block.readLong(address);
			return (T) new DNAPointer(pointerAddress, Arrays.copyOfRange(targetTypeList, 1, targetTypeList.length), __dna__blockMap);
		} else if (isPrimitive(componentType)) {
			return getScalar(address);
		} else {
			return getDNAFacet(address);
		}
	}

	@SuppressWarnings("unchecked")
	public T[] toArray() throws IOException {
		Object array = Array.newInstance(componentType, length());
		for (int i = 0; i < length(); i++) {
			Array.set(array, i, get(i));
		}
		return (T[]) array;
	}
	
	public String asString() throws IOException {
		if ((componentType.equals(byte.class) || componentType.equals(Byte.class)) && dimensions.length == 1) {
			byte[] bytes = toByteArray();
			int len = 0;
			for (; len < bytes.length && bytes[len] != 0; len++);
			return new String(bytes, 0, len);
		} else {
			throw new IllegalArgumentException("component type of array has to be byte to allow conversion in string");
		}
	}
	
	public byte[] toByteArray() throws IOException {
		return super.toByteArray(length());
	}
	
	public short[] toShortArray() throws IOException {
		return super.toShortArray(length());
	}
	
	public int[] toIntArray() throws IOException {
		return super.toIntArray(length());
	}
	
	public long[] toLongArray() throws IOException {
		return super.toLongArray(length());
	}
	
	public long[] toInt64Array() throws IOException {
		return super.toInt64Array(length());
	}
	
	public float[] toFloatArray() throws IOException {
		return super.toFloatArray(length());
	}
	
	public double[] toDoubleArray() throws IOException {
		return super.toDoubleArray(length());
	}
	
	
	
	
	private long calcElementSize() {
		long size = __dna__sizeof(componentType);
		if (dimensions.length > 1) {
			// array of arrays
			long length = dimensions[1];
			for (int i = 2; i < dimensions.length; i++) {
				length *= dimensions[i];
			}
			size = size * length;
		}
		return size;
	}

	@Override
	public Iterator<T> iterator() {
		return new DNAArrayIterator<T>(this);
	}

	
	
	
	static class DNAArrayIterator<T> extends DNAArray<T> implements Iterator<T> {

		private int current;

		public DNAArrayIterator(DNAArray<T> dnaArray) {
			super(dnaArray);
			current = 0;
		}

		@Override
		public boolean hasNext() {
			return current < length();
		}

		@Override
		public T next() {
			try {
				return get(current++);
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}


		/**
		 * Not supported!
		 * 
		 * @throws ReadOnlyBufferException
		 */
		@Deprecated
		@Override
		public void remove() throws ReadOnlyBufferException {
			throw new ReadOnlyBufferException();
		}

		public long getCurrentAddress() {
			return getAddress(current);
		}

	}
}
