package org.cakelab.blender.generator;

import java.io.IOException;

import org.cakelab.blender.generator.typemap.JavaType;
import org.cakelab.blender.generator.utils.GComment;
import org.cakelab.blender.generator.utils.GMethod;
import org.cakelab.blender.generator.utils.MethodGenerator;
import org.cakelab.blender.metac.CField;
import org.cakelab.blender.metac.CStruct;
import org.cakelab.blender.metac.CType;
import org.cakelab.blender.nio.CArrayFacade;
import org.cakelab.blender.nio.CPointer;



public class CFacadeSetMethodGenerator extends MethodGenerator implements CFacadeMembers {


	public CFacadeSetMethodGenerator(CFacadeClassGenerator classGenerator) 
	{
		super(classGenerator);
	}


	@Override
	public void visitField(long offset32, long offset64, CStruct struct, CField field, JavaType jtype) throws IOException {
		super.visitField(offset32, offset64, struct, field, jtype);
		
		if (content.numLines()>0)	classgen.addMethod(new GMethod(content));
	}


	@Override
	protected void visitScalar(long offset32, long offset64, CField field, JavaType jtype) throws IOException {
		appendMethodSignature(field, jtype);
		appendln("{");
		content.indent(+1);
		
		appendln("if (" + ARCH64_TEST + ") {");
		content.indent(+1);
		appendln(__io__block + "." + writeMethod(jtype, field.getType()) + "(" + __io__address + " + " + offset64 + ", " + field.getName() + ");");
		content.indent(-1);
		appendln("} else {");
		content.indent(+1);
		appendln(__io__block + "." + writeMethod(jtype, field.getType()) + "(" + __io__address + " + " + offset32 + ", " + field.getName() + ");");
		content.indent(-1);
		appendln("}");

		content.indent(-1);
		appendln("}");
	}


	@Override
	protected void visitPointer(long offset32, long offset64, CField field, JavaType jtype) throws IOException {
		classgen.addImport(CPointer.class);
		appendMethodSignature(field, jtype);
		appendln("{");
		content.indent(+1);
		
		String addressVar = "__address";
		
		appendln("long " + addressVar + " = ((" + field.getName() + " == null) ? 0 : " + field.getName() + ".getAddress());");
		
		appendln("if (" + ARCH64_TEST + ") {");
		content.indent(+1);
		appendln(__io__block + ".writeLong(" + __io__address + " + " + offset64 + ", " + addressVar + ");");
		content.indent(-1);
		appendln("} else {");
		content.indent(+1);
		appendln(__io__block + ".writeLong(" + __io__address + " + " + offset32 + ", " + addressVar + ");");
		content.indent(-1);
		appendln("}");

		content.indent(-1);
		appendln("}");
	}

	@Override
	protected void visitArray(long offset32, long offset64, CField field, JavaType jtype) throws IOException {
		classgen.addImport(CArrayFacade.class);
		appendMethodSignature(field, jtype);
		appendLowlevelCopy(offset32, offset64, field, jtype);
	}


	@Override
	protected void visitStruct(long offset32, long offset64, CField field, JavaType jtype) throws IOException {
		appendMethodSignature(field, jtype);
		appendLowlevelCopy(offset32, offset64, field, jtype);
	}

	private void appendLowlevelCopy(long offset32, long offset64, CField field, JavaType jtype) {
		String other = field.getName();
		appendln("{");
		content.indent(+1);
		
		String offsetVar = "__dna__offset";
		appendln("long " + offsetVar + ";");
		appendln("if (" + ARCH64_TEST + ") {");
		content.indent(+1);
		appendln(offsetVar + " = " + offset64 + ";");
		content.indent(-1);
		appendln("} else {");
		content.indent(+1);
		appendln(offsetVar + " = " + offset32 + ";");
		content.indent(-1);
		appendln("}");

		
		
		appendln("if (" + __io__equals + "(" + other + ", " + __io__address + " + "+ offsetVar +")) {");
		content.indent(+1);
		appendln("return;");
		content.indent(-1);
		appendln("} else if (" + __io__same__encoding + "(this, " + other + ")) {");
		content.indent(+1);
		appendln(__io__native__copy + "(" + __io__block + ", " + __io__address + " + " + offsetVar + ", " + other + ");");
		content.indent(-1);
		appendln("} else {");
		content.indent(+1);
		appendln(__io__generic__copy + "( get" + toCamelCase(field.getName()) + "(), " + other + ");");
		content.indent(-1);
		appendln("}");
		content.indent(-1);
		appendln("}");
	}


	private String writeMethod(JavaType jtype, CType ctype) {
		String typeName = jtype.getName();
		if (ctype.getSignature().contains("int64")) {
			typeName = "int64";
		}
		return "write" + toCamelCase(typeName);
	}


	private void appendMethodSignature(CField field, JavaType jtype) throws IOException {
		
		classgen.addImport(IOException.class);

		String paramType = getFieldType(field.getType(), jtype);
		
		
		GComment javadoc = new GComment(GComment.Type.JavaDoc);

		javadoc.appendln();
		javadoc.appendln("Set method for struct member '" + field.getName() + "'.");
		appendFieldDoc(javadoc);
		javadoc.appendln("@see #" + super.getFieldDescriptorName(field.getName()));
		
		
		appendln(javadoc.toString(0));
		appendln("public void set" + toCamelCase(field.getName()) + "(" + paramType + " " + field.getName() + ") throws " + IOException.class.getSimpleName());
	}

}
