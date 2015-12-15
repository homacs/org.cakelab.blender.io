package org.cakelab.blender.nio;

/**
 * This class has been introduced to carry type information over
 * to the runtime model and allow instantiation of template classes
 * with int64 as template parameter (e.g. {@link CArrayFacade} and {@link CPointer}).
 * 
 * It is kind of a hack but I haven't found a better solution yet.
 * 
 * TODO: ZZZ find another solution for int64
 * @author homac
 */
@CMetaData(size32=8, size64=8)
public class int64 {
}
