package org.cakelab.blender.generator;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;

import org.cakelab.blender.doc.DocumentationProvider;
import org.cakelab.blender.generator.typemap.JavaType;
import org.cakelab.blender.generator.typemap.Renaming;
import org.cakelab.blender.generator.utils.ClassGenerator;
import org.cakelab.blender.generator.utils.GComment;
import org.cakelab.blender.generator.utils.GField;
import org.cakelab.blender.generator.utils.GMethod;
import org.cakelab.blender.generator.utils.GPackage;
import org.cakelab.blender.io.Encoding;
import org.cakelab.blender.io.block.Block;
import org.cakelab.blender.io.block.BlockTable;
import org.cakelab.blender.io.dna.internal.StructDNA;
import org.cakelab.blender.metac.CField;
import org.cakelab.blender.metac.CStruct;
import org.cakelab.blender.metac.CType;
import org.cakelab.blender.nio.CFacade;
import org.cakelab.blender.nio.CMetaData;
import org.cakelab.blender.nio.CPointer;


public class CFacadeClassGenerator extends ClassGenerator implements CFacadeMembers {

	private CFacadeGetMethodGenerator readgen;
	private CFacadeSetMethodGenerator writegen;
	private CFacadeFieldDescGenerator fieldgen;
	private String classname;
	
	public CFacadeClassGenerator(ModelGenerator modelGenerator, GPackage gpackage, DocumentationProvider docs2) 
	{
		super(modelGenerator, gpackage, docs2);
		this.readgen = new CFacadeGetMethodGenerator(this);
		this.writegen = new CFacadeSetMethodGenerator(this);
		this.fieldgen = new CFacadeFieldDescGenerator(this);
	}

	
	
	public void visit(CStruct struct) throws IOException {
		reset();

		classname = Renaming.mapStruct2Class(struct.getSignature());

		
		
		addImport(CFacade.class);
		addImport(CMetaData.class);
		addImport(Block.class);
		addImport(BlockTable.class);

		GComment doc = new GComment(GComment.Type.JavaDoc);
		doc.appendln();
		doc.appendln("This is the sdna index of the struct " + struct.getSignature() + ".");
		doc.appendln("<p>");
		doc.appendln("It is required when allocating a new block to store data for " + struct.getSignature() + ".");
		doc.appendln("</p>");
		doc.appendln("@see {@link " + StructDNA.class.getName() + "}");
		doc.appendln("@see {@link " + BlockTable.class.getName() + "#allocate}");
		this.addConstField("public static final", "int", __DNA__SDNA_INDEX, Integer.toString(struct.getSdnaIndex()), doc);
		
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
		
		createMethod__io__addressof();
		
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
			
			out.println("@" + CMetaData.class.getSimpleName() + "(size32=" + struct.sizeof(Encoding.ADDR_WIDTH_32BIT) + ", size64=" + struct.sizeof(Encoding.ADDR_WIDTH_64BIT) + ")");
			out.println("public class " + classname + " extends " + CFacade.class.getSimpleName() + " {");
			out.println();

			for (GField constField : constFields) {
				out.println(constField.toString("\t"));
			}
			
			//
			// Create constructor
			//
			out.println("\tpublic " + classname + "(long __address, " + Block.class.getSimpleName() + " __block, " + BlockTable.class.getSimpleName() + " __blockTable) {");
			out.println("\t\tsuper(__address, __block, __blockTable);");
			out.println("\t}");
			out.println();
			
			//
			// Create copy constructor
			//
			out.println("\tprotected " + classname + "(" + classname + " that) {");
			out.println("\t\tsuper(that." + __io__address + ", that." + __io__block + ", that." + __io__blockTable + ");");
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



	private void createMethod__io__addressof() {
		addImport(CPointer.class);
		
		GMethod method = new GMethod(0);
		GComment comment = new GComment(GComment.Type.JavaDoc);
		comment.appendln();
		comment.appendln("Instantiates a pointer on this instance.");
		method.setComment(comment);
		
		String pointerType = "CPointer<" + classname + ">";
		method.appendln("public " + pointerType + " " + __io__addressof + "() {");
		method.indent(+1);
		
		method.appendln("return new " + pointerType + "(" + __io__address + ", new Class[]{" + classname + ".class}, " + __io__block + ", " + __io__blockTable + ");");
		
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
