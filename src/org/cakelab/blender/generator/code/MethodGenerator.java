package org.cakelab.blender.generator.code;

public abstract class MethodGenerator extends FieldVisitor {
	protected static final String ARCH64_IDENTIFICATION_BOOLEAN = "(__dna__pointersize == 8)";


	protected ClassGenerator classgen;
	protected CodeSection content;

	public MethodGenerator(ClassGenerator classGenerator) {
		super(classGenerator, 0);
		content = new CodeSection(0);
		this.classgen = classGenerator;
	}

	@Override
	public void reset() {
		content.reset();
	}

	protected CodeSection appendln(String line) {
		return content.appendln(line);
	}

}
