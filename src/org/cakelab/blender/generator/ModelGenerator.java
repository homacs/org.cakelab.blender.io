package org.cakelab.blender.generator;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;

import org.cakelab.blender.doc.Documentation;
import org.cakelab.blender.doc.DocumentationProvider;
import org.cakelab.blender.generator.utils.GPackage;
import org.cakelab.blender.io.BlenderFile;
import org.cakelab.blender.io.FileVersionInfo;
import org.cakelab.blender.io.dna.DNAModel;
import org.cakelab.blender.metac.CMetaModel;
import org.cakelab.blender.metac.CStruct;
import org.cakelab.json.JSONException;

public class ModelGenerator {
	
	
	private static final String PACKAGE_LIB = "utils";
	private CMetaModel model;
	private HashSet<CStruct> classes = new HashSet<CStruct>();
	private FileVersionInfo versionInfo;
	private boolean generateUtils;


	public ModelGenerator(DNAModel model, FileVersionInfo versionInfo, boolean generateUtils) {
		this.versionInfo = versionInfo;
		this.generateUtils = generateUtils;
		this.model = new CMetaModel(model);
	}

	private void generate(File destinationDir, String packageName, DocumentationProvider docs, boolean debug) throws IOException {
		if(!destinationDir.exists()) throw new IOException("Directory " + destinationDir + " does not exist");
		GPackage dnaPackage = new GPackage(destinationDir, packageName + ".dna");
		GPackage loaderPackage = new GPackage(destinationDir, packageName + "." + PACKAGE_LIB);
		
		CFacadeClassGenerator classgen = new CFacadeClassGenerator(this, dnaPackage, docs);
		MainLibClassGenerator libgen = null;
		FactoryClassGenerator facgen = null;
		if (generateUtils) {
			libgen = new MainLibClassGenerator(this, loaderPackage, dnaPackage, docs);
			facgen = new FactoryClassGenerator(this, loaderPackage, dnaPackage, docs);
		}
		for (CStruct struct : model.getStructs()) {
			if (!classes.contains(struct)) {
				classgen.visit(struct);
				if (generateUtils) libgen.visit(struct);
			}
		}
		
		if (generateUtils) {
			libgen.write();
			facgen.write();
		}
	}

	
	
	public static void main(String[] args) throws IOException, JSONException {
		// TODO: ZZZ use gopt for command line options
		File input = null;
		File output = null;
		String javaPackage = "org.cakelab.blender";
		String docpath = "resources/dnadoc";
		boolean generateUtils = true;
		boolean debug = true;
		
		for (int i = 0; i < args.length; i++) {
			String name = args[i++];
			String value;
			if (i == args.length) {
				System.err.println("missing parameter for argument " + name);
				System.exit(-1);
			} else {
				value = args[i];
				// TODO: ZZZ add long options
				if (name.equals("-u")) {
					generateUtils = Boolean.valueOf(value);
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
				} else if (name.equals("-c")) {
					docpath = value;
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
		
		if (input == null || output == null || javaPackage == null) {
			System.err.println("error: missing arguments.");
			System.err.println();
			synopsis();
			System.exit(-1);
		}
		
		
		
		//
		// gather required resources
		//
		BlenderFile blend = new BlenderFile(input);
		DNAModel model = blend.getBlenderModel();
		FileVersionInfo versionInfo = blend.readFileGlobal();
		blend.close();
		
		//
		// Be verbose
		//
		System.out.println("Info: Blender version and file version:");
		System.out.println("\tVERSION: " + blend.getVersion().toString());
		System.out.println("\tSUBVERSION: " + versionInfo.getSubversion());
		System.out.println("\tMINVERSION: " + versionInfo.getMinversion());
		System.out.println("\tMINSUBVERSION: " + versionInfo.getMinsubversion());
		System.out.println("Info: working directory: " + System.getProperty("user.dir"));
		
		//
		// load source code documentation
		//
		File docfolder = new File(docpath);
		File[] docfiles = null;
		docfolder = Documentation.getDocFolder(docfolder, versionInfo);
		if (docfolder == null) {
			System.err.println("Warning: can't find appropriate doc folder for version '" + versionInfo.getVersion() + "' in doc folder '" + docpath.toString() + "'");
			docfiles = new File[0];
		} else {
			System.out.println("Info: selected documentation: " + docfolder.getPath());
			docfiles = new File[] {
					new File(docfolder, "/added/doc.json"),
					new File(docfolder, "/pyapi/doc.json"),
					new File(docfolder, "/dnasrc/doc.json")
			};
		}
		DocumentationProvider docs = new DocGenerator(docfiles, debug);

		//
		// generate model
		//
		System.out.println("Info: generating source code. ");
		System.out.println("\tOutput folder:    " + output);
		System.out.println("\tPackage:          " + javaPackage);
		System.out.println("\tUtils generation: " + (generateUtils ? "enabled" : "disabled"));
		ModelGenerator generator = new ModelGenerator(model, versionInfo, generateUtils);
		generator.generate(output, javaPackage, docs, debug);
		
		//
		// create native sdna image
		//
		File resourcesDir = new File(output, javaPackage.replace('.', File.separatorChar) + File.separator + PACKAGE_LIB + File.separator + "resources");
		resourcesDir.mkdirs();
		File sdnaImageFile = new File(resourcesDir, "sdna.blend");
		System.out.println("Info: creating sdna image at: " + sdnaImageFile.getAbsolutePath());
		sdnaImageFile.delete();
		sdnaImageFile.createNewFile();
		@SuppressWarnings("resource")
		StructDNAImageGenerator sdnaImage = new StructDNAImageGenerator(sdnaImageFile, blend.getStructDNA(), blend.getVersion().getCode());
		sdnaImage.generate();
	}


	private static void synopsis() {
		// TODO: ZZZ document synopsis of model generator
		Class<?> clazz = ModelGenerator.class;
		System.out.println("Synopsis: java " + clazz.getName() + " -in input.blend -out output/src/path [-p \"target.package.name\"] [-u true]");
		System.out.println("Example: java " + clazz.getName() + " -in cube.blend -out ../project/gen -p org.blender");
		System.out.println("         reads type and version info from cube.blend and generates classes in folder ../project/gen/org/blender");
		System.out.println("         Use \"-d true\" to get additional debug information during class generation.");
	}

	public FileVersionInfo getVersionInfo() {
		return versionInfo;
	}
}
