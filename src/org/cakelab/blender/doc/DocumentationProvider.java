package org.cakelab.blender.doc;

import org.cakelab.blender.file.dna.BlendField;
import org.cakelab.blender.file.dna.BlendStruct;

public interface DocumentationProvider {
	public String getStructDoc(String struct);
	public String getFieldDoc(String struct, String field);
	
}
