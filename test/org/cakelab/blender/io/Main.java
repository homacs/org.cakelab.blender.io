package org.cakelab.blender.io;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import org.cakelab.blender.file.BlenderFile;
import org.cakelab.blender.file.block.Block;
import org.cakelab.blender.file.block.BlockHeader;
import org.cakelab.blender.file.block.BlockMap;
import org.cakelab.blender.file.dna.BlendModel;
import org.cakelab.blender.file.dna.BlendStruct;

public class Main {
	
	
	
	public static void main(String[] args) throws IOException {
		BlenderFile blender = new BlenderFile(new File("cube.blend"));
		BlendModel model = blender.readBlenderModel();
		ArrayList<Block> blocks = blender.readBlocks();
		blender.close();
		
		BlockMap blockMap = new BlockMap(blender.getEncoding());
		blockMap.addAll(blocks);
		
		BlendStruct struct = model.getStruct("Scene");
		// TODO: size is blender file specific!
		int size = struct.getType().getSize();
		blockMap.allocate(BlockHeader.CODE_SCE, size);
		
	}
}
