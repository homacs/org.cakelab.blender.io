package org.cakelab.blender.generator;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;

import org.cakelab.blender.doc.DocumentationProvider;
import org.cakelab.blender.file.BlenderFile;
import org.cakelab.blender.generator.code.ClassGenerator;
import org.cakelab.blender.generator.code.GComment;
import org.cakelab.blender.generator.code.GField;
import org.cakelab.blender.generator.code.GPackage;
import org.cakelab.blender.generator.type.CField;
import org.cakelab.blender.generator.type.CStruct;
import org.cakelab.blender.generator.type.Renaming;
import org.cakelab.blender.model.MainBase;

public class MainLibClassGenerator extends ClassGenerator {

	private String classname;
	private GComment comment;
	private GPackage dnaPackage;


	public MainLibClassGenerator(ModelGenerator modelgen, GPackage gpackage, GPackage dnaPackage, DocumentationProvider docs2) {
		super(modelgen, gpackage, docs2);
		this.dnaPackage = dnaPackage;
		classname = "Main";
		
		addImport(dnaPackage);
		addImport(BlenderFile.class);
		addImport(MainBase.class);
		addImport(IOException.class);
		
		comment = new GComment(GComment.Type.JavaDoc);
		comment.appendln();
		comment.appendln("Generated class " + classname + " derived from blenders BKE_main.h");
		comment.appendln();
		comment.appendln("This class is basically the entry point to all data in a blender\n"
				+ "file and associated external files (so called libraries).\n"
				+ "The content of one blender file goes in one main library.\n"
				+ "While blender can open multiple files, every file gets its own\n"
				+ "main lib and all main libs are linked to each other.\n"
				+ "\n"
				+ "@author homac");
		
		
		addField("private", classname, "next", "Linkage between main libraries.");
		addField("private", classname, "prev", "Linkage between main libraries.");
		addField("private", "String", "name", "1024 = FILE_MAX");
		addField("private", "short", "versionfile", "see BLENDER_VERSION, BLENDER_SUBVERSION");
		addField("private", "short", "subversionfile", "see BLENDER_VERSION, BLENDER_SUBVERSION");
		addField("private", "short", "minversionfile");
		addField("private", "short", "minsubversionfile");
		addField("private", "int", "revision", "svn revision of binary that saved file");
		addField("private", "short", "recovered", "indicate the main->name (file) is the recovered one");
	}



	public void visit(CStruct struct) throws FileNotFoundException {
		if (MainBase.isLibraryElement(struct)) {
			GField gfield = addField("private", Renaming.mapStruct2Class(struct.getSignature()), toFirstLowerCase(struct.getSignature()));
			addSetMethod(gfield);
		}
	}

	

	private void addGetMethod(GField field) {
		StringBuffer method = new StringBuffer();
		method.append("public " + field.getType() + " get" +  toCamelCase(field.getName()) + "(){ return " + field.getName() + ";}");
		addMethod(method.toString());
	}

	private void addSetMethod(GField field) {
		StringBuffer method = new StringBuffer();
		method.append("public void set" +  toCamelCase(field.getName()) + "("
				+ field.getType() + " " + field.getName() + ") {\n"
				+ "\t\tthis." + field.getName() + " = " + field.getName() + ";\n"
						+ "\t}");
		addMethod(method.toString());
	}


	public void write() throws FileNotFoundException {
		PrintStream out = new PrintStream(new FileOutputStream(new File(gpackage.getDir(), classname + ".java")));

		
		
		try {
			out.println("package " + gpackage.getName() + ";");
			out.println();
			out.println(imports.toString());
			out.println();
			
			out.print(comment.toString(0));
			
			out.println("public class " + classname + " extends " + MainBase.class.getSimpleName() + " {");
			out.println();

			indent(+1);
			for (GField field : fields) {
				out.println(field.toString(indent));
			}
			out.println();
			
			//
			// Create constructor
			//
			out.println(indent + "public " + classname + "(" + BlenderFile.class.getSimpleName() + " blendFile) throws " + IOException.class.getSimpleName() + "{");
			indent(+1);
			out.println(indent + "super(\""  + dnaPackage.getName() + "\", blendFile);");
			indent(-1);
			out.println(indent + "}");
			out.println();
			
			
			for (String method : methods) {
				out.println(indent + method);
			}
			indent(-1);
			out.println("}");
		} finally {
			out.close();
		}
	}


	@Override
	public GField addField(String modifiers, String type, String name,
			GComment gcomment) {
		GField field = super.addField(modifiers, type, name, gcomment);
		addGetMethod(field);
		return field;
	}

	public GField addField(String modifiers, String type, String name,
			String comment) {
		GComment gcomment = new GComment(GComment.Type.JavaDoc);
		gcomment.appendln(comment);
		return addField(modifiers, type, name, gcomment);
	}



	@Override
	public String getClassName() {
		return classname;
	}



}
