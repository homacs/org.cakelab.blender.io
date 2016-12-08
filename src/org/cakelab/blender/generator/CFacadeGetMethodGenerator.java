package org.cakelab.blender.generator;

import java.io.IOException;

import org.cakelab.blender.generator.typemap.JavaType;
import org.cakelab.blender.generator.typemap.JavaType.JKind;
import org.cakelab.blender.generator.utils.GComment;
import org.cakelab.blender.generator.utils.GMethod;
import org.cakelab.blender.generator.utils.MethodGenerator;
import org.cakelab.blender.metac.CField;
import org.cakelab.blender.metac.CStruct;
import org.cakelab.blender.metac.CType;
import org.cakelab.blender.metac.CType.CKind;
import org.cakelab.blender.nio.CArrayFacade;
import org.cakelab.blender.nio.CPointer;



public class CFacadeGetMethodGenerator extends MethodGenerator implements CFacadeMembers {



	public CFacadeGetMethodGenerator(CFacadeClassGenerator classGenerator) 
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
		classgen.addImport(CArrayFacade.class);
		appendMethodSignature(field, jtype);
		appendln("{");
		content.indent(+1);
		String parameterizedArrayType = CArrayFacade.class.getSimpleName() + getTemplateParameter(field.getType().getReferencedType(), jtype.getReferencedType());
		
		String targetTypeListVar = "__dna__targetTypes";
		__appendTargetTypeList(targetTypeListVar, jtype, field.getType());
		
		String dimensionsVar = "__dna__dimensions";
		appendln("int[] " + dimensionsVar + " = new int[]{");
		content.indent(+1);
		content.append(indent).append(field.getType().getArrayLength());
		CType tarr = field.getType().getReferencedType();
		while(tarr.getKind() == CKind.TYPE_ARRAY) {
			content.append(",").append(NL);
			content.append(indent).append(tarr.getArrayLength());
			tarr = tarr.getReferencedType();
		}
		appendln("");
		content.indent(-1);
		appendln("};");
		
		appendln("if (" + ARCH64_TEST + ") {");
		content.indent(+1);
		appendln("return new " + parameterizedArrayType + "(" + __io__address + " + " + offset64 + ", "+ targetTypeListVar + ", " + dimensionsVar + ", " + __io__block + ", " + __io__blockTable + ");");
		content.indent(-1);
		appendln("} else {");
		content.indent(+1);
		appendln("return new " + parameterizedArrayType + "(" + __io__address + " + " + offset32 + ", "+ targetTypeListVar + ", " + dimensionsVar + ", " + __io__block + ", " + __io__blockTable + ");");
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
		classgen.addImport(CPointer.class);
		appendMethodSignature(field, jtype);
		appendln("{");
		content.indent(+1);
		String parameterizedPointerType = CPointer.class.getSimpleName() + getTemplateParameter(field.getType().getReferencedType(), jtype.getReferencedType());
		String targetAddrVar = "__dna__targetAddress"; 
		
		appendln("long " + targetAddrVar + ";");
		
		
		appendln("if (" + ARCH64_TEST + ") {");

		content.indent(+1);
		appendln(targetAddrVar + " = " + __io__block + ".readLong(" + __io__address + " + " + offset64 + ");");
		content.indent(-1);
		
		appendln("} else {");
		
		content.indent(+1);
		appendln(targetAddrVar + " = " + __io__block + ".readLong(" + __io__address + " + " + offset32 + ");");
		content.indent(-1);
		appendln("}");
		
		String targetTypeListVar = "__dna__targetTypes";
		CType ctype = field.getType();
		__appendTargetTypeList(targetTypeListVar, jtype, ctype);
		JavaType targetType = jtype.getReferencedType();
		if (targetType.getKind() == JKind.TYPE_OBJECT && ctype.getReferencedType().getKind() != CKind.TYPE_POINTER) {
			String sdnaIndexVar;
			if (targetType.getName().equals("Object")) {
				sdnaIndexVar = "-1";
			} else {
				sdnaIndexVar = getClassTypeName(field.getType(), targetType) + "." + __DNA__SDNA_INDEX;
			}
			appendln("return new " + parameterizedPointerType + "(" + targetAddrVar +", "+ targetTypeListVar + ", " + __io__blockTable + ".getBlock(" + targetAddrVar + ", " + sdnaIndexVar + "), " + __io__blockTable + ");");
			
		} else {
			appendln("return new " + parameterizedPointerType + "(" + targetAddrVar +", "+ targetTypeListVar + ", " + __io__blockTable + ".getBlock(" + targetAddrVar + ", " + targetTypeListVar + "), " + __io__blockTable + ");");
		}
		content.indent(-1);
		appendln("}");
	}





	@Override
	protected void visitStruct(long offset32, long offset64, CField field, JavaType jtype) throws IOException {
		appendMethodSignature(field, jtype);
		appendln("{");
		content.indent(+1);
		appendln("if (" + ARCH64_TEST + ") {");

		content.indent(+1);
		appendln("return new " + jtype.getName() + "(" + __io__address + " + " + offset64 + ", " + __io__block + ", " + __io__blockTable + ");");
		content.indent(-1);
		
		appendln("} else {");
		
		content.indent(+1);
		appendln("return new " + jtype.getName() + "(" + __io__address + " + " + offset32 + ", " + __io__block + ", " + __io__blockTable + ");");
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
		appendln("if (" + ARCH64_TEST + ") {");

		content.indent(+1);
		appendln("return " + __io__block + "." + readMethod(jtype, field.getType()) + "(" + __io__address + " + " + offset64 + ");");
		content.indent(-1);
		
		appendln("} else {");
		
		content.indent(+1);
		appendln("return " + __io__block + "." + readMethod(jtype, field.getType()) + "(" + __io__address + " + " + offset32 + ");");
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
		appendFieldDoc(javadoc);
		javadoc.appendln("@see #" + super.getFieldDescriptorName(field.getName()));
		
		
		appendln(javadoc.toString(0));
		appendln("public " + returnType + " get" + toCamelCase(field.getName()) + "() throws " + IOException.class.getSimpleName());
	}



}
