package org.cakelab.blender.model;

public interface Constraint<T> {
	boolean satisfied(T obj);
}
