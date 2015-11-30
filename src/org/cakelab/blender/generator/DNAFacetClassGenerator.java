package org.cakelab.blender.generator;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;

import org.cakelab.blender.doc.Documentation;
import org.cakelab.blender.file.block.BlockMap;
import org.cakelab.blender.file.dna.BlendField;
import org.cakelab.blender.file.dna.BlendStruct;
import org.cakelab.blender.generator.code.ClassGenerator;
import org.cakelab.blender.generator.code.GComment;
import org.cakelab.blender.generator.code.GPackage;
import org.cakelab.blender.generator.type.CType;
import org.cakelab.blender.generator.type.JavaType;
import org.cakelab.blender.generator.type.Renaming;
import org.cakelab.blender.model.DNAFacet;
import org.cakelab.blender.model.DNATypeInfo;


public class DNAFacetClassGenerator extends ClassGenerator {

	private DNAFacetGetMethodGenerator readgen;
	
	public DNAFacetClassGenerator(ModelGenerator modelGenerator, GPackage gpackage, Documentation docs) 
	{
		super(modelGenerator, gpackage, docs);
		this.readgen = new DNAFacetGetMethodGenerator(this);
	}

	
	
	public void visit(BlendStruct struct) throws IOException {
		reset();
		
		addImport(DNAFacet.class);
		addImport(DNATypeInfo.class);
		addImport(BlockMap.class);
		long offset = 0;
		for (BlendField field : struct.getFields()) {
			CType ctype = new CType(field);
			JavaType jtype = new JavaType(ctype);
			readgen.visitField(offset, struct, field, ctype, jtype);
			offset += ctype.sizeof(modelgen.pointerSize);
		}
		
		String classname = Renaming.mapStruct2Class(struct.getType().getName());
		PrintStream out = new PrintStream(new FileOutputStream(new File(gpackage.getDir(), classname + ".java")));
		try {
			out.println("package " + gpackage + ";");
			out.println();
			out.println(imports.toString());
			out.println();
			
			GComment classdoc = new GComment(GComment.Type.JavaDoc);
			String structname = struct.getType().getName();
			classdoc.appendln();
			classdoc.appendln("Generated facet for DNA struct type '" + structname + "'.");
			classdoc.appendln();
			String text = getDocs().getStructDoc(struct);
			if (text != null) {
				classdoc.appendln("<h3>Class Documentation</h3>");
				classdoc.appendln(text);
				classdoc.appendln("<h3>Documentation Source:</h3>");
				classdoc.appendln("<p>"
						+ "This documentation is automatically retrieved from an externally\n"
						+ "maintained database by Java Blends model generator. "
						+ "It is based on information found in Blender source code."
						+ "</p>");
				classdoc.appendln();
				classdoc.appendln("<b>Database:</b>");
				classdoc.appendln("<ul>");
				classdoc.appendln("<li>System: " + docs.getSystem() + "</li>");
				classdoc.appendln("<li>Module: " + docs.getModule() + "</li>");
				classdoc.appendln("<li>Version: " + docs.getVersion() + "</li>");
				classdoc.appendln("<li>Path: " + docs.getPath() + "</li>");
				classdoc.appendln("</ul>");
				for (String author : docs.getAuthors()) {
					classdoc.appendln("@author " + author);
				}
			} else {
				classdoc.appendln("<br/><br/>No documentation found.");
			}
			out.println(classdoc.toString(0));
			
			out.println("@" + DNATypeInfo.class.getSimpleName() + "(size=" + struct.getType().getSize() + ")");
			out.println("public class " + classname + " extends " + DNAFacet.class.getSimpleName() + " {");
			out.println();

			//
			// Create constructor
			//
			out.println("\tpublic " + classname + "(long __address, " + BlockMap.class.getSimpleName() + " __blockMap) {");
			out.println("\t\tsuper(__address, __blockMap);");
			out.println("\t}");
			out.println();
			
			
			for (String method : methods) {
				out.println(method);
			}
			out.println("}");
		} finally {
			out.close();
		}
	}



	@Override
	public void reset() {
		super.reset();
		readgen.reset();
	}



	public long getPointerSize() {
		return modelgen.pointerSize;
	}

}
