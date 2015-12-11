package org.cakelab.blender.generator;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;

import org.cakelab.blender.doc.DocumentationProvider;
import org.cakelab.blender.file.Encoding;
import org.cakelab.blender.file.block.BlockTable;
import org.cakelab.blender.generator.code.ClassGenerator;
import org.cakelab.blender.generator.code.GComment;
import org.cakelab.blender.generator.code.GField;
import org.cakelab.blender.generator.code.GMethod;
import org.cakelab.blender.generator.code.GPackage;
import org.cakelab.blender.generator.type.CField;
import org.cakelab.blender.generator.type.CStruct;
import org.cakelab.blender.generator.type.CType;
import org.cakelab.blender.generator.type.JavaType;
import org.cakelab.blender.generator.type.Renaming;
import org.cakelab.blender.model.DNAFacet;
import org.cakelab.blender.model.DNAPointer;
import org.cakelab.blender.model.DNATypeInfo;


public class DNAFacetClassGenerator extends ClassGenerator {

	private DNAFacetGetMethodGenerator readgen;
	private DNAFacetSetMethodGenerator writegen;
	private DNAFacetFieldDescGenerator fieldgen;
	private String classname;
	
	public DNAFacetClassGenerator(ModelGenerator modelGenerator, GPackage gpackage, DocumentationProvider docs2) 
	{
		super(modelGenerator, gpackage, docs2);
		this.readgen = new DNAFacetGetMethodGenerator(this);
		this.writegen = new DNAFacetSetMethodGenerator(this);
		this.fieldgen = new DNAFacetFieldDescGenerator(this);
	}

	
	
	public void visit(CStruct struct) throws IOException {
		reset();

		classname = Renaming.mapStruct2Class(struct.getSignature());

		
		
		addImport(DNAFacet.class);
		addImport(DNATypeInfo.class);
		addImport(BlockTable.class);
		long offset32 = 0;
		long offset64 = 0;
		for (CField field : struct.getFields()) {
			CType ctype = field.getType();
			JavaType jtype = new JavaType(ctype);
			fieldgen.visitField(offset32, offset64, struct, field, jtype);
			readgen.visitField(offset32, offset64, struct, field, jtype);
			writegen.visitField(offset32, offset64, struct, field, jtype);
			offset32 += ctype.sizeof(Encoding.ADDR_WIDTH_32BIT);
			offset64 += ctype.sizeof(Encoding.ADDR_WIDTH_64BIT);
		}
		
		createMethod__dna__addressof();
		
		PrintStream out = new PrintStream(new FileOutputStream(new File(gpackage.getDir(), classname + ".java")));
		try {
			out.println("package " + gpackage + ";");
			out.println();
			out.println(imports.toString());
			out.println();
			
			GComment classdoc = new GComment(GComment.Type.JavaDoc);
			String structname = struct.getSignature();
			classdoc.appendln();
			classdoc.appendln("Generated facet for DNA struct type '" + structname + "'.");
			classdoc.appendln();
			String text = getDocs().getStructDoc(structname);
			if (text != null) {
				classdoc.appendln("<h3>Class Documentation</h3>");
				classdoc.appendln(text);
			} else {
				classdoc.appendln("<br/><br/>No documentation found.");
			}
			out.println(classdoc.toString(0));
			
			out.println("@" + DNATypeInfo.class.getSimpleName() + "(size32=" + struct.sizeof(Encoding.ADDR_WIDTH_32BIT) + ", size64=" + struct.sizeof(Encoding.ADDR_WIDTH_64BIT) + ")");
			out.println("public class " + classname + " extends " + DNAFacet.class.getSimpleName() + " {");
			out.println();

			for (GField constField : constFields) {
				out.println(constField.toString("\t"));
			}
			
			//
			// Create constructor
			//
			out.println("\tpublic " + classname + "(long __address, " + BlockTable.class.getSimpleName() + " __blockTable) {");
			out.println("\t\tsuper(__address, __blockTable);");
			out.println("\t}");
			out.println();
			
			
			for (GMethod method : methods) {
				out.println(method.toString(1));
			}
			out.println("}");
		} finally {
			out.close();
		}
	}



	private void createMethod__dna__addressof() {
		addImport(DNAPointer.class);
		GMethod method = new GMethod(0);
		String pointerType = "DNAPointer<" + classname + ">";
		method.appendln("public " + pointerType + " __dna__addressof() {");
		method.indent(+1);
		
		method.appendln("return new " + pointerType + "(__dna__address, new Class[]{" + classname + ".class}, __dna__blockTable);");
		
		method.indent(-1);
		method.appendln("}");
		addMethod(method);
	}



	@Override
	public void reset() {
		super.reset();
		readgen.reset();
		writegen.reset();
		fieldgen.reset();
	}



	@Override
	public String getClassName() {
		return classname;
	}

	


}
