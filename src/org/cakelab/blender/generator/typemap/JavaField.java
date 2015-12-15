package org.cakelab.blender.generator.typemap;

import org.cakelab.blender.metac.CField;


public class JavaField {

	private JavaType type;
	private CField field;


	public JavaField(CField field) {
		this.field = field;
		
		type = new JavaType(field.getType());
	}


	public JavaType getType() {
		return type;
	}

	public String getName() {
		return field.getName();
	}



}
