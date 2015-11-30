package org.cakelab.blender.model;

import java.io.IOException;
import java.lang.reflect.Array;
import java.util.Arrays;

import org.cakelab.blender.file.block.BlockMap;

public class DNAArray<T> extends DNAPointer<T> {

	private Class<?>[] targetTypeList;
	/**
	 * This is the first type in the type list, which is not an array.
	 * Thus, it is the type of the basic elements which are actually 
	 * stored in the array. Since this can be a pointer, we need the 
	 * information on what type of data the pointers point.
	 */
	private Class<?> componentType;
	private int[] dimensions;
	/** 
	 * Element size is the size in bytes of one element of this array.
	 * This considers, that an element can be an array as well and 
	 * accounts the length of that (those) array(s). Thus, elementSize is
	 * exactly the size of one step from one element to the next.
	 */
	private long elementSize;

	
	public DNAArray(DNAArray<T> other) {
		super(other);
		this.targetTypeList = other.targetTypeList;
		this.componentType = other.componentType;
		this.dimensions = other.dimensions;
		this.elementSize = other.elementSize;
	}
	
	public DNAArray(long baseAddress, Class<?>[] targetTypeList, int[] dimensions, BlockMap __blockMap) {
		super(baseAddress, Arrays.copyOfRange(targetTypeList, dimensions.length-1, targetTypeList.length), __blockMap);
		this.targetTypeList = targetTypeList;
		this.componentType = targetTypeList[dimensions.length-1];
		this.dimensions = dimensions;
		this.elementSize = calcElementSize();
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
	
	
	public int length() {
		return dimensions[0];
	}

	public long sizeof() {
		return length() * elementSize;
	}

	private long getAddress(int index) {
		return __dna__address + (index * elementSize);
	}
	
}
