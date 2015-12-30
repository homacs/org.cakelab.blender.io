package org.cakelab.blender.io.dna;

/**
 * Represents a field (member variable) of a struct.
 * 
 * @author homac
 *
 */
public class DNAField {

	int index;
	String signatureName;
	DNAType type;
	String name;

	public DNAField(int index, String name, DNAType type) {
		this.index = index;
		this.signatureName = name;
		this.name = removeSignatureFromName(name);
		this.type = type;
	}

	private String removeSignatureFromName(String name) {
		return name.replace("*", "").replaceAll("\\[.*\\]", "");
	}

	public DNAType getType() {
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
