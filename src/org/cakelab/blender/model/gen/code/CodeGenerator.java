package org.cakelab.blender.model.gen.code;

public abstract class CodeGenerator {
	protected static final String NL = "\n";
	protected static final String TAB = "\t";

	
	protected String indent = "";
	
	protected CodeGenerator(int initialIndent) {
		indent(initialIndent);
	}
	
	protected void indent(int n) {
		if (n > 0) {
			for (int i = 0; i < n; i++) indent += TAB; 
		} else {
			indent = indent.substring((-n) * TAB.length()); 
		}
	}
	
	protected String getIndentString(int indentionLevel) {
		String result = "";
		for (int i = 0; i < indentionLevel; i++) result += TAB; 
		return result;
	}
	

	
	protected static String toCamelCase(String name) {
		return "" + Character.toTitleCase(name.charAt(0)) + name.substring(1);
	}

	protected static String toFirstLowerCase(String name) {
		return "" + Character.toLowerCase(name.charAt(0)) + name.substring(1);
	}

	public abstract void reset();
}
