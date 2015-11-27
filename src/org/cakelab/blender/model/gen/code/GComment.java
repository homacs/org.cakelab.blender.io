package org.cakelab.blender.model.gen.code;

import org.cakelab.blender.model.gen.CodeSection;

public class GComment extends CodeSection {
	private Type type;
	private String prefix;
	private boolean finished;

	public enum Type {
		/**
		 * Set of lines starting with double slash.
		 */
		SingleLine,
		/**
		 * Set of lines in between slash star and star slash.
		 */
		Multiline,
		/**
		 * JavaDoc conform multi-line comment. 
		 */
		JavaDoc
	}
	
	
	public GComment(Type type) {
		super(0);
		this.type = type;
		reset();
	}

	@Override
	public void reset() {
		super.reset();
		finished = false;
		switch (type) {
		case JavaDoc:
			append("/**");
			prefix = " * ";
			break;
		case Multiline:
			append("/*");
			prefix = " * ";
			break;
		case SingleLine:
			prefix = "// ";
			append(prefix);
			break;
		default:
			break;
		}
	}

	@Override
	public GComment appendln(String line) {
		return (GComment) super.appendln(line);
	}

	@Override
	public GComment appendln() {
		GComment result = (GComment) super.appendln();
		if (!finished) append(prefix);
		return result;
	}

	@Override
	public GComment append(String text) {
		return (GComment) super.append(text);
	}

	@Override
	public String toString(int indent) {
		finished = true;
		switch (type) {
		case JavaDoc:
		case Multiline:
			currentLine.setLength(0);
			super.appendln(" */");
			break;
		case SingleLine:
		default:
			break;
		}
		return super.toString(indent);
	}

	
}
