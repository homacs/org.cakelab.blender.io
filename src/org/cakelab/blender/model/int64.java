package org.cakelab.blender.model;

/**
 * This is kind of a hack to provide pointers with 
 * type information in case of int64 scalars since it 
 * has to use a different read method than in case of 
 * a long type.
 * TODO: find another solution
 * @author homac
 */
@DNATypeInfo(size=8)
public class int64 {
	public long v;
}
