package org.cakelab.blender.nio;


import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.ByteOrder;

import org.cakelab.blender.io.Encoding;
import org.cakelab.blender.io.block.Block;
import org.cakelab.blender.io.block.BlockTable;

/**
 * {@link CFacade} is the base class of all complex types
 * (structs). A facet derived from this class, provides a 
 * type safe interface to access (get/set) data in native 
 * memory. This class holds the actual reference on the 
 * memory region and provides methods to access it as well as
 * helper methods to deal with type casts etc..
 * 
 * @author homac
 *
 */
public abstract class CFacade {
	
	protected long __io__address;
	protected BlockTable __io__blockTable;
	protected Block __io__block;
	protected int __io__arch_index;
	protected int __io__pointersize;
	
	public CFacade(long __address, BlockTable __blockTable) {
		this.__io__address = __address;
		this.__io__block = __blockTable.getBlock(__address);
		this.__io__blockTable = __blockTable;
		this.__io__pointersize = __blockTable.getEncoding().getAddressWidth();
		this.__io__arch_index = __blockTable.getEncoding().getAddressWidth() == Encoding.ADDR_WIDTH_32BIT ? 0 : 1;
	}
	
	public CFacade(CFacade other, long targetAddress) {
		this.__io__address = targetAddress;
		this.__io__block = other.__io__block;
		this.__io__blockTable = other.__io__blockTable;
		this.__io__arch_index = other.__io__arch_index;
	}


	/**
	 * @return block associated with the address of the object
	 * represented by this facet.
	 */
	protected Block __io__getBlock() {
		// TODO: really need to determine block again?
		return __io__blockTable.getBlock(__io__address);
	}
	

	/**
	 * This method returns the size of the C type which corresponds
	 * to the given Java type according to the type mapping of Java Blend.
	 * @param type 
	 * @return sizeof(ctype)
	 */
	public long __io__sizeof(Class<?> type) {
		return __io__sizeof(type, __io__pointersize);
	}

	/**
	 * This method returns the size of the C type which corresponds
	 * to the given Java type according to the type mapping of Java Blend.
	 * @param type 
	 * @return sizeof(ctype)
	 */
	public static long __io__sizeof(Class<?> type, int addressWidth) {
		if (type.equals(CPointer.class)) {
			return addressWidth;
		} else if (type.equals(CArrayFacade.class)) {
			throw new IllegalArgumentException("no generic runtime type information for array types available");
		} else if (__io__subclassof(type, CFacade.class)){
			CMetaData typeInfo = type.getAnnotation(CMetaData.class);
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
	public static <T extends CFacade> CPointer<T> __io__addressof(T object) {
		return new CPointer<T>(object.__io__address, new Class[]{object.getClass()}, object.__io__blockTable);
	}

	/**
	 * This method creates a void pointer on the field identified by 
	 * 'fieldDescriptor' of the struct represented by the facet (see static fields __DNA__FIELD__&lt;fieldname&gt; in the generated facets).
	 * <p>The returned pointer has to be casted (by means of {@link CPointer#cast(Class)} and similar methods)
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
	public CPointer<Object> __io__addressof(long[] fieldDescriptor) {
		return new CPointer<Object>(this.__io__address + fieldDescriptor[__io__arch_index], new Class[]{Object.class}, this.__io__blockTable);
	}

	
	
	/**
	 * Tests whether the given type is a subclass of superType.
	 * @param type type to be tested.
	 * @param superType expected base class of the given type.
	 * @return true if true.
	 */
	public static boolean __io__subclassof(Class<?> type,
			Class<?> superType) {
		Class<?> superClass = type.getSuperclass();
		if (superClass == null || superClass.equals(Object.class)) return false;
		else if (superClass.equals(superType)) return true;
		else return __io__subclassof(superClass, superType);
	}

	/**
	 * Tests whether the given object is an instance of class clazz
	 * or some subclass of class clazz.
	 * @param type type to be tested.
	 * @param superType expected base class of the given type.
	 * @return true if true.
	 */
	public static boolean __io__instanceof(CFacade object, Class<?> clazz) {
		Class<?> testClass = object.getClass();
		if (testClass.equals(clazz)) return true;
		return __io__subclassof(testClass, clazz);
	}

	/**
	 * Creates a new facet instance of the given type.
	 * @param type The type of facet to instantiate.
	 * @param address The associated address for the instantiated facet.
	 * @param blockTable the global block map of the associated file.
	 * @return new facet instance of the given type
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 * @throws IllegalArgumentException
	 * @throws InvocationTargetException
	 * @throws NoSuchMethodException
	 * @throws SecurityException
	 */
	public static CFacade __io__newInstance(Class<? extends CFacade> type, long address,
			BlockTable blockTable) throws InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException {
		// TODO: ZZZ cache constructors?
		Constructor<?> constructor = type.getDeclaredConstructor(long.class, BlockTable.class);
		return (CFacade) constructor.newInstance(address, blockTable);
	}

	/** Generates a string representation of the given CFacade considering all getter methods. 
	 * Mainly for debugging purposes. */
	public String __io__toString() {
		StringBuffer result = new StringBuffer();
		for (Method method : getClass().getDeclaredMethods()) {
			if (method.getName().startsWith("get") && method.getParameterTypes().length == 0) {
				Object obj;
				try {
					obj = method.invoke(this);
					if (obj instanceof CFacade) {
						obj = ((CFacade)obj).__io__address;
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
	 * @return address (virtual) of the given object.
	 */
	protected boolean __io__equals(CFacade facet, long address) {
		return facet.__io__block == this.__io__block 
				&& facet.__io__address == address;
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
	protected static void __io__native__copy(Block targetBlock, long targetAddress, CFacade source) throws IOException {
		// just copy memory
		assert(targetBlock.contains(targetAddress));
		
		int size;
		if (source instanceof CArrayFacade) {
			size = (int) ((CArrayFacade<?>)source).sizeof();
		} else {
			size = (int) source.__io__sizeof(source.getClass());
		}
		// TODO: ZZZ support direct memory copy (without buffer) or reuse buffer
		byte[] buffer = new byte[size];
		source.__io__block.readFully(source.__io__address, buffer);
		targetBlock.writeFully(targetAddress, buffer);
	}

	/**
	 * <p>
	 * This method does a highlevel copy of the given source to this object.
	 * The method is used in case a lowlevel copy (see {@link #__io__native__copy(Block, long, CFacade)})
	 * is not possible due to different encodings of the underlying blocks 
	 * (see {@link #__io__same__encoding(CFacade, CFacade)}.
	 * </p>
	 * <h3>Mandatory Preconditions</h3>
	 * <ul>
	 * <li>This object and the source object are of exactly the same type.</li>
	 * <li>The source is not a CPointer.</li>
	 * <ul>
	 * 
	 * <p>
	 * The method is overridden by CArrayFacade.
	 * </p>
	 * 
	 * 
	 * @param source An object derived from CFacade or CArrayFacade, but not CPointer. 
	 * @param nla_tracks 
	 * @throws IOException
	 */
	protected void __io__generic__copy (CFacade source) throws IOException {
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
	protected static void __io__generic__copy (CFacade target, CFacade source) throws IOException {
		target.__io__generic__copy(source);
	}
	
	
	/**
	 * Tests whether the underlying data blocks of both facets use the
	 * same encoding (byte order and address length).
	 * @param facetA
	 * @param facetB
	 * @return
	 */
	protected boolean __io__same__encoding(CFacade facetA, CFacade facetB) {
		return facetA.__io__pointersize == facetB.__io__pointersize
											&& 
				facetA.__io__byteorder() == facetB.__io__byteorder();
	}

	/**
	 * @return byte order used by the underlying data block.
	 */
	private ByteOrder __io__byteorder() {
		return __io__blockTable.getEncoding().getByteOrder();
	}

	
	
	
}
