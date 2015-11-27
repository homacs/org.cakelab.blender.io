package org.cakelab.blender.model;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;

import org.cakelab.blender.file.BlenderFile;
import org.cakelab.blender.file.block.Block;
import org.cakelab.blender.file.block.BlockHeader;
import org.cakelab.blender.file.block.BlockMap;
import org.cakelab.blender.file.dna.BlendModel;
import org.cakelab.blender.file.dna.BlendStruct;
import org.cakelab.blender.file.util.Identifier;
import org.cakelab.blender.model.gen.type.Renaming;

public class MainBase {
	private BlockMap blockMap  = new BlockMap();
	private String packageName;

	public MainBase(String packageName, BlenderFile blend) throws IOException {
		this.packageName = packageName;
		ArrayList<Block> blocks = blend.readBlocks();
		BlendModel model = blend.readBlenderModel();
		blockMap.addAll(blocks);
		for (Block block : blocks) {
			BlockHeader header = block.header;
			if (isPossibleLibraryBlock(header.getCode())) {
				BlendStruct struct = model.getStruct(block.header.getSdnaIndex());
				if (BlendModel.isListElement(struct)) {
					addLibraryElements(block, struct);
				}
			}
		}
	}

	@SuppressWarnings("unchecked")
	private void addLibraryElements(Block block, BlendStruct struct) throws IOException {
		short size = struct.getType().getSize();
		try {
			Class<? extends DNAFacet> clazz = (Class<? extends DNAFacet>) MainBase.class.getClassLoader().loadClass(packageName + '.' + Renaming.mapStruct2Class(struct.getType().getName()));
			int count = 0;
			for (long address = block.header.getAddress(); count < block.header.getCount();
					address += size) 
			{
				DNAFacet libElem = DNAFacet.__dna__newInstance(clazz, address, blockMap);
				addLibraryElement(libElem);
				count++;
			}
		} catch (InstantiationException | IllegalAccessException
				| IllegalArgumentException | InvocationTargetException
				| NoSuchMethodException | SecurityException | ClassNotFoundException e) {
			throw new IOException(e);
		}
	}

	private void addLibraryElement(DNAFacet libElem) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		
		String setMethodName = "set" + libElem.getClass().getSimpleName();

		for (Method method : getClass().getDeclaredMethods()) {
			if (method.getName().equals(setMethodName)) {
				method.invoke(this, libElem);
				return;
			}
		}
	}

	private boolean isPossibleLibraryBlock(Identifier code) {
		return !(code.equals(BlockHeader.CODE_DNA1) 
				|| code.equals(BlockHeader.CODE_ENDB)
				|| code.equals(BlockHeader.CODE_TEST)
				);
	}
	

}
