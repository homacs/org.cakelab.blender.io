package org.cakelab.blender.generator;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.List;

import org.cakelab.blender.doc.DocumentationProvider;
import org.cakelab.blender.file.BlenderFile;
import org.cakelab.blender.file.FileVersionInfo;
import org.cakelab.blender.file.block.Block;
import org.cakelab.blender.file.block.BlockHeader;
import org.cakelab.blender.generator.code.ClassGenerator;
import org.cakelab.blender.generator.code.GComment;
import org.cakelab.blender.generator.code.GComment.Type;
import org.cakelab.blender.generator.code.GField;
import org.cakelab.blender.generator.code.GMethod;
import org.cakelab.blender.generator.code.GPackage;
import org.cakelab.blender.generator.type.CStruct;
import org.cakelab.blender.generator.type.Renaming;
import org.cakelab.blender.model.MainBase;

public class MainLibClassGenerator extends ClassGenerator {

	private static final String CLASSNAME = "Main";
	private static final String MEMBER_fileGlobal = "__dna__fileGlobal";
	private static final String MEMBER_blendFile = "__dna__blendFile";
	private static final String MEMBER_doVersionCheck = "doVersionCheck";
	
	private GComment comment;
	private GPackage dnaPackage;


	public MainLibClassGenerator(ModelGenerator modelgen, GPackage gpackage, GPackage dnaPackage, DocumentationProvider docs2) {
		super(modelgen, gpackage, docs2);
		this.dnaPackage = dnaPackage;
		
		addImport(dnaPackage);
		addImport(BlenderFile.class);
		addImport(MainBase.class);
		addImport(IOException.class);
		addImport(BlockHeader.class);
		addImport(Block.class);
		addImport(List.class);
		
		comment = new GComment(GComment.Type.JavaDoc);
		comment.appendln();
		comment.appendln("Generated class " + CLASSNAME + " derived from blenders BKE_main.h");
		comment.appendln();
		comment.appendln("<p>This class is basically the entry point to all data in a blender\n"
				+ "file and associated external files (so called libraries).\n"
				+ "The content of one blender file goes in one main library.\n"
				+ "While blender can open multiple files, every file gets its own\n"
				+ "main lib and all main libs are linked to each other.\n"
				+ "</p>\n<p>"
				+ "When initialised, it scans all block headers and searches for structs, which\n"
				+ "contain a variable of class {@link ID} as its first member and attaches\n"
				+ "them to the appropriate members of the main lib (represented by this class).\n"
				+ "</p>\n<p>"
				+ "This class is also vital to check whether a given blender file is compatible\n"
				+ "with the data model associated with this Main lib class (see {@link #"+ MEMBER_doVersionCheck + "} and {@link #get" + MEMBER_fileGlobal + ")."
						+ "</p>"
				+ "@author homac");

		addVersionSpecifiers(modelgen.getVersionInfo());
		
		addVersionCheckMethod();
		
		addField("private", CLASSNAME, "next", "Linkage between main libraries.");
		addField("private", CLASSNAME, "prev", "Linkage between main libraries.");
		addField("private", "FileGlobal", MEMBER_fileGlobal, "Information about the associated file");
		
	}



	private void addVersionCheckMethod() {
		GComment comment = new GComment(GComment.Type.JavaDoc);
		comment.appendln("\n"
				+ "This method checks whether the given file is supported by the");
		comment.appendln("the generated data model.");
		GMethod method = new GMethod(0);
		method.setComment(comment);
		method.appendln("public boolean "+ MEMBER_doVersionCheck + "() throws IOException {");
		method.indent(+1);
		method.appendln("int version = " + MEMBER_blendFile + ".getVersion().getCode();");
		method.appendln("if (" + MEMBER_fileGlobal + " != null) {");
		method.indent(+1);
		method.appendln("short subversion = " + MEMBER_fileGlobal + ".getSubversion();");
		method.appendln("return (version > BLENDER_MINVERSION ");
		method.indent(+2);
		method.appendln("|| (version == BLENDER_MINVERSION && subversion >= BLENDER_MINSUBVERSION))");
		method.indent(-1);
		method.appendln("&& ");
		method.indent(+1);
		method.appendln("(version < BLENDER_VERSION ");
		method.appendln("|| (version == BLENDER_VERSION && subversion <= BLENDER_SUBVERSION));");
		method.indent(-3);
		method.appendln("} else {");
		method.indent(+1);
		method.appendln("return version > BLENDER_MINVERSION && version <= BLENDER_VERSION;");
		method.indent(-1);
		method.appendln("}");
		method.indent(-1);
		method.appendln("}");
		addMethod(method);
	}



	private void addVersionSpecifiers(FileVersionInfo versionInfo) {
		GComment comment = new GComment(GComment.Type.JavaDoc);
		comment.appendln();
		comment.appendln("This is the version of blender, the data model was generated from.");
		comment.appendln("Implicitly, it is the maximum version the generated import code can understand.");
		addConstField("public static final", "int", "BLENDER_VERSION", Integer.toString(versionInfo.getVersion().getCode()), comment);

		comment = new GComment(GComment.Type.JavaDoc);
		comment.appendln();
		comment.appendln("This is the subversion of blender, the data model was generated from.");
		comment.appendln("Implicitly, it is the maximum subversion the generated import code can understand.");
		addConstField("public static final", "int", "BLENDER_SUBVERSION", Short.toString(versionInfo.getSubversion()), comment);

		comment = new GComment(GComment.Type.JavaDoc);
		comment.appendln();
		comment.appendln("This is the minimal version of blender, the generated data model corresponds to.");
		comment.appendln("Every file with a version lower than this needs conversion.");
		addConstField("public static final", "int", "BLENDER_MINVERSION", Short.toString(versionInfo.getMinversion()), comment);

		comment = new GComment(GComment.Type.JavaDoc);
		comment.appendln();
		comment.appendln("This is the minimal version of blender, the generated data model corresponds to.");
		comment.appendln("Every file with a version lower than this needs conversion.");
		addConstField("public static final", "int", "BLENDER_MINSUBVERSION", Short.toString(versionInfo.getMinsubversion()), comment);
	}



	public void visit(CStruct struct) throws FileNotFoundException {
		if (MainBase.isLibraryElement(struct)) {
			String classname = Renaming.mapStruct2Class(struct.getSignature());
			GComment comment = new GComment(Type.JavaDoc);
			comment.appendln();
			comment.appendln("See {@link " + classname + "} for documentation.");
			GField gfield = addField("private", classname, toFirstLowerCase(struct.getSignature()), comment);
			addSetMethod(gfield, comment);
		}
	}

	

	private void addGetMethod(GField field, GComment fieldComment) {
		GMethod method = new GMethod(0);
		GComment comment = new GComment(fieldComment);
		method.setComment(comment);
		method.appendln("public " + field.getType() + " get" +  toCamelCase(field.getName()) + "(){");
		method.indent(+1);
		method.appendln("return " + field.getName() + ";");
		method.indent(-1);
		method.appendln("}");
		addMethod(method);
	}

	private void addSetMethod(GField field, GComment fieldComment) {
		GMethod method = new GMethod(0);
		GComment comment = new GComment(fieldComment);
		method.setComment(comment);
		method.appendln("public void set" +  toCamelCase(field.getName()) + "("
				+ field.getType() + " " + field.getName() + ") {");
		method.indent(+1);
		method.appendln("this." + field.getName() + " = " + field.getName() + ";");
		method.indent(-1);
		method.appendln("}");
		addMethod(method);
	}


	public void write() throws FileNotFoundException {
		PrintStream out = new PrintStream(new FileOutputStream(new File(gpackage.getDir(), CLASSNAME + ".java")));

		
		
		try {
			out.println("package " + gpackage.getName() + ";");
			out.println();
			out.println(imports.toString());
			out.println();
			
			out.print(comment.toString(0));
			
			out.println("public class " + CLASSNAME + " extends " + MainBase.class.getSimpleName() + " {");
			indent(+1);
			out.println();

			
			for (GField constField : constFields) {
				out.println(constField.toString("\t"));
			}
			
			
			for (GField field : fields) {
				out.println(field.toString(indent));
			}
			out.println();
			
			//
			// Create constructor
			//
			out.println(indent + "public " + CLASSNAME + "(" + BlenderFile.class.getSimpleName() + " blendFile) throws " + IOException.class.getSimpleName() + "{");
			indent(+1);
			out.println(indent + "super(\""  + dnaPackage.getName() + "\", blendFile);");
			out.println();
			out.println(indent + "List<Block> globalBlock = blockTable.getBlocks(BlockHeader.CODE_GLOB);");
			out.println(indent + MEMBER_fileGlobal + " = null;");
			out.println(indent + "if (globalBlock.size() == 1) {");
			indent(+1);
			out.println(indent + "Block b = globalBlock.get(0);");
			out.println(indent + MEMBER_fileGlobal + " = new FileGlobal(b.header.getAddress(), blockTable);");
			indent(-1);
			out.println(indent + "}");
			out.println();
			indent(-1);
			out.println(indent + "}");
			out.println();
			
			
			for (GMethod method : methods) {
				out.println(method.toString(1));
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
		addGetMethod(field, gcomment);
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
		return CLASSNAME;
	}



}
