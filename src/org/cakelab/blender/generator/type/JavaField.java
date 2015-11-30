package org.cakelab.blender.generator.type;

import org.cakelab.blender.file.dna.BlendField;


public class JavaField {

	private String comment;
	private JavaType type;
	private String name;
	private BlendField field;
	private CType ctype;


	public JavaField(BlendField field) {
		this.field = field;
		
		ctype = new CType(field.getType().getName(), field.getType().getSize(), field.getSignatureName());
		
		comment = generateJavaComment(field);
		type = new JavaType(ctype);
		name = getJavaName(field);
	}




	/** Generate a comment which at least explains 
	 * the type of the field */
	private String generateJavaComment(BlendField field) {
		return "/** original: " + field.getType().getName() + "(size="+ field.getType().getSize() + ")" + " " + field.getSignatureName() + "; */";
	}


	/** Remove any type specifiers from the fields name (e.g. * or []). */
	private String getJavaName(BlendField field) {
		String name = field.getSignatureName();
		name = name.replaceAll("\\*", "");
		name = name.replaceAll("\\[.*\\]$", "");
		name = name.replaceAll("[()]", "");
		return name;
	}

	
	public String getComment() {
		return comment;
	}

	public JavaType getType() {
		return type;
	}

	public String getName() {
		return name;
	}



	public int getNumElems() {
		String name = field.getSignatureName();
		int start = name.indexOf('[');
		if (start < 0) return -1;
		
		int size = 1;
		int end;
		do {
			end = name.indexOf(']');
			String substr = name.substring(start + 1, end);
			size *= Integer.valueOf(substr.trim());
			name = name.substring(end+1);
			start = name.indexOf('[');
		} while (start >= 0);
		return size;
	}


}
