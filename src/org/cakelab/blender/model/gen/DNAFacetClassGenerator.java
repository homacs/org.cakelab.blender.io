package org.cakelab.blender.model.gen;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;

import org.cakelab.blender.file.block.BlockMap;
import org.cakelab.blender.file.dna.BlendField;
import org.cakelab.blender.file.dna.BlendStruct;
import org.cakelab.blender.model.DNAFacet;
import org.cakelab.blender.model.DNATypeInfo;
import org.cakelab.blender.model.gen.code.ClassGenerator;
import org.cakelab.blender.model.gen.code.GPackage;
import org.cakelab.blender.model.gen.type.CType;
import org.cakelab.blender.model.gen.type.JavaType;
import org.cakelab.blender.model.gen.type.Renaming;


public class DNAFacetClassGenerator extends ClassGenerator {

	private DNAFacetGetMethodGenerator readgen;
	
	public DNAFacetClassGenerator(ModelGenerator modelGenerator, GPackage gpackage) 
	{
		super(modelGenerator, gpackage);
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
			readgen.visitField(offset, field, ctype, jtype);
			offset += sizeof(ctype);
		}
		
		String classname = Renaming.mapStruct2Class(struct.getType().getName());
		PrintStream out = new PrintStream(new FileOutputStream(new File(gpackage.getDir(), classname + ".java")));
		try {
			out.println("package " + gpackage + ";");
			out.println();
			out.println(imports.toString());
			out.println();
			
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

	private long sizeof(CType ctype) {
		switch(ctype.getTypetype()) {
		case TYPE_ARRAY:
			long totalLength = ctype.getArrayLength();
			for (ctype = ctype.getReferencedType(); 
				 ctype.getTypetype() == CType.CTypeType.TYPE_ARRAY; 
				 ctype = ctype.getReferencedType()) 
			{
				totalLength *= ctype.getArrayLength();
			}
			return sizeof(ctype) * totalLength;
		case TYPE_FUNCTION_POINTER:
		case TYPE_POINTER:
			return modelgen.pointerSize;
		case TYPE_SCALAR:
		case TYPE_STRUCT:
			return ctype.getSize();
		case TYPE_VOID:
		default:
			throw new IllegalArgumentException("sizeof can't determine size of '" + ctype.getName() + "'");
		}
	}



	@Override
	public void reset() {
		super.reset();
		readgen.reset();
	}

}
