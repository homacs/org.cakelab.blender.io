package org.cakelab.blender.model.gen;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;

import org.cakelab.blender.file.BlenderFile;
import org.cakelab.blender.file.dna.BlendModel;
import org.cakelab.blender.file.dna.BlendStruct;
import org.cakelab.blender.file.dna.BlendType;
import org.cakelab.blender.model.gen.code.GPackage;

public class ModelGenerator {
	
	
	
	private BlendModel model;
	private HashMap<BlendType, BlendStruct> classes = new HashMap<BlendType, BlendStruct>();
	int pointerSize;


	public ModelGenerator(BlendModel model, int pointerSize) {
		this.model = model;
		this.pointerSize = pointerSize;
	}


	private void generate(File destinationDir, String packageName) throws IOException {
		if(!destinationDir.exists()) throw new IOException("Directory " + destinationDir + "does not exist");
		GPackage dnaPackage = new GPackage(destinationDir, packageName + ".dna");
		GPackage loaderPackage = new GPackage(destinationDir, packageName + ".lib");
		
		DNAFacetClassGenerator classgen = new DNAFacetClassGenerator(this, dnaPackage);
		MainLibClassGenerator libgen = new MainLibClassGenerator(this, loaderPackage, dnaPackage);

		for (BlendStruct struct : model.getStructs()) {
			if (!classes.containsKey(struct.getType())) {
				classgen.visit(struct);
				libgen.visit(struct);
			}
		}
		
		libgen.write();

	}

	
	public static void main(String[] args) throws IOException {
		BlenderFile blend = new BlenderFile(new File("cube.blend"));
		ModelGenerator generator = new ModelGenerator(blend.readBlenderModel(), blend.getPointerSize());
		blend.close();
		
		generator.generate(new File("../BlendGenResult/gen"), "org.blender");
		
	}
}
