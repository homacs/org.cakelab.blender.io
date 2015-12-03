package org.cakelab.blender.generator;

import java.io.File;
import java.io.IOException;

import org.cakelab.json.JSONException;

public class Main {
	public static void main(String[] args) throws IOException, JSONException {
		String version = "2.69";
		File input = new File("cube.blend");
		File output = new File("../JavaBlendDemo/gen");
		String javaPackage = "org.blender";

		args = new String[]{
			"-v", version,
			"-in", input.getPath(),
			"-out", output.getPath(),
			"-p", javaPackage,
			"-d", "true"
		};
		
		ModelGenerator.main(args);
		
	}
}
