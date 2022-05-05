package org.cakelab.blender.generator;

import java.io.File;
import java.io.IOException;

import org.cakelab.json.JSONException;

public class Test {
	public static void main(String[] args) throws IOException, JSONException {
		String home = System.getProperty("user.home");
		String version = "2.69";
		String blender_file = home + "/.config/blender/" + version + "/config/userpref.blend";
//		String blender_file = "versions/2.76.0-2.70.5.blend";
		File input = new File(blender_file);
		File output = new File("../JavaBlendDemo/gen");
		String javaPackage = "org.blender";

		args = new String[]{
			"-in", input.getPath(),
			"-out", output.getPath(),
			"-p", javaPackage,
			"-c", "../org.cakelab.blender.dnadoc/resources/dnadoc",
			"-u", "true",
			"-d", "true"
		};
		
		LibraryGenerator.main(args);
		
	}
}
