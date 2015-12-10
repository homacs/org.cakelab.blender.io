package org.cakelab.blender.io;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import org.cakelab.blender.file.BlenderFile;
import org.cakelab.blender.file.Encoding;
import org.cakelab.blender.file.block.Block;
import org.cakelab.blender.file.block.BlockHeader;
import org.cakelab.blender.file.block.BlockMap;
import org.cakelab.blender.file.dna.BlendModel;
import org.cakelab.blender.generator.type.CStruct;
import org.cakelab.blender.generator.type.MetaModel;

public class Main {
	
	
	
	public static void main(String[] args) throws IOException {
		BlenderFile blender = new BlenderFile(new File("cube.blend"));
		BlendModel model = blender.getBlenderModel();
		ArrayList<Block> blocks = blender.getBlocks();
		blender.close();
		
		Encoding encoding = blender.getEncoding();
		
		BlockMap blockMap = new BlockMap(encoding);
		blockMap.addAll(blocks);

		//
		// retrieve the meta model, which is encoding independent
		//
		MetaModel meta = new MetaModel(model);
		
		//
		// Create a new block to store 1 scene struct
		//
		CStruct struct = (CStruct) meta.getType("Scene");
		int size = struct.sizeof(encoding.getAddressWidth());
		Block block = blockMap.allocate(BlockHeader.CODE_SCE, size);

		// 
		// init the block to retrieve the scene struct
		//
		block.header.setSdnaIndex(struct.getSdnaIndex());
		
		//
		// get a facet of type Scene
		// 
		
	}
}
