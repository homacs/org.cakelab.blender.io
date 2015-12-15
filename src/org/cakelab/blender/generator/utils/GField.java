package org.cakelab.blender.generator.utils;
public class GField extends CodeGenerator {

	protected String signature;
	protected GComment comment;
	protected String modifiers;
	protected String type;
	protected String name;
	protected String initialiser;

	public GField(String modifiers, String type, String name, String initialiser, GComment javadoc) {
		super(1);
		this.modifiers = modifiers;
		this.type = type;
		this.name = name;
		this.initialiser = initialiser;
		this.signature = modifiers + " " + type + " " + name;
		if (initialiser != null) {
			this.signature += " = " + initialiser;
		}
		this.comment = javadoc;
	}

	public String toString(String indent) {
		String result = "";
		if (comment != null) result += comment.toString(1);
		result += indent + signature + ';' + NL;
		return result;
	}

	@Override
	public void reset() {
	}

	public GComment getComment() {
		return comment;
	}

	public String getModifiers() {
		return modifiers;
	}

	public String getType() {
		return type;
	}

	public String getName() {
		return name;
	}

}
