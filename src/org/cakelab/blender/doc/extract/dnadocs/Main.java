package org.cakelab.blender.doc.extract.dnadocs;

import java.io.File;
import java.io.IOException;

import org.cakelab.blender.io.FileHeader.Version;
import org.xml.sax.SAXException;

public class Main {

	
	
	
	private static String version;
	private static File input;
	private static File output;


	public static void main(String[] args) {
		setDefaults();


		readArguments(args);
		
		validateConfig();
		
		
		
		output = new File(output, getVersionFolderName(version));
		output = new File(output, "/dnasrc");
		output.mkdirs();
		
		output = new File(output, "/doc.json");

		
		Converter converter = new Converter(input, version, output);
		try {
			converter.run();
		} catch (SAXException | IOException e) {
			e.printStackTrace();
		}
	}

	
	private static String getVersionFolderName(String version) {
		Version v = new Version(version);
		return v.toString();
	}


	private static void validateConfig() {
		if (version == null || input == null) {
			System.err.println("error: missing arguments.");
			System.err.println();
			synopsis();
			System.exit(-1);
		}
	}


	private static void setDefaults() {
		version = "3.0";
		input = new File("/tmp/blender-" + version + "-xmldoc");
		output = new File("/tmp/blender-" + version + "-dnadoc");
	}


	private static void readArguments(String[] args) {
		
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
					if (!input.exists() || !input.canRead() || input.isFile()) {
						System.err.println("Can't read: " + value);
						System.exit(-1);
					}
				} else if (name.equals("-out")) {
					output = new File(value);
					if (!output.isDirectory() || !output.canWrite()) {
						System.err.println("Can't write to output folder: " + value);
						System.exit(-1);
					}
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
		
	}


	private static void synopsis() {
		Class<?> clazz = Main.class;
		System.err.println("Synopsis: java " + clazz.getName() + " -in doxygenXmlDir -out dnaDocOutputFolder -v blenderVersionStr");
		System.err.println("Example: java " + clazz.getName() + " -in /tmp/blender-2.78-doxml -out ./resources/dnadoc -v 2.78");
		System.err.println("\t\treads directory with doxygen xml output\n"
						 + "\t\tand generates a Java .Blend documentation for Blender v2.78 in file\n"
						 + "\t\t./resources/dnadoc/$version/dnasrc/doc.json");
	}

}
