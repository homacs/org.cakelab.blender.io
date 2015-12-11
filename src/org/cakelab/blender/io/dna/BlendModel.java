package org.cakelab.blender.io.dna;

import org.cakelab.blender.io.dna.internal.StructDNA;
import org.cakelab.blender.io.dna.internal.StructDNA.Struct;
import org.cakelab.blender.io.dna.internal.StructDNA.Struct.Field;


public class BlendModel {
	private StructDNA dna;
	private BlendType[] types;
	private BlendStruct[] structs;

	public BlendModel(StructDNA dna) {
		this.dna = dna;
		this.types = new BlendType[dna.types_len];
		for (int i = 0; i < types.length; i++) {
			types[i] = new BlendType(dna.types[i], dna.type_lengths[i]);
		}
		
		structs = new BlendStruct[dna.structs_len];
		for (int i = 0; i < dna.structs.length; i++) {
			Struct s = dna.structs[i];
			structs[i] = createStruct(i, s);
		}
	}

	private BlendStruct createStruct(int sdnaIndex, Struct s) {
		BlendType type = getType(s.type);
		BlendStruct struct = new BlendStruct(sdnaIndex, type, s.fields_len);
		for (int fieldNo = 0; fieldNo < s.fields_len; fieldNo++) {
			Field field = s.fields[fieldNo];
			struct.set(fieldNo, createField(struct, fieldNo, field));
		}
		return struct;
	}

	private BlendField createField(BlendStruct struct, int fieldNo, Field field) {
		BlendField f = new BlendField(fieldNo, dna.names[field.name], getType(field.type));
		return f;
	}

	public BlendType getType(short typeIndex) {
		return types[typeIndex];
	}
	
	public BlendStruct getStruct(int sdnaIndex) {
		return structs[sdnaIndex];
	}
	
	public BlendStruct getStruct(String structName) {
		for (BlendStruct struct : structs) {
			if (struct.type.name.equals(structName)) {
				return struct;
			}
		}
		return null;
	}

	public BlendStruct[] getStructs() {
		return structs;
	}


}
