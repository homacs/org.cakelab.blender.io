package org.cakelab.blender.model;

/**
 * This is kind of a hack to provide pointers and array 
 * types with type information in case of int64 scalars since it 
 * has to use a different read method than in case of 
 * a long type.
 * TODO: find another solution for int64
 * @author homac
 */
@DNATypeInfo(size32=8, size64=8)
public class int64 {
	public long v;
}
