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
	
	private ArrayList<CField> fields = new ArrayList<>();

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

	/** warning: lookup not optimised! Linear search!*/
	public CField getField(String name) {
		// FIXME: optimise: lookup for CStruct fields
		for (CField f : fields) {
			if (f.getName().equals(name)) {
				return f;
			}
		}
		return null;
	}
}
