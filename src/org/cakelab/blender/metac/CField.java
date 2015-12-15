package org.cakelab.blender.metac;



public class CField {
	private String name;
	private CType ctype;

	public CField(String name, CType ctype) {
		this.name = name;
		this.ctype = ctype;
	}

	public CType getType() {
		return ctype;
	}

	public String getName() {
		return name;
	}

}
