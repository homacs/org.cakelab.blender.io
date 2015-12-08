package org.cakelab.blender.model;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;



/**
 * This annotation is used for classes
 * derived from {@link DNAFacet}. It provides necessary
 * runtime meta data such as the memory footprint of
 * a the corresponding C struct. This is for example used
 * in the method {@link DNAFacet#__dna__sizeof(Class)}.
 * 
 * @author homac
 *
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface DNATypeInfo {
	long size32();
	long size64();
}
