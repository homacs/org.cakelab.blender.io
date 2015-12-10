package org.cakelab.blender.generator;

import java.io.IOException;

import org.cakelab.blender.generator.code.GComment;
import org.cakelab.blender.generator.code.GMethod;
import org.cakelab.blender.generator.code.MethodGenerator;
import org.cakelab.blender.generator.type.CField;
import org.cakelab.blender.generator.type.CStruct;
import org.cakelab.blender.generator.type.CType;
import org.cakelab.blender.generator.type.CType.CTypeType;
import org.cakelab.blender.generator.type.JavaType;
import org.cakelab.blender.model.DNAArray;
import org.cakelab.blender.model.DNAPointer;



public class DNAFacetGetMethodGenerator extends MethodGenerator {



	public DNAFacetGetMethodGenerator(DNAFacetClassGenerator classGenerator) 
	{
		super(classGenerator);
	}

	@Override
	public void visitField(long offset32, long offset64, CStruct struct, CField field, JavaType jtype) throws IOException {
		super.visitField(offset32, offset64, struct, field, jtype);
		
		if (content.numLines()>0)	classgen.addMethod(new GMethod(content));
	}

	@Override
	protected void visitArray(long offset32, long offset64, CField field, JavaType jtype) throws IOException {
		classgen.addImport(DNAArray.class);
		appendMethodSignature(field, jtype);
		appendln("{");
		content.indent(+1);
		String parameterizedArrayType = DNAArray.class.getSimpleName() + getTemplateParameter(field.getType().getReferencedType(), jtype.getReferencedType());
		
		String targetTypeListVar = "__dna__targetTypes";
		__appendTargetTypeList(targetTypeListVar, jtype, field.getType());
		
		String dimensionsVar = "__dna__dimensions";
		appendln("int[] " + dimensionsVar + " = new int[]{");
		content.indent(+1);
		content.append(indent).append(field.getType().getArrayLength());
		CType tarr = field.getType().getReferencedType();
		while(tarr.getTypetype() == CTypeType.TYPE_ARRAY) {
			content.append(",").append(NL);
			content.append(indent).append(tarr.getArrayLength());
			tarr = tarr.getReferencedType();
		}
		appendln("");
		content.indent(-1);
		appendln("};");
		
		appendln("if (" + ARCH64_IDENTIFICATION_BOOLEAN + ") {");
		content.indent(+1);
		appendln("return new " + parameterizedArrayType + "(__dna__address + " + offset64 + ", "+ targetTypeListVar + ", " + dimensionsVar + ", __dna__blockMap);");
		content.indent(-1);
		appendln("} else {");
		content.indent(+1);
		appendln("return new " + parameterizedArrayType + "(__dna__address + " + offset32 + ", "+ targetTypeListVar + ", " + dimensionsVar + ", __dna__blockMap);");
		content.indent(-1);
		appendln("}");
	
		content.indent(-1);
		appendln("}");
	}

	private void __appendTargetTypeList(String targetTypeListVar, JavaType jtype,
			CType ctype) throws IOException {
		appendln("Class<?>[] " + targetTypeListVar + " = " + getTypeList(jtype.getReferencedType(), ctype) + ";");
	}



	@Override
	protected void visitPointer(long offset32, long offset64, CField field, JavaType jtype) throws IOException {
		classgen.addImport(DNAPointer.class);
		appendMethodSignature(field, jtype);
		appendln("{");
		content.indent(+1);
		String parameterizedPointerType = DNAPointer.class.getSimpleName() + getTemplateParameter(field.getType().getReferencedType(), jtype.getReferencedType());
		String targetAddrVar = "__dna__targetAddress"; 
		
		appendln("long " + targetAddrVar + ";");
		
		
		appendln("if (" + ARCH64_IDENTIFICATION_BOOLEAN + ") {");

		content.indent(+1);
		appendln(targetAddrVar + " = " + "__dna__block.readLong(__dna__address + " + offset64 + ");");
		content.indent(-1);
		
		appendln("} else {");
		
		content.indent(+1);
		appendln(targetAddrVar + " = " + "__dna__block.readLong(__dna__address + " + offset32 + ");");
		content.indent(-1);
		appendln("}");
		
		String targetTypeListVar = "__dna__targetTypes";
		__appendTargetTypeList(targetTypeListVar, jtype, field.getType());
		
		appendln("return new " + parameterizedPointerType + "(" + targetAddrVar +", "+ targetTypeListVar + ", __dna__blockMap);");
		
		content.indent(-1);
		appendln("}");
	}





	@Override
	protected void visitStruct(long offset32, long offset64, CField field, JavaType jtype) throws IOException {
		appendMethodSignature(field, jtype);
		appendln("{");
		content.indent(+1);
		appendln("if (" + ARCH64_IDENTIFICATION_BOOLEAN + ") {");

		content.indent(+1);
		appendln("return new " + jtype.getName() + "(__dna__address + " + offset64 + ", __dna__blockMap);");
		content.indent(-1);
		
		appendln("} else {");
		
		content.indent(+1);
		appendln("return new " + jtype.getName() + "(__dna__address + " + offset32 + ", __dna__blockMap);");
		content.indent(-1);
		appendln("}");
		
		content.indent(-1);
		appendln("}");
	}


	@Override
	protected void visitScalar(long offset32, long offset64, CField field, JavaType jtype) throws IOException {
		appendMethodSignature(field, jtype);
		appendln("{");
		content.indent(+1);
		appendln("if (" + ARCH64_IDENTIFICATION_BOOLEAN + ") {");

		content.indent(+1);
		appendln("return __dna__block." + readMethod(jtype, field.getType()) + "(__dna__address + " + offset64 + ");");
		content.indent(-1);
		
		appendln("} else {");
		
		content.indent(+1);
		appendln("return __dna__block." + readMethod(jtype, field.getType()) + "(__dna__address + " + offset32 + ");");
		content.indent(-1);
		
		appendln("}");
		content.indent(-1);
		appendln("}");
	}


	private String readMethod(JavaType jtype, CType ctype) {
		String typeName = jtype.getName();
		if (ctype.getSignature().contains("int64")) {
			typeName = "int64";
		}
		return "read" + toCamelCase(typeName);
	}


	private void appendMethodSignature(CField field, JavaType jtype) throws IOException {
		
		classgen.addImport(IOException.class);

		String returnType = getFieldType(field.getType(), jtype);
		
		GComment javadoc = new GComment(GComment.Type.JavaDoc);

		javadoc.appendln();
		javadoc.appendln("Get method for struct member '" + field.getName() + "'.");
		javadoc.appendln("@see #" + super.getFieldDescriptorName(field.getName()));
		
		
		appendln(javadoc.toString(0));
		appendln("public " + returnType + " get" + toCamelCase(field.getName()) + "() throws " + IOException.class.getSimpleName());
	}



}
