package org.cakelab.blender.generator.utils;

/** 
 * Contains code and documentation of a generated method.
 * 
 * @author homac
 *
 */
public class GMethod extends GCodeSection {

	GComment comment;
	
	public GMethod(GMethod other) {
		super(other);
		comment = other.comment;
	}

	
	public GMethod(int indent) {
		super(indent);
	}

	public void setComment(GComment comment) {
		this.comment = comment;
	}

	public String toString(int indent) {
		if (comment != null) {
			return comment.toString(indent) + super.toString(indent);
		} else {
			return super.toString(indent);
		}
	}
}
