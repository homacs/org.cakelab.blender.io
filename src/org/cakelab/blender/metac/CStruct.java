package org.cakelab.blender.metac;

import java.util.ArrayList;

import org.cakelab.blender.io.dna.DNAStruct;

/**
 * Stores type information of a struct.
 * 
 * @see CMetaModel
 * @author homac
 *
 */
public class CStruct extends CType {
	int sdnaIndex;
	
	private ArrayList<CField> fields = new ArrayList<CField>();

	public CStruct(DNAStruct bstruct) {
		super(bstruct.getType().getName(), CKind.TYPE_STRUCT);
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
