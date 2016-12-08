package org.cakelab.blender.nio;

import java.io.IOException;
import java.lang.reflect.Array;
import java.nio.ReadOnlyBufferException;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.Iterator;

import org.cakelab.blender.io.Encoding;
import org.cakelab.blender.io.block.Block;
import org.cakelab.blender.io.block.BlockTable;


/**
 * This is the facade class for fixed length arrays. 
 * <p>
 * Arrays provide common array functionality and conversion between
 * underlying data and corresponding Java arrays 
 * (see various toArray and fromArray methods).
 * </p>
 * <p>
 * Since arrays in C are interchangable with pointers, the class inherits 
 * the capabilities of {@link CPointer}. This way, an array can 
 * always be assigned to a pointer variable. To turn a pointer in 
 * an instance of {@link CArrayFacade} use its {@link CPointer#toCArrayFacade(int)}.
 * </p>
 * <h3>Array Types</h3>
 * <p>
 * Arrays have a <em>component type</em> and an <em>elementary type</em>. This is important
 * when dealing with multi-dimensional arrays.
 * The elementary type is the most basic type contained in the array. If
 * the array is an array of arrays of int (i.e. CArrayFacade&lt;CArrayFacade&lt;int&gt;&gt;), 
 * then the elementary type is 'int' but it's component type
 * is CArrayFacade&lt;int&gt;. In case the array has just one dimension, the
 * component type and the elementary type are the same.
 * </p>
 * <p>
 * Arrays need runtime information about their component type. 
 * If the component type is an array or a pointer then the array 
 * needs the information about those contained types, too. Thus, arrays are 
 * instanciated with a list of types as type specification, where each element in the type 
 * list corresponds to a component type of the component type.
 * </p>
 * <h4>Detailed Example:</h4>
 * The following code snippet denotes an array of arrays of 
 * pointers on integers.
 * <pre>
 * CArrayFacade&lt;CArrayFacade&lt;CPointer&lt;Integer&gt;&gt;&gt; array;
 * </pre>
 * To instantiate such an array, we need to specify the list of
 * types for each component. 
 * <pre>
 * Class[] typeList = new Class[]{
 * 		CArrayFacade.class,
 * 		CArrayFacade.class,
 * 		CPointer.class,
 * 		Integer.class
 * };
 * </pre>
 * <p>
 * You will note, that we just read out the template parameters 
 * and put them in an array. The array also needs to know the length 
 * of each dimension beforehand. Thus, we create another array which holds
 * the length for each dimension. Let's say we want an array which 
 * corresponds to the C type declaration <code>int* array[2][8]</code>.
 * </p>
 * <pre>
 * int[] dimensions = new int[]{2,8};
 * </pre>
 * <p>
 * Finally we can create an instance of that array (given that we 
 * have an open blender file and a blockTable and the address of that array).
 * </p>
 * <pre>
 * array = new CArrayFacade&lt;CArrayFacade&lt;CPointer&lt;Integer&gt;&gt;&gt;(
 * 		address,
 * 		typeList,
 * 		dimensions,
 * 		blockTable
 * 	);
 * </pre>
 * <p>
 * Remember, that arrays are just facades and the actual data is stored in
 * a block of the blender file. Thus, the address (first parameter of 
 * the constructor) is either received from {@link CFacade#__io__addressof(long[])} or
 * from a pointer or by allocating a new block.
 * </p>
 * <p>
 * To allocate a block for an entirely new array, refer to the block 
 * allocation methods in either {@link org.cakelab.blender.utils.BlenderFactoryBase}
 * or the derived class {@link BlenderFactory} in the generated code 
 * or even directly to {@link BlenderFile} and {@link BlockTable}.
 * </p>
 * 
 * @author homac
 *
 * @param <T> Component type of the array.
 */
public class CArrayFacade<T> extends CPointer<T> implements Iterable<T>{

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

	/**
	 * Copy constructor.
	 * 
	 * @param other
	 */
	public CArrayFacade(CArrayFacade<T> other) {
		super(other);
		this.targetTypeList = other.targetTypeList;
		this.dimensions = other.dimensions;
		this.componentSize = other.componentSize;
	}
	
	/**
	 * This is the constructor to attach an array facade to existing 
	 * data in a block of a blender file.
	 * 
	 * <p>
	 * To allocate a block for an entirely new array, refer to the block 
	 * allocation methods in either {@link org.cakelab.blender.utils.BlenderFactoryBase}
	 * or the derived class {@link BlenderFactory} in the generated code.
	 * </p>
	 * 
	 * @param baseAddress virtual start address of the array (file specific).
	 * @param targetTypeList Type specification of the component type (see class documentation).
	 * @param dimensions Length of each dimension (see class documentation)
	 * @param __blockTable Block table of the associated blender file.
	 */
	public CArrayFacade(long baseAddress, Class<?>[] targetTypeList, int[] dimensions, Block block, BlockTable __blockTable) {
		super(baseAddress, Arrays.copyOfRange(targetTypeList, dimensions.length-1, targetTypeList.length), block, __blockTable);
		this.targetTypeList = targetTypeList;
		this.dimensions = dimensions;
		this.componentSize = calcComponentSize(targetTypeList[dimensions.length-1]);
	}
	
	/**
	 * @return Length of the array (number of elements).
	 */
	public int length() {
		return dimensions[0];
	}

	/**
	 * Delivers the native size of this array considering
	 * its architecture specific encoding.
	 * 
	 * @return size of this array in bytes
	 */
	public long sizeof() {
		return length() * componentSize;
	}

	/**
	 * Returns the address of the element at the given index in this array.
	 * 
	 * @param index of the element in this array wich address is calculated.
	 * @return the address of the element at the given index in this array.
	 */
	long getAddress(int index) {
		return __io__address + (index * componentSize);
	}
	
	/**
	 * Returns the element with the given 'index'.
	 * 
	 * @param index
	 * @return array[index]
	 * @throws IOException
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public T get(int index) throws IOException {
		long address = getAddress(index);
		if (dimensions.length > 1) {
			// array of arrays
			assert(targetTypeList[0].equals(CArrayFacade.class));
			return (T) new CArrayFacade<T>(
					address,
					Arrays.copyOfRange(targetTypeList, 1, targetTypeList.length), 
					Arrays.copyOfRange(dimensions, 1, dimensions.length), 
					__io__block,
					__io__blockTable);
		} else if (targetTypeList[0].equals(CPointer.class)) {
			// array of pointers
			long pointerAddress = __io__block.readLong(address);
			Class<?>[] type = Arrays.copyOfRange(targetTypeList, 1, targetTypeList.length);
			Block block = __io__blockTable.getBlock(pointerAddress, type);
			return (T) new CPointer(pointerAddress, type, block, __io__blockTable);
		} else if (isPrimitive(targetTypeList[0])) {
			return getScalar(address);
		} else {
			return getCFacade(address);
		}
	}

	/**
	 * Set the value of the array element with given 'index'.
	 * I.e. <code>array[index] = value;</code>
	 * @param index index of the array element.
	 * @param value New value for that element.
	 * @throws IOException
	 */
	public void set(int index, T value) throws IOException {
		long address = getAddress(index);
		super.__set(address, value);
	}


	
	/**
	 * Converts the underlying data into a Java array with
	 * component type T.
	 * @return
	 * @throws IOException
	 */
	@SuppressWarnings("unchecked")
	public T[] toArray() throws IOException {
		Object array = Array.newInstance(targetTypeList[0], length());
		for (int i = 0; i < length(); i++) {
			Array.set(array, i, get(i));
		}
		return (T[]) array;
	}

	/**
	 * Copyies all elements of the given array to this array.
	 * @param data
	 * @throws IOException
	 */
	public void fromArray(T[] data) throws IOException {
		for (int i = 0; i < length(); i++) {
			set(i, data[i]);
		}
	}

	/**
	 * Converts the array into a string.
	 * <b>Warning:</b> This method assumes, that the string 
	 * is null terminated.
	 * @return
	 * @throws IOException
	 */
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
	
	/**
	 * Converts the underlying data into an array of the given native type.
	 * The created array has the same length as this array.
	 * @return New Java array with a copy of the elements of this array.
	 * @throws IOException
	 */
	public byte[] toByteArray() throws IOException {
		return super.toByteArray(length());
	}
	
	/**
	 * Copies all values of the given Java array to this array.
	 * <h3>Precoditions</h3>
	 * <li>Source array 'data' and this array must have 
	 * equivalent component types. 
	 * </li>
	 * Equivalent types means, that the component type of the CArrayFacade
	 * has the corresponding class type of the native component type of the 
	 * array 'data'. So, if 'data' is of type short[] than this array must 
	 * have component type Short.
	 * 
	 * @param data Array of data to be copied to this array.
	 * @throws IOException
	 */
	public void fromByteArray(byte[] data) throws IOException {
		super.fromArray(data);
	}
	
	/**
	 * Converts the underlying data into an array of the given native type.
	 * The created array has the same length as this array.
	 * @return New Java array with a copy of the elements of this array.
	 * @throws IOException
	 */
	public short[] toShortArray() throws IOException {
		return super.toShortArray(length());
	}
	
	/**
	 * Converts the underlying data into an array of the given native type.
	 * The created array has the same length as this array.
	 * @return New Java array with a copy of the elements of this array.
	 * @throws IOException
	 */
	public int[] toIntArray() throws IOException {
		return super.toIntArray(length());
	}
	
	/**
	 * Converts the underlying data into an array of the given native type.
	 * The created array has the same length as this array.
	 * @return New Java array with a copy of the elements of this array.
	 * @throws IOException
	 */
	public long[] toLongArray() throws IOException {
		return super.toLongArray(length());
	}
	
	/**
	 * Converts the underlying data into an array of the given native type.
	 * The created array has the same length as this array.
	 * @return New Java array with a copy of the elements of this array.
	 * @throws IOException
	 */
	public long[] toInt64Array() throws IOException {
		return super.toInt64Array(length());
	}
	
	/**
	 * Converts the underlying data into an array of the given native type.
	 * The created array has the same length as this array.
	 * @return New Java array with a copy of the elements of this array.
	 * @throws IOException
	 */
	public float[] toFloatArray() throws IOException {
		return super.toFloatArray(length());
	}
	
	/**
	 * Converts the underlying data into an array of the given native type.
	 * The created array has the same length as this array.
	 * @return New Java array with a copy of the elements of this array.
	 * @throws IOException
	 */
	public double[] toDoubleArray() throws IOException {
		return super.toDoubleArray(length());
	}
	

	/**
	 * Calculates the number of elements contained in the array over all dimensions (in case of multi-dimensional arrays).
	 * @param elementaryType
	 * @return
	 */
	private long calcComponentSize(Class<?> elementaryType) {
		long size = __io__sizeof(elementaryType);
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
	 * Considers multi-dimensional arrays.
	 * @param elementaryType The elementary type, i.e. the scalar type.
	 * @param dimensions
	 * @param encoding
	 * @return
	 */
	public static long __io__sizeof(Class<?> elementaryType, int[] dimensions, Encoding encoding) {
		long size = CFacade.__io__sizeof(elementaryType, encoding.getAddressWidth());
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
	 * @param sourceArray
	 * @throws IOException
	 */
	@SuppressWarnings("unchecked")
	@Override
	protected void __io__generic__copy(CFacade sourceArray) throws IOException {
		assert(sourceArray instanceof CArrayFacade);
		CArrayFacade<T> source = (CArrayFacade<T>)sourceArray;
		
		for (int i = 0; i < source.length(); i++) {
			this.set(i, source.get(i));
		}
	}

	
	/**
	 * For convienient iteration over this array's elements.
	 * <pre>
	 * for (T elem : array) {
	 *   // ..
	 * }
	 * </pre>
	 * Does not support deletion.
	 */
	@Override
	public Iterator<T> iterator() {
		return new CArrayFacadeIterator<T>(this);
	}

	static class CArrayFacadeIterator<T> extends CArrayFacade<T> implements Iterator<T> {

		private int current;

		public CArrayFacadeIterator(CArrayFacade<T> dnaArray) {
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
