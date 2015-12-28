package org.cakelab.blender.io;

import java.io.File;
import java.io.IOException;

import org.cakelab.blender.io.block.Block;
import org.cakelab.blender.io.block.BlockCodes;
import org.cakelab.blender.io.block.BlockTable;
import org.cakelab.blender.io.dna.DNAModel;
import org.cakelab.blender.metac.CMetaModel;
import org.cakelab.blender.metac.CStruct;

public class Main {
	
	
	
	public static void main(String[] args) throws IOException {
		BlenderFile blender = new BlenderFile(new File("cube.blend"));
		DNAModel model = blender.getBlenderModel();
		BlockTable blockTable = blender.getBlockTable();
		blender.close();

		Encoding encoding = blender.getEncoding();


		//
		// retrieve the meta model, which is encoding independent
		//
		CMetaModel meta = new CMetaModel(model);
		
		//
		// Create a new block to store 1 scene struct
		//
		CStruct struct = (CStruct) meta.getType("Scene");
		int size = struct.sizeof(encoding.getAddressWidth());
		Block block = blockTable.allocate(BlockCodes.ID_SCE, size);

		// 
		// init the block to retrieve the scene struct
		//
		block.header.setSdnaIndex(struct.getSdnaIndex());
		
		//
		// get a facet of type Scene
		// 
		
	}
}
