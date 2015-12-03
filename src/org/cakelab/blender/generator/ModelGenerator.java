package org.cakelab.blender.generator;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;

import org.cakelab.blender.doc.DocumentationProvider;
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


	public ModelGenerator(BlendModel model, int pointerSize) {
		this.model = model;
		this.pointerSize = pointerSize;
	}


	private void generate(File destinationDir, String packageName, DocumentationProvider docs, boolean debug) throws IOException {
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
		// TODO: read version from file
		String version = null;
		File input = null;
		File output = null;
		String javaPackage = null;
		boolean debug = false;
		
		for (int i = 0; i < args.length; i++) {
			String name = args[i++];
			String value;
			if (i == args.length) {
				System.err.println("missing parameter for argument " + name);
				System.exit(-1);
			} else {
				value = args[i];

				if (name.equals("-v")) {
					version = value;
				} else if (name.equals("-in")) {
					input = new File(value);
					if (!input.exists() || !input.canRead() || input.isDirectory()) {
						System.err.println("Can't read: " + value);
						System.exit(-1);
					}
				} else if (name.equals("-out")) {
					output = new File(value);
				} else if (name.equals("-p")) {
					javaPackage = value;
				} else if (name.equals("-d")) {
					debug = Boolean.valueOf(value);
				} else {
					System.err.println("unknown argument " + name);
					System.exit(-1);
				}
			}
		}
		
		if (version == null || input == null || output == null || javaPackage == null) {
			System.err.println("error: missing arguments.");
			System.err.println();
			Class<?> clazz = ModelGenerator.class;
			System.err.println("synopsis: java " + clazz.getName() + " -in input.blend -out output/src/path -v blenderVersionStr -p target.package.name");
			System.err.println("example: java " + clazz.getName() + " -in cube.blend -out ../project/gen -v 2.69 -p org.blender");
			System.err.println("         reads type info from cube.blend which was stored from blender v2.69 and generates class in folder ../project/gen/org/blender");
			System.err.println("         Use \"-d true\" to get additional debug information during class generation.");
			System.exit(-1);
		}
		
		
		BlenderFile blend = new BlenderFile(input);
		ModelGenerator generator = new ModelGenerator(blend.readBlenderModel(), blend.getPointerSize());
		blend.close();
		File[] docfiles = {
				new File("resources/dnadoc/" + version + "/added/doc.json"),
				new File("resources/dnadoc/" + version + "/pyapi/doc.json"),
				new File("resources/dnadoc/" + version + "/dnasrc/doc.json")
		};
		DocumentationProvider docs = new DocGenerator(docfiles, debug);
		generator.generate(output, javaPackage, docs, debug);
	}
}
