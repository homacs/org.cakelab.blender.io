package org.cakelab.blender.io.dna;

import org.cakelab.blender.io.dna.internal.StructDNA;
import org.cakelab.blender.io.dna.internal.StructDNA.Struct;
import org.cakelab.blender.io.dna.internal.StructDNA.Struct.Field;

/**
 * DNAModel is a more convinient interface to meta data provided in
 * {@link StructDNA}. Information is the same, just access to it is
 * easier.
 * 
 * @author homac
 *
 */
public class DNAModel {
	private StructDNA dna;
	private DNAType[] types;
	private DNAStruct[] structs;

	public DNAModel(StructDNA dna) {
		this.dna = dna;
		this.types = new DNAType[dna.types_len];
		for (int i = 0; i < types.length; i++) {
			types[i] = new DNAType(dna.types[i], dna.type_lengths[i]);
		}
		
		structs = new DNAStruct[dna.structs_len];
		for (int i = 0; i < dna.structs.length; i++) {
			Struct s = dna.structs[i];
			structs[i] = createStruct(i, s);
		}
	}

	private DNAStruct createStruct(int sdnaIndex, Struct s) {
		DNAType type = getType(s.type);
		DNAStruct struct = new DNAStruct(sdnaIndex, type, s.fields_len);
		for (int fieldNo = 0; fieldNo < s.fields_len; fieldNo++) {
			Field field = s.fields[fieldNo];
			struct.set(fieldNo, createField(struct, fieldNo, field));
		}
		return struct;
	}

	private DNAField createField(DNAStruct struct, int fieldNo, Field field) {
		DNAField f = new DNAField(fieldNo, dna.names[field.name], getType(field.type));
		return f;
	}

	public DNAType getType(short typeIndex) {
		return types[typeIndex];
	}
	
	public DNAStruct getStruct(int sdnaIndex) {
		return structs[sdnaIndex];
	}
	
	public DNAStruct getStruct(String structName) {
		for (DNAStruct struct : structs) {
			if (struct.type.name.equals(structName)) {
				return struct;
			}
		}
		return null;
	}

	public DNAStruct[] getStructs() {
		return structs;
	}


}
