package org.cakelab.blender.io;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import org.cakelab.blender.file.BlenderFile;
import org.cakelab.blender.file.block.Block;
import org.cakelab.blender.file.block.BlockHeader;

public class Main {
	
	
	
	public static void main(String[] args) throws IOException {
		BlenderFile blender = new BlenderFile(new File("cube.blend"));
		ArrayList<Block> blocks = blender.readBlocks();
		for (Block block : blocks) {
			System.out.println(block.header.getCode().toString());
			if (block.header.getCode().equals(BlockHeader.CODE_ID)) {
				System.out.println(block.header.getCode().toString());
			} else if (block.header.getCode().equals(BlockHeader.CODE_LI)) {
				System.out.println(block.header.getCode().toString());
			}
		}
		blender.close();
	}
}
