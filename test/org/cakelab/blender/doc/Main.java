package org.cakelab.blender.doc;

import java.io.File;
import java.io.IOException;

import org.cakelab.json.JSONException;

public class Main {

	public static void main (String [] args) throws IOException, JSONException {
		Documentation doc = new Documentation(new File("resources/dnadoc/2.69/doc.json"), true);
		
		System.out.println(doc.getStructDoc("ID"));
		System.out.println(doc.getFieldDoc("ID", "next"));
		System.out.println(doc.getFieldDoc("ID", "properties"));
		
	}


}
