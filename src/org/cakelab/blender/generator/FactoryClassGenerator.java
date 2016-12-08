package org.cakelab.blender.generator;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.List;

import org.cakelab.blender.doc.DocumentationProvider;
import org.cakelab.blender.generator.utils.ClassGenerator;
import org.cakelab.blender.generator.utils.GCodeSection;
import org.cakelab.blender.generator.utils.GComment;
import org.cakelab.blender.generator.utils.GComment.Type;
import org.cakelab.blender.generator.utils.GField;
import org.cakelab.blender.generator.utils.GMethod;
import org.cakelab.blender.generator.utils.GPackage;
import org.cakelab.blender.io.BlenderFile;
import org.cakelab.blender.io.block.Block;
import org.cakelab.blender.io.block.BlockCodes;
import org.cakelab.blender.io.block.BlockTable;
import org.cakelab.blender.io.dna.internal.StructDNA;
import org.cakelab.blender.utils.BlenderFactoryBase;

public class FactoryClassGenerator extends ClassGenerator implements CFacadeMembers {

	private static final String CLASSNAME = "BlenderFactory";
	private static final String MEMBER_sdna = "sdna";
	
	private GComment comment;


	public FactoryClassGenerator(ModelGenerator modelgen, GPackage gpackage, GPackage dnaPackage, DocumentationProvider docs2) {
		super(modelgen, gpackage, docs2);
		
		addImport(dnaPackage);
		addImport(File.class);
		addImport(IOException.class);
		addImport(Block.class);
		addImport(BlockCodes.class);
		addImport(BlockTable.class);
		addImport(StructDNA.class);
		addImport(BlenderFactoryBase.class);
		addImport(BlenderFile.class);
		addImport(List.class);
		
		comment = new GComment(Type.JavaDoc);
		comment.appendln();
		comment.appendln("Factory class to create blender files and blocks in it.");
		comment.appendln();
		GComment fieldDoc = new GComment(Type.JavaDoc);
		fieldDoc.appendln("Struct dna of the generated data model.");
		addField("protected static", StructDNA.class.getSimpleName(), MEMBER_sdna, fieldDoc);
		
		GMethod method = new GMethod(0);
		method.appendln("public BlenderFactory(BlenderFile blend) throws IOException {");
		method.indent(+1);
		method.appendln("super(blend);");
		method.indent(-1);
		method.appendln("}");
		addMethod(method);
		
		method = new GMethod(0);
		method.appendln("public static FileGlobal getFileGlobal(" + BlenderFile.class.getSimpleName() + " blend) throws " + IOException.class.getSimpleName() + " {");
		method.indent(+1);
		method.appendln(BlockTable.class.getSimpleName() + " blockTable = blend.getBlockTable();");
		method.appendln("List<Block> globalBlock = blockTable.getBlocks(BlockCodes.ID_GLOB);");
		method.appendln("FileGlobal fileGlobal = null;");
		method.appendln("if (globalBlock.size() == 1) {");
		method.indent(+1);
		method.appendln("Block b = globalBlock.get(0);");
		method.appendln("fileGlobal = new FileGlobal(b.header.getAddress(), b, blockTable);");
		method.indent(-1);
		method.appendln("}");
		method.appendln("return fileGlobal;");
		method.indent(-1);
		method.appendln("}");
		addMethod(method);

		method = new GMethod(0);
		method.appendln("public FileGlobal getFileGlobal() throws " + IOException.class.getSimpleName() + " {");
		method.indent(+1);
		method.appendln("return getFileGlobal(blend);");
		method.indent(-1);
		method.appendln("}");
		addMethod(method);
		
		method = new GMethod(0);
		method.appendln("public static BlenderFile newBlenderFile(File file) throws IOException {");
		method.indent(+1);
		method.appendln("StructDNA sdna = getStructDNA();");
		method.appendln("BlenderFileImpl blend = new BlenderFileImpl(file, sdna, MainLib.BLENDER_VERSION);");
		method.appendln("return blend;");
		method.indent(-1);
		method.appendln("}");
		addMethod(method);
		
		method = new GMethod(0);
		method.appendln("public static StructDNA getStructDNA() throws IOException {");
		method.indent(+1);
		method.appendln("if (sdna == null) {");
		method.indent(+1);
		method.appendln("sdna = createStructDNA(\"" + gpackage.getResourcePath() + "/resources/sdna.blend\");");
		method.indent(-1);
		method.appendln("}");
		method.appendln("return sdna;");
		method.indent(-1);
		method.appendln("}");
		addMethod(method);
		
		
	}


	private GCodeSection createEmbeddedClass() {
		GCodeSection code = new GCodeSection(0);
		code.appendln("static class BlenderFileImpl extends BlenderFileImplBase {");
		code.indent(+1);
		code.appendln();
		code.appendln("private FileGlobal global;");
		code.appendln();
		code.appendln("protected BlenderFileImpl(File file, StructDNA sdna, int blenderVersion) throws IOException {");
		code.indent(+1);
		code.appendln("super(file, sdna, blenderVersion);");
		code.appendln("BlockTable blockTable = getBlockTable();");
		code.appendln("Block block = blockTable.allocate(BlockCodes.ID_GLOB, FileGlobal." + __io__sizeof + "(FileGlobal.class, getEncoding().getAddressWidth()), FileGlobal." + __DNA__SDNA_INDEX + ", 1);");
		code.appendln("global = new FileGlobal(block.header.getAddress(), block, blockTable);");
		code.appendln("String filename = file.getCanonicalPath();");
		code.appendln("global.getFilename().fromString(filename);");
		code.appendln("global.setMinsubversion(MainLib.BLENDER_MINSUBVERSION);");
		code.appendln("global.setMinversion(MainLib.BLENDER_MINVERSION);");
		code.appendln("global.setSubversion(MainLib.BLENDER_SUBVERSION);");
		code.appendln("add(block);");
		code.indent(-1);
		code.appendln("}");
		code.appendln();
		code.indent(-1);
		code.appendln("}");
		return code;
	}



	public void write() throws FileNotFoundException {
		PrintStream out = new PrintStream(new FileOutputStream(new File(gpackage.getDir(), CLASSNAME + ".java")));

		GCodeSection classBlenderFileImpl = createEmbeddedClass();
		try {
			out.println("package " + gpackage.getName() + ";");
			out.println();
			out.println(imports.toString());
			out.println();
			
			out.print(comment.toString(0));
			
			out.println("public class " + CLASSNAME + " extends " + BlenderFactoryBase.class.getSimpleName() + " {");
			indent(+1);
			out.println();

			out.println(classBlenderFileImpl.toString(1));
			
			for (GField constField : constFields) {
				out.println(constField.toString(indent));
			}
			
			for (GField field : fields) {
				out.println(field.toString(indent));
			}
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
	public String getClassName() {
		return CLASSNAME;
	}



}
