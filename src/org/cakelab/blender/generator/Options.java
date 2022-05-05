package org.cakelab.blender.generator;

import java.io.File;

public class Options {

	// TODO: ZZZ use gopt for command line options
	File input = null;
	File output = null;
	String javaPackage = "org.cakelab.blender";
	String docpath = "resources/dnadoc";
	boolean userProvidedDocPath = false;
	boolean generateUtils = true;
	boolean debug = false;


	private static void synopsis() {
		// TODO: ZZZ document synopsis of model generator
		Class<?> clazz = ModelGenerator.class;
		System.out.println("Synopsis: java " + clazz.getName() + " -in input.blend -out output/src/path [-p \"target.package.name\"] [-u true]");
		System.out.println("Example: java " + clazz.getName() + " -in cube.blend -out ../project/gen -p org.blender");
		System.out.println("         reads type and version info from cube.blend and generates classes in folder ../project/gen/org/blender");
		System.out.println("         Use \"-d true\" to get additional debug information during class generation.");
	}
	
	
	public static Options fromArguments(String[] args) {
		Options opts = new Options();
		opts.readArgs(args);
		return opts;
	}
	
	void readArgs(String[] args) {
		
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
					userProvidedDocPath = true;
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
		
	}
	
}
