package org.cakelab.blender.generator.type;

import java.util.ArrayList;

import org.cakelab.blender.io.dna.BlendStruct;

public class CStruct extends CType {
	int sdnaIndex;
	
	private ArrayList<CField> fields = new ArrayList<CField>();

	public CStruct(BlendStruct bstruct) {
		super(bstruct.getType().getName(), CTypeType.TYPE_STRUCT);
		this.sdnaIndex = bstruct.getIndex();
	}

	public void addField(CField cfield) {
		fields.add(cfield);
	}

	public ArrayList<CField> getFields() {
		return fields;
	}

	public int getSdnaIndex() {
		return sdnaIndex;
	}
}
