package org.cakelab.blender.utils;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import org.cakelab.blender.generator.typemap.Renaming;
import org.cakelab.blender.io.BlenderFile;
import org.cakelab.blender.io.block.Block;
import org.cakelab.blender.io.block.BlockCodes;
import org.cakelab.blender.io.block.BlockHeader;
import org.cakelab.blender.io.block.BlockTable;
import org.cakelab.blender.io.dna.DNAField;
import org.cakelab.blender.io.dna.DNAModel;
import org.cakelab.blender.io.dna.DNAStruct;
import org.cakelab.blender.io.util.Identifier;
import org.cakelab.blender.metac.CField;
import org.cakelab.blender.metac.CStruct;
import org.cakelab.blender.nio.CFacade;

/**
 * Blender organises data in so-called libraries. A main library contains
 * all root elements found in a blender file. This is the base class with
 * several helper methods for the actual class <code>MainLib</code>, which will be generated
 * based on the meta data found in the Blender file. 
 * 
 * @author homac
 *
 */
public abstract class MainLibBase {
	protected BlockTable blockTable;
	private String packageName;
	protected BlenderFile blenderFile;

	protected MainLibBase(String packageName, BlenderFile blend) throws IOException {
		this.packageName = packageName;
		this.blenderFile = blend;
		DNAModel model = blend.getBlenderModel();
		blockTable = blend.getBlockTable();
		List<Block> blocks = blend.getBlocks();
		for (Block block : blocks) {
			BlockHeader header = block.header;
			if (isPossibleLibraryBlock(header.getCode())) {
				DNAStruct struct = model.getStruct(block.header.getSdnaIndex());
				if (isLibraryElement(struct)) {
					addLibraryElements(block, struct);
				}
			}
		}
	}

	@SuppressWarnings("unchecked")
	private void addLibraryElements(Block block, DNAStruct struct) throws IOException {
		short size = struct.getType().getSize();
		try {
			Class<? extends CFacade> clazz = (Class<? extends CFacade>) MainLibBase.class.getClassLoader().loadClass(packageName + '.' + Renaming.mapStruct2Class(struct.getType().getName()));
			int count = 0;
			for (long address = block.header.getAddress(); count < block.header.getCount();
					address += size) 
			{
				CFacade libElem = CFacade.__io__newInstance(clazz, address, block, blockTable);
				addLibraryElement(libElem);
				count++;
			}
		} catch (InstantiationException | IllegalAccessException
				| IllegalArgumentException | InvocationTargetException
				| NoSuchMethodException | SecurityException | ClassNotFoundException e) {
			throw new IOException(e);
		}
	}

	private void addLibraryElement(CFacade libElem) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException, IOException {
		
		String methodName = libElem.getClass().getSimpleName();
		methodName = Character.toUpperCase(methodName.charAt(0)) + methodName.substring(1);
		
		String getMethodName = "get" + methodName;
		Method getter = null;
		try {
			getter = getClass().getDeclaredMethod(getMethodName);
		} catch (NoSuchMethodException e) {
			// if there is no getter, than this lib elem was not considered when
			// main lib was generated. So, just ignore it.
			return;
		}

		CFacade first = (CFacade) getter.invoke(this);
		
		if (first == null) {
			String setMethodName = "set" + methodName;
			Method setter = getClass().getDeclaredMethod(setMethodName, libElem.getClass());
			setter.invoke(this, getFirst(libElem));
		}
	}

	protected abstract CFacade getFirst(CFacade libElem) throws IOException;

	private boolean isPossibleLibraryBlock(Identifier code) {
		return !(code.equals(BlockCodes.ID_DNA1) 
				|| code.equals(BlockCodes.ID_ENDB)
				|| code.equals(BlockCodes.ID_TEST)
				);
	}

	public static boolean isLibraryElement(CStruct struct) {
		ArrayList<CField> blendFields = struct.getFields();
		if (blendFields.size() > 0) {
			return blendFields.get(0).getType().getSignature().equals("ID");
		}
		return false;
	}




	private static boolean isLibraryElement(DNAStruct struct) {
		DNAField[] blendFields = struct.getFields();
		if (blendFields.length > 0) {
			return blendFields[0].getType().getName().equals("ID");
		}
		return false;
	}
	

}
