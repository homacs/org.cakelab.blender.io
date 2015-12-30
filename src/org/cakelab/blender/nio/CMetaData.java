package org.cakelab.blender.nio;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;



/**
 * This annotation is used for classes
 * derived from {@link CFacade}. 
 * <p>
 * The annotation provides necessary runtime meta data 
 * such as the memory footprint of the corresponding C type. 
 * This is for example used in the method {@link CFacade#__io__sizeof(Class)}.
 * </p>
 * @author homac
 *
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface CMetaData {
	long size32();
	long size64();
}
