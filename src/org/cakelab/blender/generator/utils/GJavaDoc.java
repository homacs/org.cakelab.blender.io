package org.cakelab.blender.generator.utils;

public class GJavaDoc extends GComment {
	
	private ClassGenerator classgen;

	public GJavaDoc(ClassGenerator classgen) {
		super(GComment.Type.JavaDoc);
		this.classgen = classgen;
	}
	
	public void addSeeTag(Class<?> clazz) {
		classgen.addImport(clazz);
		addSeeTag(clazz.getSimpleName());
	}

	public void addSeeTag(String reference) {
		appendln("@see " + reference);
	}

	
	
}
