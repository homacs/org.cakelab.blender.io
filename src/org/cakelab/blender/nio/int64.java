package org.cakelab.blender.nio;

/**
 * This class has been introduced to carry type information for 
 * int64 types over to the runtime model and allow instantiation 
 * of template classes with int64 as template parameter 
 * (e.g. {@link CArrayFacade} and {@link CPointer}).
 * 
 * @author homac
 */
@CMetaData(size32=8, size64=8)
public class int64 {
}
