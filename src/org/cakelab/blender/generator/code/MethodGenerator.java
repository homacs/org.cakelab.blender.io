package org.cakelab.blender.generator.code;

public class MethodGenerator extends CodeGenerator {

	protected ClassGenerator classgen;
	protected CodeSection content;

	public MethodGenerator(ClassGenerator classGenerator) {
		super(0);
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
