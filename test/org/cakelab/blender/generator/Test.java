package org.cakelab.blender.generator;

import java.io.File;
import java.io.IOException;

import org.cakelab.json.JSONException;

public class Test {
	public static void main(String[] args) throws IOException, JSONException {
		File input = new File("cube.blend");
		File output = new File("../JavaBlendDemo/gen");
		String javaPackage = "org.blender";

		args = new String[]{
			"-in", input.getPath(),
			"-out", output.getPath(),
			"-p", javaPackage,
			"-c", "resources/dnadoc",
			"-u", "true",
			"-d", "true"
		};
		
		ModelGenerator.main(args);
		
	}
}
