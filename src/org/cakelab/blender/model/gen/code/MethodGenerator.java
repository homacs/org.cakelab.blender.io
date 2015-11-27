package org.cakelab.blender.model.gen.code;

public class MethodGenerator extends CodeGenerator {

	protected ClassGenerator classgen;
	protected StringBuffer content = new StringBuffer();

	public MethodGenerator(ClassGenerator classGenerator) {
		super(+1);
		this.classgen = classGenerator;
	}

	@Override
	public void reset() {
		content.setLength(0);
	}

	protected StringBuffer appendln(String line) {
		return content.append(indent).append(line).append("\n");
	}

}
