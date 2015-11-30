package org.cakelab.blender.model;


import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.cakelab.blender.file.block.Block;
import org.cakelab.blender.file.block.BlockMap;

public class DNAFacet {
	protected long __dna__address;
	protected BlockMap __dna__blockMap;
	protected Block __dna__block;
	
	public DNAFacet(long __address, BlockMap __blockMap) {
		this.__dna__address = __address;
		this.__dna__block = __blockMap.getBlock(__address);
		this.__dna__blockMap = __blockMap;
	}
	
	
	protected Block __dna__getBlock() {
		return __dna__blockMap.getBlock(__dna__address);
	}
	

	public long __dna__sizeof(Class<?> targetType) {
		if (targetType.equals(DNAPointer.class)) {
			return __dna__pointersize();
		} else if (targetType.equals(DNAArray.class)) {
			throw new IllegalArgumentException("no generic runtime type information for array types available");
		} else if (__dna__subclassof(targetType, DNAFacet.class)){
			DNATypeInfo typeInfo = targetType.getAnnotation(DNATypeInfo.class);
			return typeInfo.size();
		} else if (targetType.equals(byte.class) || targetType.equals(Byte.class)) {
			return 1;
		} else if (targetType.equals(short.class) || targetType.equals(Short.class)) {
			return 2;
		} else if (targetType.equals(int.class) || targetType.equals(Integer.class)) {
			return 4;
		} else if (targetType.equals(long.class) || targetType.equals(Long.class)) {
			return __dna__pointersize();
		} else if (targetType.equals(int64.class)) {
			return 8;
		} else if (targetType.equals(float.class) || targetType.equals(Float.class)) {
			return 4;
		} else if (targetType.equals(double.class) || targetType.equals(Double.class)) {
			return 8;
		} else if (targetType.equals(Object.class)) {
			/* 
			 * special case: this type of pointer cannot support pointer 
			 * arithmetics, same way as in C.
			 */
			return 0;
		} else {
			throw new IllegalArgumentException("missing size information for type '" + targetType.getSimpleName() + "'");
		}
	}

	private long __dna__pointersize() {
		return __dna__block.data.getPointerSize();
	}


	private static boolean __dna__subclassof(Class<?> type,
			Class<DNAFacet> superType) {
		Class<?> superClass = type.getSuperclass();
		if (superClass == null || superClass.equals(Object.class)) return false;
		else if (superClass.equals(superType)) return true;
		else return __dna__subclassof(superClass, superType);
	}


	public static DNAFacet __dna__newInstance(Class<? extends DNAFacet> type, long address,
			BlockMap blockMap) throws InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException {
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
