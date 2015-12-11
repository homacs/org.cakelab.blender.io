package org.cakelab.blender.model;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;

import org.cakelab.blender.generator.type.CField;
import org.cakelab.blender.generator.type.CStruct;
import org.cakelab.blender.generator.type.Renaming;
import org.cakelab.blender.io.BlenderFile;
import org.cakelab.blender.io.block.Block;
import org.cakelab.blender.io.block.BlockHeader;
import org.cakelab.blender.io.block.BlockTable;
import org.cakelab.blender.io.dna.BlendField;
import org.cakelab.blender.io.dna.BlendModel;
import org.cakelab.blender.io.dna.BlendStruct;
import org.cakelab.blender.io.util.Identifier;

/**
 * Blender organises data in so-called libraries. A main library contains
 * all root elements found in a blender file. This is the base class with
 * several helper methods for the actual class <code>Main</code>, which will be generated
 * based on the meta data found in the Blender file. Refer to the generated
 * Main class for more details.
 * 
 * @author homac
 *
 */
public abstract class MainBase {
	protected BlockTable blockTable;
	private String packageName;
	protected BlenderFile __dna__blendFile;

	protected MainBase(String packageName, BlenderFile blend) throws IOException {
		this.packageName = packageName;
		this.__dna__blendFile = blend;
		BlendModel model = blend.getBlenderModel();
		blockTable = blend.getBlockMap();
		ArrayList<Block> blocks = blend.getBlocks();
		for (Block block : blocks) {
			BlockHeader header = block.header;
			if (isPossibleLibraryBlock(header.getCode())) {
				BlendStruct struct = model.getStruct(block.header.getSdnaIndex());
				if (isLibraryElement(struct)) {
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
				DNAFacet libElem = DNAFacet.__dna__newInstance(clazz, address, blockTable);
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

	public static boolean isLibraryElement(CStruct struct) {
		ArrayList<CField> blendFields = struct.getFields();
		if (blendFields.size() > 0) {
			return blendFields.get(0).getType().getSignature().equals("ID");
		}
		return false;
	}




	private static boolean isLibraryElement(BlendStruct struct) {
		BlendField[] blendFields = struct.getFields();
		if (blendFields.length > 0) {
			return blendFields[0].getType().getName().equals("ID");
		}
		return false;
	}
	

}
