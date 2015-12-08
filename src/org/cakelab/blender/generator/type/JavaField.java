package org.cakelab.blender.generator.type;


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
