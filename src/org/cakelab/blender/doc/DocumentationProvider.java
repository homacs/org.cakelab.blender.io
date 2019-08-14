package org.cakelab.blender.doc;

/**
 * Interface used by the code generator to retrieve documentation 
 * for a given struct.
 * 
 * @author homac
 *
 */
public interface DocumentationProvider {
	public String getStructDoc(String struct);
	public String getFieldDoc(String struct, String field);
	
}
