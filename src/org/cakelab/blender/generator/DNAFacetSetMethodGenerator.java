package org.cakelab.blender.generator;

import java.io.IOException;

import org.cakelab.blender.generator.code.GComment;
import org.cakelab.blender.generator.code.GMethod;
import org.cakelab.blender.generator.code.MethodGenerator;
import org.cakelab.blender.generator.type.CField;
import org.cakelab.blender.generator.type.CStruct;
import org.cakelab.blender.generator.type.CType;
import org.cakelab.blender.generator.type.JavaType;
import org.cakelab.blender.model.DNAArray;
import org.cakelab.blender.model.DNAPointer;



public class DNAFacetSetMethodGenerator extends MethodGenerator {


	public DNAFacetSetMethodGenerator(DNAFacetClassGenerator classGenerator) 
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
		
		appendln("if (" + ARCH64_IDENTIFICATION_BOOLEAN + ") {");
		content.indent(+1);
		appendln("__dna__block." + writeMethod(jtype, field.getType()) + "(__dna__address + " + offset64 + ", " + field.getName() + ");");
		content.indent(-1);
		appendln("} else {");
		content.indent(+1);
		appendln("__dna__block." + writeMethod(jtype, field.getType()) + "(__dna__address + " + offset32 + ", " + field.getName() + ");");
		content.indent(-1);
		appendln("}");

		content.indent(-1);
		appendln("}");
	}


	@Override
	protected void visitPointer(long offset32, long offset64, CField field, JavaType jtype) throws IOException {
		classgen.addImport(DNAPointer.class);
		appendMethodSignature(field, jtype);
		appendln("{");
		content.indent(+1);
		
		appendln("if (" + ARCH64_IDENTIFICATION_BOOLEAN + ") {");
		content.indent(+1);
		appendln("__dna__block.writeLong(__dna__address + " + offset64 + ", " + field.getName() + ".getAddress());");
		content.indent(-1);
		appendln("} else {");
		content.indent(+1);
		appendln("__dna__block.writeLong(__dna__address + " + offset32 + ", " + field.getName() + ".getAddress());");
		content.indent(-1);
		appendln("}");

		content.indent(-1);
		appendln("}");
	}

	@Override
	protected void visitArray(long offset32, long offset64, CField field, JavaType jtype) throws IOException {
		classgen.addImport(DNAArray.class);
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
		appendln("if (" + ARCH64_IDENTIFICATION_BOOLEAN + ") {");
		content.indent(+1);
		appendln(offsetVar + " = " + offset64 + ";");
		content.indent(-1);
		appendln("} else {");
		content.indent(+1);
		appendln(offsetVar + " = " + offset32 + ";");
		content.indent(-1);
		appendln("}");

		
		
		appendln("if (__dna__equals(" + other + ", __dna__address + "+ offsetVar +")) {");
		content.indent(+1);
		appendln("return;");
		content.indent(-1);
		appendln("} else if (__dna__same__encoding(this, " + other + ")) {");
		content.indent(+1);
		appendln("__dna__native__copy(__dna__block, __dna__address + " + offsetVar + ", " + other + ");");
		content.indent(-1);
		appendln("} else {");
		content.indent(+1);
		appendln("__dna__generic__copy( get" + toCamelCase(field.getName()) + "(), " + other + ");");
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
		javadoc.appendln("@see #" + super.getFieldDescriptorName(field.getName()));
		
		
		appendln(javadoc.toString(0));
		appendln("public void set" + toCamelCase(field.getName()) + "(" + paramType + " " + field.getName() + ") throws " + IOException.class.getSimpleName());
	}

}
