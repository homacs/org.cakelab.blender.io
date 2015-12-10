package org.cakelab.blender.doc;


public interface DocumentationProvider {
	public String getStructDoc(String struct);
	public String getFieldDoc(String struct, String field);
	
}
