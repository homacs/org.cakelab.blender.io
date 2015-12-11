package org.cakelab.blender.io.dna;

public class BlendField {

	int index;
	String signatureName;
	BlendType type;
	String name;

	public BlendField(int index, String name, BlendType type) {
		this.index = index;
		this.signatureName = name;
		this.name = removeSignatureFromName(name);
		this.type = type;
	}

	private String removeSignatureFromName(String name) {
		return name.replace("*", "").replaceAll("\\[.*\\]", "");
	}

	public BlendType getType() {
		return type;
	}

	/**
	 * @return name including type specifications like * for pointers and [n] for arrays.
	 */
	public String getSignatureName() {
		return signatureName;
	}
	
	public String getSignature() {
		return type.name + " " + signatureName;
	}
	
	/**
	 * @return name of the field without type specifications such as * and [];
	 */
	public String getName() {
		return name;
	}

}
