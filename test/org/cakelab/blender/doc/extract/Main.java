package org.cakelab.blender.doc.extract;

import java.io.File;
import java.io.IOException;

import org.cakelab.blender.doc.extract.rnadocs.ExtractPyAPIDoc;

public class Main {
	public static void main(String[] args) throws IOException {

		String version = "2.69";
		
		File input = new File("/tmp/pyapidoc-" + version + ".txt");
		File output = new File("resources/dnadoc");
		
		
		args = new String[]{
			"-v", version,
			"-in", input.getPath(),
			"-out", output.getPath()
		};

		//
		// convert to Java Blend documentation system.
		//
		new ExtractPyAPIDoc( input, version, output);
		
	}

}
