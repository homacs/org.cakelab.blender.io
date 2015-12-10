package org.cakelab.blender.model;


import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.ByteOrder;

import org.cakelab.blender.file.Encoding;
import org.cakelab.blender.file.block.Block;
import org.cakelab.blender.file.block.BlockMap;

/**
 * {@link DNAFacet} is the base class of all complex types
 * (structs). A facet derived from this class, provides a 
 * type safe interface to access (get/set) data in native 
 * memory. This class holds the actual reference on the 
 * memory region and provides methods to access it as well as
 * helper methods to deal with type casts etc..
 * 
 * @author homac
 *
 */
public abstract class DNAFacet {
	
	// TODO: need constants of all member offsets for dereferencing.
	
	
	protected long __dna__address;
	protected BlockMap __dna__blockMap;
	protected Block __dna__block;
	protected int __dna__arch_index;
	protected int __dna__pointersize;
	
	public DNAFacet(long __address, BlockMap __blockMap) {
		this.__dna__address = __address;
		this.__dna__block = __blockMap.getBlock(__address);
		this.__dna__blockMap = __blockMap;
		this.__dna__pointersize = __blockMap.getEncoding().getAddressWidth();
		this.__dna__arch_index = __blockMap.getEncoding().getAddressWidth() == Encoding.ADDR_WIDTH_32BIT ? 0 : 1;
	}
	
	public DNAFacet(DNAFacet other, long targetAddress) {
		this.__dna__address = targetAddress;
		this.__dna__block = other.__dna__block;
		this.__dna__blockMap = other.__dna__blockMap;
		this.__dna__arch_index = other.__dna__arch_index;
	}


	/**
	 * @return block associated with the address of the object
	 * represented by this facet.
	 */
	protected Block __dna__getBlock() {
		return __dna__blockMap.getBlock(__dna__address);
	}
	

	/**
	 * This method returns the size of the C type which corresponds
	 * to the given Java type according to the type mapping of Java Blend.
	 * @param type 
	 * @return sizeof(ctype)
	 */
	public long __dna__sizeof(Class<?> type) {
		return __dna__sizeof(type, __dna__pointersize);
	}

	/**
	 * This method returns the size of the C type which corresponds
	 * to the given Java type according to the type mapping of Java Blend.
	 * @param type 
	 * @return sizeof(ctype)
	 */
	public static long __dna__sizeof(Class<?> type, int addressWidth) {
		if (type.equals(DNAPointer.class)) {
			return addressWidth;
		} else if (type.equals(DNAArray.class)) {
			throw new IllegalArgumentException("no generic runtime type information for array types available");
		} else if (__dna__subclassof(type, DNAFacet.class)){
			DNATypeInfo typeInfo = type.getAnnotation(DNATypeInfo.class);
			return addressWidth == 8 ? typeInfo.size64() : typeInfo.size32();
		} else if (type.equals(byte.class) || type.equals(Byte.class)) {
			return 1;
		} else if (type.equals(short.class) || type.equals(Short.class)) {
			return 2;
		} else if (type.equals(int.class) || type.equals(Integer.class)) {
			return 4;
		} else if (type.equals(long.class) || type.equals(Long.class)) {
			return addressWidth;
		} else if (type.equals(int64.class)) {
			return 8;
		} else if (type.equals(float.class) || type.equals(Float.class)) {
			return 4;
		} else if (type.equals(double.class) || type.equals(Double.class)) {
			return 8;
		} else if (type.equals(Object.class)) {
			/* 
			 * special case: this type of pointer cannot support pointer 
			 * arithmetics, same way as in C.
			 */
			return 0;
		} else {
			throw new IllegalArgumentException("missing size information for type '" + type.getSimpleName() + "'");
		}
	}

	/**
	 * This method creates a pointer on the given facet.
	 * @param object
	 * @return
	 */
	public static <T extends DNAFacet> DNAPointer<T> __dna__addressof(T object) {
		return new DNAPointer<T>(object.__dna__address, new Class[]{object.getClass()}, object.__dna__blockMap);
	}

	/**
	 * This method creates a void pointer on the field identified by 
	 * 'fieldDescriptor' of the struct represented by the facet (see static fields __DNA__FIELD__&lt;fieldname&gt; in the generated facets).
	 * <p>The returned pointer has to be casted (by means of {@link DNAPointer#cast(Class)} and similar methods)
	 * in order to use pointer arithmetics on it. We had the choice 
	 * here to either carry all type information for every field over
	 * to the runtime model (and waste a lot of performance) or deal 
	 * with the risk of having void pointers. Since it is very rare
	 * that pointers on fields are needed, we decided to go with this approach.
	 * </p>
	 * <b>This method is highly dangerous</b> because we do not check,
	 * whether the field descriptor actually belongs to the facet and it is
	 * directly interpreted as offset to the structs base address.
	 * </p>
	 * @param fieldDescriptor The __DNA__FIELD__&lt;fieldname&gt; descriptor 
	 *        of the field, whos address will be determined.
	 * @return
	 */
	public DNAPointer<Object> __dna__addressof(long[] fieldDescriptor) {
		return new DNAPointer<Object>(this.__dna__address + fieldDescriptor[__dna__arch_index], new Class[]{Object.class}, this.__dna__blockMap);
	}

	
	
	/**
	 * Tests whether the given type is a subclass of superType.
	 * @param type type to be tested.
	 * @param superType expected base class of the given type.
	 * @return true if true.
	 */
	static boolean __dna__subclassof(Class<?> type,
			Class<?> superType) {
		Class<?> superClass = type.getSuperclass();
		if (superClass == null || superClass.equals(Object.class)) return false;
		else if (superClass.equals(superType)) return true;
		else return __dna__subclassof(superClass, superType);
	}

	/**
	 * Tests whether the given object is an instance of class clazz
	 * or some subclass of class clazz.
	 * @param type type to be tested.
	 * @param superType expected base class of the given type.
	 * @return true if true.
	 */
	static boolean __dna__instanceof(DNAFacet object, Class<?> clazz) {
		Class<?> testClass = object.getClass();
		if (testClass.equals(clazz)) return true;
		return __dna__subclassof(testClass, clazz);
	}

	/**
	 * Creates a new facet instance of the given type.
	 * @param type The type of facet to instantiate.
	 * @param address The associated address for the instantiated facet.
	 * @param blockMap the global block map of the associated file.
	 * @return new facet instance of the given type
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 * @throws IllegalArgumentException
	 * @throws InvocationTargetException
	 * @throws NoSuchMethodException
	 * @throws SecurityException
	 */
	public static DNAFacet __dna__newInstance(Class<? extends DNAFacet> type, long address,
			BlockMap blockMap) throws InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException {
		// TODO: cache constructors
		Constructor<?> constructor = type.getDeclaredConstructor(long.class, BlockMap.class);
		return (DNAFacet) constructor.newInstance(address, blockMap);
	}

	/** Generates a string representation of the given DNAFacet considering all getter methods. 
	 * Mainly for debugging purposes. */
	public String __dna__toString() {
		StringBuffer result = new StringBuffer();
		for (Method method : getClass().getDeclaredMethods()) {
			if (method.getName().startsWith("get") && method.getParameterTypes().length == 0) {
				Object obj;
				try {
					obj = method.invoke(this);
					if (obj instanceof DNAFacet) {
						obj = ((DNAFacet)obj).__dna__address;
					}
					result.append(method.getName()).append(": ").append(obj.toString()).append("\n");
				} catch (IllegalAccessException | IllegalArgumentException
						| InvocationTargetException e) {
					result.append(method.getName()).append(": error: ").append(e.getMessage()).append("\n");
				}
			}
		}
		return result.toString();
	}

	/**
	 * @param facet
	 * @param address 
	 * @param __dna__block2 
	 * @return address (virtual) of the given object.
	 */
	protected boolean __dna__equals(DNAFacet facet, long address) {
		return facet.__dna__block == this.__dna__block 
				&& facet.__dna__address == address;
	}

	/**
	 * <p>
	 * This method performs a low level copy of the given object to
	 * the given target address in the target block. This requires, that the target
	 * <ul>
	 * 
	 * @param targetBlock Block, which will receive the written data.
	 * @param targetAddress Target address where the data will be written to. This address has to be 
	 *        in range of the given target block.
	 * @param source facet with the data to be copied to the given address.
	 * @throws IOException 
	 */
	protected static void __dna__native__copy(Block targetBlock, long targetAddress, DNAFacet source) throws IOException {
		// TODO: consider array
		// TODO: test
		// just copy memory
		assert(targetBlock.contains(targetAddress));
		
		int size;
		if (source instanceof DNAArray) {
			size = (int) ((DNAArray<?>)source).sizeof();
		} else {
			size = (int) source.__dna__sizeof(source.getClass());
		}
		// TODO: support direct memory copy (without buffer)
		// or at least reuse buffer
		byte[] buffer = new byte[size];
		source.__dna__block.readFully(source.__dna__address, buffer);
		targetBlock.writeFully(targetAddress, buffer);
	}

	/**
	 * <p>
	 * This method does a highlevel copy of the given source to this object.
	 * The method is used in case a lowlevel copy (see {@link #__dna__native__copy(Block, long, DNAFacet)})
	 * is not possible due to different encodings of the underlying blocks 
	 * (see {@link #__dna__same__encoding(DNAFacet, DNAFacet)}.
	 * </p>
	 * <h3>Mandatory Preconditions</h3>
	 * <ul>
	 * <li>This object and the source object are of exactly the same type.</li>
	 * <li>The source is not a DNAPointer.</li>
	 * <ul>
	 * 
	 * <p>
	 * The method is overridden by DNAArray.
	 * </p>
	 * 
	 * 
	 * @param source An object derived from DNAFacet or DNAArray, but not DNAPointer. 
	 * @param nla_tracks 
	 * @throws IOException
	 */
	protected void __dna__generic__copy (DNAFacet source) throws IOException {
		// deserialise source and serialise to this
		// use getter and setter methods of both
		Class<?> clazz = source.getClass();
		
		try {
			for (Method getter : clazz.getDeclaredMethods()) {
				if (getter.getName().startsWith("get")) {
					String setterName = getter.getName().replaceFirst("g", "s");
						Method setter = clazz.getDeclaredMethod(setterName, getter.getReturnType());
						setter.invoke(this, getter.invoke(source));
				}
			}
		} catch (NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
			throw new IOException("unexpected case", e);
		}
	}
	
	/**
	 * 
	 * @param target
	 * @param source
	 * @throws IOException
	 */
	protected static void __dna__generic__copy (DNAFacet target, DNAFacet source) throws IOException {
		target.__dna__generic__copy(source);
	}
	
	
	/**
	 * Tests whether the underlying data blocks of both facets use the
	 * same encoding (byte order and address length).
	 * @param facetA
	 * @param facetB
	 * @return
	 */
	protected boolean __dna__same__encoding(DNAFacet facetA, DNAFacet facetB) {
		return facetA.__dna__pointersize == facetB.__dna__pointersize
											&& 
				facetA.__dna__byteorder() == facetB.__dna__byteorder();
	}

	/**
	 * @return byte order used by the underlying data block.
	 */
	private ByteOrder __dna__byteorder() {
		return __dna__blockMap.getEncoding().getByteOrder();
	}

	
	
	
}
