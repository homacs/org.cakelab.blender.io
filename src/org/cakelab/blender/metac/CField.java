package org.cakelab.blender.metac;


/**
 * Stores type information for a field of a struct.
 * @see CMetaModel
 * @author homac
 *
 */
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
