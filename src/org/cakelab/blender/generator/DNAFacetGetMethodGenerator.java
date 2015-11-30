package org.cakelab.blender.generator;

import java.io.IOException;

import org.cakelab.blender.doc.Documentation;
import org.cakelab.blender.file.dna.BlendField;
import org.cakelab.blender.file.dna.BlendStruct;
import org.cakelab.blender.generator.code.CodeSection;
import org.cakelab.blender.generator.code.GComment;
import org.cakelab.blender.generator.code.MethodGenerator;
import org.cakelab.blender.generator.type.CType;
import org.cakelab.blender.generator.type.JavaType;
import org.cakelab.blender.generator.type.CType.CTypeType;
import org.cakelab.blender.generator.type.JavaType.JTypeType;
import org.cakelab.blender.model.DNAArray;
import org.cakelab.blender.model.DNAPointer;
import org.cakelab.blender.model.int64;



public class DNAFacetGetMethodGenerator extends MethodGenerator {

	private Documentation docs;
	private long pointersize;
	private String fielddoc;

	public DNAFacetGetMethodGenerator(DNAFacetClassGenerator classGenerator) 
	{
		super(classGenerator);
		docs = classGenerator.getDocs();
		pointersize = classGenerator.getPointerSize();
	}

	
	public void visitField(long offset, BlendStruct struct, BlendField field, CType ctype, JavaType jtype) throws IOException {
		reset();
		fielddoc = docs.getFieldDoc(struct, field);
		switch(ctype.getTypetype()) {
		case TYPE_ARRAY:
			visitArray(offset, field, ctype, jtype);
			break;
		case TYPE_FUNCTION_POINTER:
			break;
		case TYPE_POINTER:
			visitPointer(offset, field, ctype, jtype);
			break;
		case TYPE_SCALAR:
			visitScalar(offset, field, ctype, jtype);
			break;
		case TYPE_STRUCT:
			visitStruct(offset, field, ctype, jtype);
			break;
		case TYPE_VOID:
			break;
		default:
			break;
		}
		if (content.lines()>0)	classgen.addMethod(content.toString(1));
	}


	private void visitArray(long offset, BlendField field, CType ctype,
			JavaType jtype) throws IOException {
		classgen.addImport(DNAArray.class);
		appendMethodSignature(field, ctype, jtype);
		appendln("{");
		content.indent(+1);
		String parameterizedArrayType = DNAArray.class.getSimpleName() + getTemplateParameter(ctype.getReferencedType(), jtype.getReferencedType());
		
		String targetTypeListVar = "targetTypes";
		appendTargetTypeList(targetTypeListVar, jtype, ctype);
		
		String dimensionsVar = "dimensions";
		appendln("int[] " + dimensionsVar + " = new int[]{");
		content.indent(+1);
		content.append(indent).append(ctype.getArrayLength());
		CType tarr = ctype.getReferencedType();
		while(tarr.getTypetype() == CTypeType.TYPE_ARRAY) {
			content.append(",").append(NL);
			content.append(indent).append(tarr.getArrayLength());
			tarr = tarr.getReferencedType();
		}
		appendln("");
		content.indent(-1);
		appendln("};");
		
		appendln("return new " + parameterizedArrayType + "(__dna__address + " + offset + ", "+ targetTypeListVar + ", " + dimensionsVar + ", __dna__blockMap);");
		
		content.indent(-1);
		appendln("}");
	}


	private void appendTargetTypeList(String targetTypeListVar, JavaType jtype,
			CType ctype) throws IOException {
		appendln("Class<?>[] " + targetTypeListVar + " = new Class[]{");
		content.indent(+1);
		JavaType target = jtype.getReferencedType();
		content.append(getClassTypeName(ctype, target)).append(".class");
		while(target.getReferencedType() != null) {
			target = target.getReferencedType();
			content.appendln(",");
			content.append(getClassTypeName(ctype, target)).append(".class");
		}
		appendln("");
		content.indent(-1);
		appendln("};");
	}

	private String getClassTypeName(CType ctype, JavaType jtype) throws IOException {
		if (jtype.getTypetype() == JTypeType.TYPE_SCALAR) {
			return getScalarJavaObjectType(ctype, jtype).getSimpleName();
		} else {
			return jtype.getName();
		}
	}


	private void visitPointer(long offset, BlendField field, CType ctype,
			JavaType jtype) throws IOException {
		classgen.addImport(DNAPointer.class);
		appendMethodSignature(field, ctype, jtype);
		appendln("{");
		content.indent(+1);
		String parameterizedPointerType = DNAPointer.class.getSimpleName() + getTemplateParameter(ctype.getReferencedType(), jtype.getReferencedType());
		String targetAddrVar = "targetAddress"; 
		appendln("long " + targetAddrVar + " = " + "__dna__block.readLong(__dna__address + " + offset + ");");
		
		String targetTypeListVar = "targetTypes";
		appendTargetTypeList(targetTypeListVar, jtype, ctype);
		
		appendln("return new " + parameterizedPointerType + "(" + targetAddrVar +", "+ targetTypeListVar + ", __dna__blockMap);");
		
		content.indent(-1);
		appendln("}");
	}



	private String getTemplateParameter(CType ctype, JavaType jtype) throws IOException {
		String templateParam = "?";
		switch(ctype.getTypetype()) {
		case TYPE_ARRAY:
			templateParam = jtype.getName() + getTemplateParameter(ctype.getReferencedType(), jtype.getReferencedType());
			break;
		case TYPE_FUNCTION_POINTER:
			templateParam = jtype.getName();
			break;
		case TYPE_POINTER:
			classgen.addImport(DNAPointer.class);
			templateParam = jtype.getName() + getTemplateParameter(ctype.getReferencedType(), jtype.getReferencedType());
			break;
		case TYPE_SCALAR:
			templateParam = getScalarJavaObjectType(ctype, jtype).getSimpleName();
			break;
		case TYPE_STRUCT:
			templateParam = jtype.getName();
			break;
		case TYPE_VOID:
			templateParam = jtype.getName();
			break;
		default:
			throw new IOException("unrecognized type in field '" + ctype.getSignature() + "'");
		}
		return "<" + templateParam + ">";
	}

	private Class<?> getScalarJavaObjectType(CType ctype, JavaType jtype) throws IOException {
		if (jtype.getName().equals(byte.class.getName())) {
			return Byte.class;
		} else if (jtype.getName().equals(short.class.getName())) {
			return Short.class;
		} else if (jtype.getName().equals(int.class.getName())) {
			return Integer.class;
		} else if (jtype.getName().equals(long.class.getName())) {
			if (ctype.getName().contains("int64")) {
				return int64.class;
			} else {
				return Long.class;
			}
		} else if (jtype.getName().equals(int64.class.getName())) {
			return int64.class;
		} else if (jtype.getName().equals(float.class.getName())) {
			return Float.class;
		} else if (jtype.getName().equals(double.class.getName())) {
			return Double.class;
		} else {
			throw new IOException("unrecognized type '" + jtype.getName() + "'");
		}
	}



	private void visitStruct(long offset, BlendField field, CType ctype,
			JavaType jtype) throws IOException {
		appendMethodSignature(field, ctype, jtype);
		appendln("{");
		content.indent(+1);
		
		appendln("return new " + jtype.getName() + "(__dna__address + " + offset + ", __dna__blockMap);");
		
		content.indent(-1);
		appendln("}");
		
	}


	private void visitScalar(long offset, BlendField field, CType ctype, JavaType jtype) throws IOException {
		appendMethodSignature(field, ctype, jtype);
		appendln("{");
		content.indent(+1);
		
		appendln("return __dna__block." + readMethod(jtype, ctype) + "(__dna__address + " + offset + ");");
		
		content.indent(-1);
		appendln("}");
	}


	private String readMethod(JavaType jtype, CType ctype) {
		String typeName = jtype.getName();
		if (ctype.getName().contains("int64")) {
			typeName = "int64";
		}
		return "read" + toCamelCase(typeName);
	}


	private void appendMethodSignature(BlendField field, CType ctype,
			JavaType jtype) throws IOException {
		
		classgen.addImport(IOException.class);
		
		String returnType = jtype.getName();
		if (ctype.getTypetype() == CTypeType.TYPE_POINTER || ctype.getTypetype() == CTypeType.TYPE_ARRAY) {
			returnType += getTemplateParameter(ctype.getReferencedType(), jtype.getReferencedType());
		}
		
		
		GComment javadoc = new GComment(GComment.Type.JavaDoc);

		javadoc.appendln();
		javadoc.appendln("Get method for struct member '" + field.getName() + "'.");
		if (fielddoc != null) {
			javadoc.appendln("<h4>Field Documentation</h4>");
			javadoc.appendln(fielddoc);
		}
		javadoc.appendln("<h4>Metadata</h4>");
		javadoc.appendln("<ul>");
		javadoc.appendln("<li>Field: '" + field.getName() + "'</li>");
		javadoc.appendln("<li>Signature: '" + field.getSignature() + "'</li>");
		javadoc.appendln("<li>Size (from metadata): " + ctype.getSize() + "</li>");
		javadoc.appendln("<li>Actual Size: " + ctype.sizeof(pointersize) + "</li>");
		javadoc.appendln("</ul>");
		
		
		appendln(javadoc.toString(0));
		appendln("public " + returnType + " get" + toCamelCase(field.getName()) + "() throws " + IOException.class.getSimpleName());
	}

}
