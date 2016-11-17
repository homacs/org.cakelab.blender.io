package org.cakelab.blender.generator;

import java.io.IOException;

import org.cakelab.blender.generator.typemap.JavaType;
import org.cakelab.blender.generator.utils.ClassGenerator;
import org.cakelab.blender.generator.utils.FieldVisitor;
import org.cakelab.blender.generator.utils.GComment;
import org.cakelab.blender.generator.utils.HtmlEncoder;
import org.cakelab.blender.io.Encoding;
import org.cakelab.blender.metac.CField;
import org.cakelab.blender.metac.CType;
import org.cakelab.blender.nio.CPointer;


public class CFacadeFieldDescGenerator extends FieldVisitor {

	protected CFacadeFieldDescGenerator(ClassGenerator classGenerator) {
		super(classGenerator, 0);
	}

	@Override
	protected void visitArray(long offset32, long offset64, CField field, JavaType jtype) throws IOException {
		addConstField(field, offset32, offset64, field.getType(), jtype);
	}

	@Override
	protected void visitPointer(long offset32, long offset64, CField field, JavaType jtype) throws IOException {
		addConstField(field, offset32, offset64, field.getType(), jtype);
	}

	@Override
	protected void visitScalar(long offset32, long offset64, CField field, JavaType jtype) throws IOException {
		addConstField(field, offset32, offset64, field.getType(), jtype);
	}

	private void addConstField(CField field, long offset32, long offset64, CType ctype, JavaType jtype) throws IOException {
		GComment javadoc = new GComment(GComment.Type.JavaDoc);

		String classname = classgen.getClassName();
		String descrname = getFieldDescriptorName(field.getName());
		String pointerType = CPointer.class.getSimpleName() + getTemplateParameter(ctype, jtype);
		String typeList = getTypeList(jtype, ctype);
		javadoc.appendln();
		javadoc.appendln("Field descriptor (offset) for struct member '" + field.getName() + "'.");
		appendFieldDoc(javadoc);
		javadoc.appendln("<h3>Pointer Arithmetics</h3>");
		javadoc.appendln("<p>");
		javadoc.appendln("This is how you get a reference on the corresponding field in the struct:");
		javadoc.appendln("</p>");
		javadoc.appendln("<pre>");
		javadoc.appendln(
				HtmlEncoder.encode(
					  classname + " " + classname.toLowerCase() + " = ...;\n"
					+ CPointer.class.getSimpleName() + "<Object> p = " + classname.toLowerCase() + ".__dna__addressof(" + classname + "." + descrname + ");\n"
					+ pointerType + " p_"+ field.getName() +" = p.cast(" + typeList + ");")
					);
		javadoc.appendln("</pre>");
		
		javadoc.appendln("<h3>Metadata</h3>");
		javadoc.appendln("<ul>");
		javadoc.appendln("<li>Field: '" + field.getName() + "'</li>");
		javadoc.appendln("<li>Signature: '" + field.getType().getSignature() + "'</li>");
		javadoc.appendln("<li>Actual Size (32bit/64bit): " + ctype.sizeof(Encoding.ADDR_WIDTH_32BIT) + "/" + ctype.sizeof(Encoding.ADDR_WIDTH_64BIT) + "</li>");
		javadoc.appendln("</ul>");

		classgen.addConstField("public static final", "long[]", descrname, "new long[]{" + Long.toString(offset32) + ", " + Long.toString(offset64) + "}", javadoc);
	}

	@Override
	protected void visitStruct(long offset32, long offset64, CField field, JavaType jtype) throws IOException {
		addConstField(field, offset32, offset64, field.getType(), jtype);
	}

	@Override
	public void reset() {
	}

}
