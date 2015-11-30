package org.cakelab.blender.generator;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;

import org.cakelab.blender.doc.Documentation;
import org.cakelab.blender.file.BlenderFile;
import org.cakelab.blender.file.dna.BlendModel;
import org.cakelab.blender.file.dna.BlendStruct;
import org.cakelab.blender.file.dna.BlendType;
import org.cakelab.blender.generator.code.GPackage;
import org.cakelab.json.JSONException;

public class ModelGenerator {
	
	
	
	private BlendModel model;
	private HashMap<BlendType, BlendStruct> classes = new HashMap<BlendType, BlendStruct>();
	int pointerSize;
	private Documentation docs;


	public ModelGenerator(BlendModel model, int pointerSize) {
		this.model = model;
		this.pointerSize = pointerSize;
	}


	private void generate(File destinationDir, String packageName, Documentation docs) throws IOException {
		this.docs = docs;
		if(!destinationDir.exists()) throw new IOException("Directory " + destinationDir + "does not exist");
		GPackage dnaPackage = new GPackage(destinationDir, packageName + ".dna");
		GPackage loaderPackage = new GPackage(destinationDir, packageName + ".lib");
		
		DNAFacetClassGenerator classgen = new DNAFacetClassGenerator(this, dnaPackage, docs);
		MainLibClassGenerator libgen = new MainLibClassGenerator(this, loaderPackage, dnaPackage, docs);

		for (BlendStruct struct : model.getStructs()) {
			if (!classes.containsKey(struct.getType())) {
				classgen.visit(struct);
				libgen.visit(struct);
			}
		}
		
		libgen.write();

	}

	
	public static void main(String[] args) throws IOException, JSONException {
		BlenderFile blend = new BlenderFile(new File("cube.blend"));
		ModelGenerator generator = new ModelGenerator(blend.readBlenderModel(), blend.getPointerSize());
		blend.close();
		Documentation docs = new Documentation(new File("resources/dnadoc/2.69/DNA_documentation.json"));
		generator.generate(new File("../JavaBlendDemo/gen"), "org.blender", docs);
	}
}
