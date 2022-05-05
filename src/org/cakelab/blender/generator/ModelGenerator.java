package org.cakelab.blender.generator;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;

import org.cakelab.blender.doc.DocumentationProvider;
import org.cakelab.blender.generator.utils.GPackage;
import org.cakelab.blender.io.FileVersionInfo;
import org.cakelab.blender.io.dna.DNAModel;
import org.cakelab.blender.metac.CMetaModel;
import org.cakelab.blender.metac.CStruct;
import org.cakelab.json.JSONException;

public class ModelGenerator {
	
	
	public static final String PACKAGE_UTILS = "utils";
	private CMetaModel model;
	private HashSet<CStruct> classes = new HashSet<CStruct>();
	private FileVersionInfo versionInfo;
	private boolean generateUtils;


	public ModelGenerator(DNAModel model, FileVersionInfo versionInfo, boolean generateUtils) {
		this.versionInfo = versionInfo;
		this.generateUtils = generateUtils;
		this.model = new CMetaModel(model);
	}

	void generate(File destinationDir, String packageName, DocumentationProvider docs, boolean debug) throws IOException {
		if(!destinationDir.exists()) throw new IOException("Directory " + destinationDir + " does not exist");
		GPackage dnaPackage = new GPackage(destinationDir, packageName + ".dna");
		GPackage loaderPackage = new GPackage(destinationDir, packageName + "." + PACKAGE_UTILS);
		
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

	
	
	public FileVersionInfo getVersionInfo() {
		return versionInfo;
	}

	/**
	 * @deprecated Use {@link LibraryGenerator#main(String[])} instead
	 */
	public static void main(String[] args) throws IOException, JSONException {
		LibraryGenerator.main(args);
	}

}
