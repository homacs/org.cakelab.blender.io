package org.cakelab.blender.file.dna;



public class BlendStruct {

	int index;
	BlendType type;
	BlendField[] fields;
	
	public BlendStruct(int sdnaIndex, BlendType type, short fields_len) {
		this.index = sdnaIndex;
		this.type = type;
		fields = new BlendField[fields_len];
	}

	public void set(int i, BlendField f) {
		fields[i] = f;
	}

	public BlendType getType() {
		return type;
	}

	public BlendField[] getFields() {
		return fields;
	}

	public int getIndex() {
		return index;
	}

	
}
