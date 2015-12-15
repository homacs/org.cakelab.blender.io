package org.cakelab.blender.io.dna;


/**
 * DNAStruct is not {@link StructDNA}!
 * 
 * DNAStruct is a part of {@link DNAModel} providing 
 * a more convinient interface to struct descriptions in {@link StructDNA}.
 * 
 * @author homac
 *
 */
public class DNAStruct {

	int index;
	DNAType type;
	DNAField[] fields;
	
	public DNAStruct(int sdnaIndex, DNAType type, short fields_len) {
		this.index = sdnaIndex;
		this.type = type;
		fields = new DNAField[fields_len];
	}

	public void set(int i, DNAField f) {
		fields[i] = f;
	}

	public DNAType getType() {
		return type;
	}

	public DNAField[] getFields() {
		return fields;
	}

	public int getIndex() {
		return index;
	}

	
}
