package org.cakelab.blender.generator;

import java.io.File;
import java.io.IOException;

import org.cakelab.json.JSONException;

public class Test {
	public static void main(String[] args) throws IOException, JSONException {
		String home = System.getProperty("user.home");
		String version = "2.69";
		String blender_prefs = home + "/.config/blender/" + version + "/config/userpref.blend";
		File input = new File(blender_prefs);
		File output = new File("../JavaBlendDemo/gen");
		String javaPackage = "org.blender";

		args = new String[]{
			"-in", input.getPath(),
			"-out", output.getPath(),
			"-p", javaPackage,
			"-c", "../JavaBlendDocs/resources/dnadoc",
			"-u", "true",
			"-d", "true"
		};
		
		ModelGenerator.main(args);
		
	}
}
