package org.cakelab.blender.model;

import java.io.IOException;
import java.lang.reflect.Array;
import java.nio.ReadOnlyBufferException;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.Iterator;

import org.cakelab.blender.io.Encoding;
import org.cakelab.blender.io.block.BlockTable;


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
 * <p>
 * Arrays have a component type and an elementary type. This is important
 * when dealing with multidimensional arrays.
 * The elementary type is the most basic type contained in the array. If
 * the array is an array of arrays of int (i.e. DNAArray&lt;DNAArray&lt;int&gt;&gt;), 
 * then the <em>elementary</em> type is 'int' but it's <em>component type</em> 
 * is DNAArray&lt;int&gt;. In case the array has just one dimension, the
 * component type and the elementary type are the same.
 * </p>
 * <p>
 * Arrays need runtime information about their component type. 
 * If the component type is an array or a pointer then the array 
 * needs the information about their types, too. Thus, arrays are 
 * instanciated with a list of types, where each element in the type 
 * list corresponds to a component type of the component type.
 * </p>
 * <b>Example:</b>
 * The following code snippet denotes an array of arrays of 
 * pointers on integers.
 * <pre>
 * DNAArray&lt;DNAArray&lt;DNAPointer&lt;Integer&gt;&gt;&gt; array;
 * </pre>
 * To instantiate such an array, we need to specify the list of
 * types for each component. 
 * <pre>
 * Class[] typeList = new Class[]{
 * 							DNAArray.class,
 * 							DNAArray.class,
 * 							DNAPointer.class,
 * 							Integer.class
 * }
 * </pre>
 * <p>
 * You will note, that we just read out the template parameters 
 * and put them in an array. The array also needs to know the length 
 * of each dimension beforehand. Thus we create another array which holds
 * the length for each dimension. Let's say we want an array which 
 * corresponds to <code>int* array[2][8]</code>.
 * </p>
 * <pre>
 * int[] dimensions = new int[]{2,8};
 * </pre>
 * <p>
 * Finally we can create an instance of that array (given that we 
 * have an open blender file and a blockTable).
 * </p>
 * <pre>
 * array = new DNAArray&lt;DNAArray&lt;DNAPointer&lt;Integer&gt;&gt;&gt;(
 * 				address,
 * 				typeList,
 * 				dimensions,
 * 				blockTable
 * 			);
 * </pre>
 * <p>
 * Remember, that arrays are just facets and the actual data is stored in
 * a block of the blender file. Thus, the address (first parameter) is
 * either received from {@link DNAFacet#__dna__addressof(long[])} or
 * from a pointer or by allocating a new block.
 * </p>
 * 
 * @author homac
 *
 * @param <T> Component type of the array.
 */
public class DNAArray<T> extends DNAPointer<T> implements Iterable<T>{

	protected Class<?>[] targetTypeList;
	
	/**
	 * This list contains the length of each dimension of the array.
	 */
	protected int[] dimensions;
	/** 
	 * Component size is the size in bytes of one element of this array.
	 * This considers, that an element can be an array as well and 
	 * accounts the length of that (those) array(s). Thus, elementSize is
	 * exactly the size of one step from one element to the next.
	 */
	protected long componentSize;

	
	public DNAArray(DNAArray<T> other) {
		super(other);
		this.targetTypeList = other.targetTypeList;
		this.dimensions = other.dimensions;
		this.componentSize = other.componentSize;
	}
	
	public DNAArray(long baseAddress, Class<?>[] targetTypeList, int[] dimensions, BlockTable __blockTable) {
		super(baseAddress, Arrays.copyOfRange(targetTypeList, dimensions.length-1, targetTypeList.length), __blockTable);
		this.targetTypeList = targetTypeList;
		this.dimensions = dimensions;
		this.componentSize = calcComponentSize(targetTypeList[dimensions.length-1]);
	}
	
	
	public int length() {
		return dimensions[0];
	}

	/**
	 * delivers the actual native size of this array considering
	 * its architecture specific encoding.
	 * 
	 * @return size of this array in bytes
	 */
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
					__dna__blockTable);
		} else if (targetTypeList[0].equals(DNAPointer.class)) {
			// array of pointers
			long pointerAddress = __dna__block.readLong(address);
			return (T) new DNAPointer(pointerAddress, Arrays.copyOfRange(targetTypeList, 1, targetTypeList.length), __dna__blockTable);
		} else if (isPrimitive(targetTypeList[0])) {
			return getScalar(address);
		} else {
			return getDNAFacet(address);
		}
	}

	public void set(int index, T value) throws IOException {
		long address = getAddress(index);
		super.__set(address, value);
	}


	

	@SuppressWarnings("unchecked")
	public T[] toArray() throws IOException {
		Object array = Array.newInstance(targetTypeList[0], length());
		for (int i = 0; i < length(); i++) {
			Array.set(array, i, get(i));
		}
		return (T[]) array;
	}

	public void fromArray(T[] data) throws IOException {
		for (int i = 0; i < length(); i++) {
			set(i, data[i]);
		}
	}

	public String asString() throws IOException {
		if ((targetTypeList[0].equals(byte.class) || targetTypeList[0].equals(Byte.class)) && dimensions.length == 1) {
			byte[] bytes = toByteArray();
			int len = 0;
			for (; len < bytes.length && bytes[len] != 0; len++);
			return new String(bytes, 0, len);
		} else {
			throw new IllegalArgumentException("component type of array has to be byte to allow conversion to string. Consider a type cast.");
		}
	}

	/**
	 * Fills the underlying buffer with a null-terminated string
	 * from the given string using the default charset (see {@link Charset#defaultCharset()}).
	 * @param str string to write into the array.
	 * @throws IOException
	 */
	public void fromString(String str) throws IOException {
		fromString(str, Charset.defaultCharset(), true);
	}
	
	/**
	 * Fills the underlying buffer with the bytes of the given string str using the default charset (see {@link Charset#defaultCharset()}).
	 * The parameter addNullTermination controls, whether the method adds a 0 to terminate the string or not.
	 * @param str string to write into the array.
	 * @param addNullTermination Adds a '\0' if true.
	 * @throws IOException
	 */
	public void fromString(String str, boolean addNullTermination) throws IOException {
		fromString(str, Charset.defaultCharset(), addNullTermination);
	}
	
	
	/**
	 * Fills the underlying buffer with the bytes of the given string str using the given charset (see {@link Charset}).
	 * The parameter addNullTermination controls, whether the method adds a 0 to terminate the string or not.
	 * @param str string to write into the array.
	 * @param charset Charset to use when converting into a byte array.
	 * @param addNullTermination Adds a '\0' if true.
	 * @throws IOException
	 */
	@SuppressWarnings("unchecked")
	public void fromString(String str, Charset charset, boolean addNullTermination) throws IOException {
		if ((targetTypeList[0].equals(byte.class) || targetTypeList[0].equals(Byte.class)) && dimensions.length == 1) {
			byte[] bytes = str.getBytes(charset);
			super.fromArray(bytes, 0, bytes.length);
			if (addNullTermination) set(bytes.length, (T)Byte.valueOf((byte) 0));
		} else {
			throw new IllegalArgumentException("component type of array has to be byte to allow conversion from string. Consider a type cast.");
		}
	}
	
	public byte[] toByteArray() throws IOException {
		return super.toByteArray(length());
	}
	
	public void fromByteArray(byte[] data) throws IOException {
		super.fromArray(data);
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
	


	
	
	private long calcComponentSize(Class<?> elementaryType) {
		long size = __dna__sizeof(elementaryType);
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

	/** 
	 * Calculates the total size of an array based on the given parameters.
	 * @param elementaryType The elementary type, i.e. the scalar type.
	 * @param dimensions
	 * @param encoding
	 * @return
	 */
	public static long __dna__sizeof(Class<?> elementaryType, int[] dimensions, Encoding encoding) {
		long size = DNAFacet.__dna__sizeof(elementaryType, encoding.getAddressWidth());
		if (dimensions.length > 0) {
			// array of arrays
			long length = dimensions[0];
			for (int i = 1; i < dimensions.length; i++) {
				length *= dimensions[i];
			}
			size = size * length;
		}
		return size;
	}
	
	/**
	 * {@inheritDoc}
	 * 
	 * This is for arrays only. Behaviour is unspecified 
	 * if the given parameter source is not an array!
	 * 
	 * @param source
	 * @throws IOException
	 */
	@SuppressWarnings("unchecked")
	@Override
	protected void __dna__generic__copy(DNAFacet sourceArray) throws IOException {
		assert(sourceArray instanceof DNAArray);
		DNAArray<T> source = (DNAArray<T>)sourceArray;
		
		for (int i = 0; i < source.length(); i++) {
			this.set(i, source.get(i));
		}
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
