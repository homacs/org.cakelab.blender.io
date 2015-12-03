package org.cakelab.blender.model;


import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

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
	protected long __dna__address;
	protected BlockMap __dna__blockMap;
	protected Block __dna__block;
	
	public DNAFacet(long __address, BlockMap __blockMap) {
		this.__dna__address = __address;
		this.__dna__block = __blockMap.getBlock(__address);
		this.__dna__blockMap = __blockMap;
	}
	
	public DNAFacet(DNAFacet other, long targetAddress) {
		this.__dna__address = targetAddress;
		this.__dna__block = other.__dna__block;
		this.__dna__blockMap = other.__dna__blockMap;
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
		if (type.equals(DNAPointer.class)) {
			return __dna__pointersize();
		} else if (type.equals(DNAArray.class)) {
			throw new IllegalArgumentException("no generic runtime type information for array types available");
		} else if (__dna__subclassof(type, DNAFacet.class)){
			DNATypeInfo typeInfo = type.getAnnotation(DNATypeInfo.class);
			return typeInfo.size();
		} else if (type.equals(byte.class) || type.equals(Byte.class)) {
			return 1;
		} else if (type.equals(short.class) || type.equals(Short.class)) {
			return 2;
		} else if (type.equals(int.class) || type.equals(Integer.class)) {
			return 4;
		} else if (type.equals(long.class) || type.equals(Long.class)) {
			return __dna__pointersize();
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
	 * @return pointer size according to the meta data read from blender file.
	 */
	long __dna__pointersize() {
		return __dna__block.data.getPointerSize();
	}

	public <T extends DNAFacet> DNAPointer<T> __dna__addressof(T object) {
		return new DNAPointer<T>(object.__dna__address, new Class[]{object.getClass()}, object.__dna__blockMap);
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
	 * @param type The facet type to instantiate.
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

	/** Generates a JSON like representation of the given DNAFacet considering all getter methods. 
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


	
	
}
