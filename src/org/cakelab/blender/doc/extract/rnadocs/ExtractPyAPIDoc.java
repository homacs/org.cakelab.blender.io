package org.cakelab.blender.doc.extract.rnadocs;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;

import org.cakelab.blender.doc.Documentation;
import org.cakelab.json.JSONArray;
import org.cakelab.json.JSONObject;

/**
 * Class used to extract Blender's python API documentation received by executing
 * the rna_info.py script.
 * 
 * It is recommended to use the shell script extract-pyapi-docs.sh for this task.
 * The shell script is located in JavaBlendDocs repository. 
 * 
 * @author homac
 *
 */
public class ExtractPyAPIDoc extends Documentation {
	
	private BufferedReader in;
	
	public ExtractPyAPIDoc(File file, String version, File out) throws IOException {
		in = new BufferedReader(new FileReader(file));
		
		includePath = null;
		system = "Blender";
		module = "DNA";
		source = "Blender Python API";
		this.version = version;

		this.structdocs = new JSONObject();
		
		String line;
		while (null != (line = in.readLine())) {
			parse(line);
		}
		out = new File(out, version);
		out = new File(out, "pyapi");
		out.mkdirs();
		out = new File(out, "doc.json");
		super.write(out);
		
	}

	private void parse(String line) {
		// An output line of the python script looks like this:
		//
		// BaseClass|SubClass.member -> sdnaMember:   type   documentation<NL>
		//
		
		
		line = line.trim();
		int i = line.indexOf(":");
		if (i<0) return; // empty line
		String name = line.substring(0, i);
		String value = line.substring(i+1);
		
		//
		// parse name portion
		//
		String[] structNames = name.split("\\|");
		name = structNames[structNames.length-1];

		String[] baseStructNames = Arrays.copyOf(structNames, structNames.length-1);
		
		i = name.indexOf(".");
		String structName = name.substring(0, i);
		String sdnaFieldPrefix = " -> ";
		i = name.indexOf(sdnaFieldPrefix);
		String memberName = name.substring(i+sdnaFieldPrefix.length()).trim();
		
		//
		// parse value portion
		//
		value = value.trim();
		i = value.indexOf(" ");
		if (i<0) return; // no doc
		
		// String type = value.substring(0, i);
		
		String documentation = value.substring(i).trim();
		
		
		addFieldDoc(baseStructNames, structName, memberName, documentation);
		
	}

	public void addFieldDoc(String[] baseStructNames, String structName, String memberName,
			String documentation) {
		JSONObject struct = (JSONObject) structdocs.get(structName);
		if (struct == null) {
			struct = new JSONObject();
			structdocs.put(structName, struct);
		}
		
		if (baseStructNames.length > 0) {
			JSONArray baseclasses = new JSONArray();
			struct.put("inherits", baseclasses);
			for (String baseStructName : baseStructNames) {
				baseclasses.add(baseStructName);
			}
		}
		JSONObject fieldsdoc = (JSONObject) struct.get("fields");
		if (fieldsdoc == null) {
			fieldsdoc = new JSONObject();
			struct.put("fields", fieldsdoc);
		}

		fieldsdoc.put(memberName, documentation);
	}

	public static void main(String[] args) throws IOException {

		String version = null;
		
		File input = null;
		File output = new File("resources/dnadoc");
		
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
		//
		// print help if arguments are missing
		//
		if (version == null || input == null) {
			System.err.println("error: missing arguments.");
			System.err.println();
			synopsis();
			System.exit(-1);
		}
		
		//
		// convert to Java Blend documentation system.
		//
		new ExtractPyAPIDoc(input, version, output);
		
	}

	private static void synopsis() {
		Class<?> clazz = ExtractPyAPIDoc.class;
		System.err.println("Synopsis: java " + clazz.getName() + " -in docTextFile -out outputFolder -v blenderVersionStr");
		System.err.println("Example: java " + clazz.getName() + " -in pyapi.txt -out ./resources/dnadoc -v 2.69");
		System.err.println("\t\treads documentation text file pyapi.txt retreived from blender\n"
						 + "\t\tv2.69 and generates a Java Blend documentation in folder\n"
						 + "\t\t./resources/dnadoc");
	}

}
