package org.cakelab.blender.generator.utils;


public class GComment extends GCodeSection {
	public static final GComment EMPTY = new GComment() {
		@Override
		public String toString(int indent) {
			return "";
		}
	};
	private Type type = Type.Unspecified;
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
		JavaDoc,
		/**
		 * Unspecified. Comment is handled like a simple code section.
		 */
		Unspecified
	}
	
	
	public GComment(Type type) {
		super(0);
		this.type = type;
		reset();
	}

	public GComment(GComment other) {
		super(other);
		this.type = other.type;
		this.prefix = other.prefix;
		this.finished = other.finished;
	}

	GComment() {
		super(0);
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
