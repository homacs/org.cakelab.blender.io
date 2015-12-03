package org.cakelab.blender.doc;

import org.cakelab.blender.file.dna.BlendField;
import org.cakelab.blender.file.dna.BlendStruct;

public interface DocumentationProvider {
	public String getStructDoc(BlendStruct struct);
	
	public String getFieldDoc(BlendStruct struct, BlendField field);
	
}
