package org.cakelab.blender.generator.type;

import java.util.ArrayList;

import org.cakelab.blender.file.dna.BlendStruct;

public class CStruct extends CType {
	private ArrayList<CField> fields = new ArrayList<CField>();

	public CStruct(BlendStruct bstruct) {
		super(bstruct.getType().getName(), CTypeType.TYPE_STRUCT);
	}

	public void addField(CField cfield) {
		fields.add(cfield);
	}

	public ArrayList<CField> getFields() {
		return fields;
	}
}
