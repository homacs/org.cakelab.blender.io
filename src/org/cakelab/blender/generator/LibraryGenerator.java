package org.cakelab.blender.generator;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import org.cakelab.blender.doc.Documentation;
import org.cakelab.blender.doc.DocumentationProvider;
import org.cakelab.blender.io.BlenderFile;
import org.cakelab.blender.io.FileVersionInfo;
import org.cakelab.blender.io.dna.DNAModel;
import org.cakelab.blender.io.dna.internal.StructDNA;
import org.cakelab.blender.io.dna.internal.StructDNA.Struct;
import org.cakelab.blender.metac.CMetaModel;
import org.cakelab.json.JSONException;

public class LibraryGenerator {

	public static void main(String[] args) throws IOException, JSONException {
		Options opts = Options.fromArguments(args);
		
		BlenderFile blend = new BlenderFile(opts.input);
		DNAModel model = blend.getBlenderModel();
		FileVersionInfo versionInfo = blend.readFileGlobal();
		blend.close();
		
		// This is for debugging purposes. If there are unknown types, than
		// it's usually because of new scalar types, that are not yet supported
		// by CMetaModel.isScalarType() and CMetaModel.getScalarSize()
		checkForUnsupportedTypes(blend.getStructDNA());
		
		printVersionInfos(blend, versionInfo);
		
		DocumentationProvider docs = loadSourceCodeDocumentation(opts, blend.getMetaModel(), versionInfo);
		
		generateModel(model, versionInfo, opts.output, opts.javaPackage, docs, opts.generateUtils, opts.debug);
		
		createNativeSdnaImage(blend, opts.output, opts.javaPackage);
		
		writeVersionProperties(blend, versionInfo, opts.output, opts.javaPackage);
	}
	

	private static void checkForUnsupportedTypes(StructDNA dna) {
		// Check if all types can be identified as one of the following
		//    scalar type
		//    struct type
		//    opaque type with unknown size (type_length == 0)
		// As it looks, opaque types are used for runtime data only and referenced by points,
		// so the unknown length does not affect the length calculation.
		
		Set<Short> structTypes = new HashSet<>();
		for (Struct s : dna.structs) {
			structTypes.add(s.type);
		}
		
		List<String> unsupported = new ArrayList<>();
		for (short type = 0; type < dna.types_len; type++) {
			if (!structTypes.contains(type)               // not struct
				&& !CMetaModel.isScalar(dna.types[type])  // not scalar
				&& dna.type_lengths[type] != 0)           // not opaque
			{
				unsupported.add(dna.types[type] + " " + dna.type_lengths[type]);
			}
		}
		
		if (!unsupported.isEmpty()) {
			System.err.println("There are unknown types:");
			for (String t : unsupported) {
				System.err.println("\t" + t);
			}
			throw new IllegalStateException("Unsupported types found. Please fix that first.");
		}
	}


	private static void printVersionInfos(BlenderFile blend, FileVersionInfo versionInfo) {
		System.out.println("Info: Blender version and file version:");
		System.out.println("\tVERSION: " + blend.getVersion());
		System.out.println("\tSUBVERSION: " + versionInfo.getSubversion());
		System.out.println("\tMINVERSION: " + versionInfo.getMinversion());
		System.out.println("\tMINSUBVERSION: " + versionInfo.getMinsubversion());
		System.out.println("Info: working directory: " + System.getProperty("user.dir"));
	}

	private static DocumentationProvider loadSourceCodeDocumentation(Options opts, CMetaModel model, FileVersionInfo versionInfo) throws IOException, JSONException {
		File docfolder = new File(opts.docpath);
		File[] docfiles = null;
		docfolder = Documentation.getDocFolder(docfolder, versionInfo);
		if (docfolder == null) {
			if (opts.userProvidedDocPath) {
				throw new Error("Missing documentation for version " + versionInfo.getVersion() + " at: " + opts.docpath);
			} else {
				System.err.println("Warning: can't find appropriate doc folder for version '" + versionInfo.getVersion() + "' in doc folder '" + opts.docpath.toString() + "'");
			}
			docfiles = new File[0];
		} else {
			System.out.println("Info: selected documentation: " + docfolder.getPath());
			docfiles = new File[] {
					new File(docfolder, "/added/doc.json"),
					new File(docfolder, "/pyapi/doc.json"),
					new File(docfolder, "/dnasrc/doc.json")
			};
		}
		return new DocGenerator(docfiles, model, opts.debug);
	}

	private static void generateModel(DNAModel model, FileVersionInfo versionInfo, File output, String javaPackage, DocumentationProvider docs, boolean generateUtils, boolean debug) throws IOException {
		System.out.println("Info: generating source code. ");
		System.out.println("\tOutput folder:    " + output);
		System.out.println("\tPackage:          " + javaPackage);
		System.out.println("\tUtils generation: " + (generateUtils ? "enabled" : "disabled"));
		ModelGenerator generator = new ModelGenerator(model, versionInfo, generateUtils);
		generator.generate(output, javaPackage, docs, debug);
	}

	private static void createNativeSdnaImage(BlenderFile blend, File output, String javaPackage) throws IOException {
		File resourcesDir = new File(output, javaPackage.replace('.', File.separatorChar) + File.separator + ModelGenerator.PACKAGE_UTILS + File.separator + "resources");
		resourcesDir.mkdirs();
		File sdnaImageFile = new File(resourcesDir, "sdna.blend");
		System.out.println("Info: creating sdna image at: " + sdnaImageFile.getAbsolutePath());
		sdnaImageFile.delete();
		sdnaImageFile.createNewFile();
		@SuppressWarnings("resource")
		StructDNAImageGenerator sdnaImage = new StructDNAImageGenerator(sdnaImageFile, blend.getStructDNA(), blend.getVersion().getCode());
		sdnaImage.generate();
	}

	private static void writeVersionProperties(BlenderFile blend, FileVersionInfo versionInfo, File output, String javaPackage) throws FileNotFoundException, IOException {
		String blendVersion = blend.getVersion().toString();
		String blendFileVersion = blend.getVersion() + "." + versionInfo.getSubversion();
		String blendFileMinVersion = versionInfo.getMinversion() + "." + versionInfo.getMinsubversion();
		
		File propertiesFile = new File(new File(output, javaPackage.replace('.',File.separatorChar)), "version.properties");
		Properties props = new Properties();
		props.setProperty("org.blender.version", blendVersion);
		props.setProperty("org.blender.file.version", blendFileVersion);
		props.setProperty("org.blender.file.minversion", blendFileMinVersion);
		
		if (propertiesFile.exists()) {
			// prevent updating the properties file, if content hasn't changed
			Properties existing = new Properties();
			try (InputStream in = new FileInputStream(propertiesFile)){
				existing.load(in);
				if (props.equals(existing)) {
					System.out.println("Info: existing version.properties has the same content: " + existing.toString());
					return;
				}
			} catch (Exception t) {
				// malformed i guess ... try overwriting it below
			}
		}
		try (OutputStream out = new FileOutputStream(propertiesFile)) {
			props.store(out, null);
			System.out.println("Info: written: " + propertiesFile.getAbsolutePath());
		}
	}

}
