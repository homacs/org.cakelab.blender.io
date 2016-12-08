package org.cakelab.blender.generator;

import java.io.File;
import java.io.IOException;

import org.cakelab.blender.io.BlenderFile;
import org.cakelab.blender.io.Encoding;
import org.cakelab.blender.io.block.BlockList;
import org.cakelab.blender.io.dna.internal.StructDNA;

public class StructDNAImageGenerator extends BlenderFile {

	public StructDNAImageGenerator(File file, StructDNA sdna, int blenderVersion)
			throws IOException {
		super(file, sdna, blenderVersion, Encoding.JAVA_NATIVE, null);
	}

	/**
	 * 
	 * @throws IOException
	 */
	public void generate() throws IOException {
		write(new BlockList());
		close();
	}
}
