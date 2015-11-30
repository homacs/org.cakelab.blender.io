package org.cakelab.blender.generator.code;
public class GField extends CodeGenerator {

	protected String signature;
	protected String comment;
	protected String modifiers;
	protected String type;
	protected String name;

	public GField(String modifiers, String type, String name, String comment) {
		super(1);
		this.modifiers = modifiers;
		this.type = type;
		this.name = name;
		this.signature = modifiers + " " + type + " " + name;
		this.comment = comment;
	}

	public String toString(String indent) {
		String result = "";
		if (comment != null) result += indent + "/** " + comment + " */" + NL;
		result += indent + signature + ';' + NL;
		return result;
	}

	@Override
	public void reset() {
	}

	public String getComment() {
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
