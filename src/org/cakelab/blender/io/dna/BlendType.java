package org.cakelab.blender.io.dna;


public class BlendType {

	String name;
	short size;

	public BlendType(String name, short size) {
		this.name = name;
		this.size = size;
	}

	public String getName() {
		return name;
	}
	
	public short getSize() {
		return size;
	}
	
}
