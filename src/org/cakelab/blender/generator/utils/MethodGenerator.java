package org.cakelab.blender.generator.utils;

public abstract class MethodGenerator extends FieldVisitor {
	protected static final String ARCH64_TEST = "(__io__pointersize == 8)";


	protected ClassGenerator classgen;
	protected GMethod content;

	public MethodGenerator(ClassGenerator classGenerator) {
		super(classGenerator, 0);
		content = new GMethod(0);
		this.classgen = classGenerator;
	}

	@Override
	public void reset() {
		content.reset();
	}

	protected GCodeSection appendln(String line) {
		return content.appendln(line);
	}

}
