package org.cakelab.blender.generator;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;

import org.cakelab.blender.doc.DocumentationProvider;
import org.cakelab.blender.generator.code.GPackage;
import org.cakelab.blender.generator.type.CStruct;
import org.cakelab.blender.generator.type.MetaModel;
import org.cakelab.blender.io.BlenderFile;
import org.cakelab.blender.io.FileVersionInfo;
import org.cakelab.blender.io.dna.BlendModel;
import org.cakelab.json.JSONException;

public class ModelGenerator {
	
	
	
	private MetaModel model;
	private HashSet<CStruct> classes = new HashSet<CStruct>();
	private FileVersionInfo versionInfo;


	public ModelGenerator(BlendModel model, FileVersionInfo versionInfo) {
		this.versionInfo = versionInfo;
		this.model = new MetaModel(model);
	}

	private void generate(File destinationDir, String packageName, DocumentationProvider docs, boolean debug) throws IOException {
		if(!destinationDir.exists()) throw new IOException("Directory " + destinationDir + "does not exist");
		GPackage dnaPackage = new GPackage(destinationDir, packageName + ".dna");
		GPackage loaderPackage = new GPackage(destinationDir, packageName + ".lib");
		
		DNAFacetClassGenerator classgen = new DNAFacetClassGenerator(this, dnaPackage, docs);
		MainLibClassGenerator libgen = new MainLibClassGenerator(this, loaderPackage, dnaPackage, docs);

		for (CStruct struct : model.getStructs()) {
			if (!classes.contains(struct)) {
				classgen.visit(struct);
				libgen.visit(struct);
			}
		}
		
		libgen.write();

	}

	
	
	public static void main(String[] args) throws IOException, JSONException {
		// TODO: read version from file
		// TODO: map file version to blender documentation version 
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
				} else if (name.equals("-h") || name.equals("--help") || name.equals("?")) {
					synopsis();
					System.exit(0);
				} else {
					System.err.println("unknown argument " + name);
					synopsis();
					System.exit(-1);
				}
			}
		}
		
		if (version == null || input == null || output == null || javaPackage == null) {
			System.err.println("error: missing arguments.");
			System.err.println();
			synopsis();
			System.exit(-1);
		}
		
		
		BlenderFile blend = new BlenderFile(input);
		FileVersionInfo versionInfo = blend.readFileGlobal();
		ModelGenerator generator = new ModelGenerator(blend.getBlenderModel(), versionInfo);
		blend.close();
		File[] docfiles = {
				new File("resources/dnadoc/" + version + "/added/doc.json"),
				new File("resources/dnadoc/" + version + "/pyapi/doc.json"),
				new File("resources/dnadoc/" + version + "/dnasrc/doc.json")
		};
		DocumentationProvider docs = new DocGenerator(docfiles, debug);
		generator.generate(output, javaPackage, docs, debug);
	}


	private static void synopsis() {
		Class<?> clazz = ModelGenerator.class;
		System.err.println("Synopsis: java " + clazz.getName() + " -in input.blend -out output/src/path -v blenderVersionStr -p target.package.name");
		System.err.println("Example: java " + clazz.getName() + " -in cube.blend -out ../project/gen -v 2.69 -p org.blender");
		System.err.println("         reads type info from cube.blend which was stored from blender v2.69 and generates class in folder ../project/gen/org/blender");
		System.err.println("         Use \"-d true\" to get additional debug information during class generation.");
	}

	public FileVersionInfo getVersionInfo() {
		return versionInfo;
	}
}
