package org.cakelab.blender.io.dna;

/**
 * 
 * @author homac
 *
 */
public class DNAType {

	String name;
	short size;

	public DNAType(String name, short size) {
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
