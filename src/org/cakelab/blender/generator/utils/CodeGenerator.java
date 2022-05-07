package org.cakelab.blender.generator.utils;

import org.cakelab.blender.typemap.NameMapping;

public abstract class CodeGenerator extends NameMapping {
	protected static final String NL = "\n";
	protected static final String TAB = "\t";

	
	protected String indent = "";
	
	protected CodeGenerator(int initialIndent) {
		indent(initialIndent);
	}
	
	public CodeGenerator(GCodeSection other) {
		this.indent = other.indent;
	}

	public void indent(int n) {
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
	

	
	public abstract void reset();
}
