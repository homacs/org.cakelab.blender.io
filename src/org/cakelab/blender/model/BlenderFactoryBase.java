package org.cakelab.blender.model;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;

import org.cakelab.blender.io.BlenderFile;
import org.cakelab.blender.io.block.Block;
import org.cakelab.blender.io.block.BlockTable;
import org.cakelab.blender.io.dna.internal.StructDNA;
import org.cakelab.blender.io.util.BigEndianInputStreamWrapper;
import org.cakelab.blender.io.util.Identifier;

public class BlenderFactoryBase {
	protected static class BlenderFileImplBase extends BlenderFile {

		protected BlenderFileImplBase(BigEndianInputStreamWrapper in) throws IOException {
			super(in);
			this.io = in;
		}

		protected BlenderFileImplBase(File file, StructDNA sdna, int blenderVersion) throws IOException {
			super(file, sdna, blenderVersion);
		}
		
	}

	
	@SuppressWarnings("unchecked")
	public static <T extends DNAFacet> T newDNAStructBlock(Identifier blockCode, Class<T> facetClass, BlenderFile blend) throws IOException {
		BlockTable blockTable = blend.getBlockTable();
		try {
			Field field__dna__sdnaIndex = facetClass.getDeclaredField("__dna__sdnaIndex");
			int sdnaIndex = field__dna__sdnaIndex.getInt(null);
			Block block = blockTable.allocate(blockCode, DNAFacet.__dna__sizeof(facetClass, blend.getEncoding().getAddressWidth()), sdnaIndex, 1);
			return (T)DNAFacet.__dna__newInstance(facetClass, block.header.getAddress(), blockTable);
		} catch (IllegalArgumentException | InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException | SecurityException e) {
			throw new IOException(e);
		} catch (NoSuchFieldException e) {
			throw new IOException("you cannot instantiate pointers or arrays this way. Use the appropriate factory methods for the respective types instead.", e);
		}
	}
	
	public static <T extends DNAFacet> DNAPointer<T> newDNAStructBlock(Identifier blockCode, Class<T> facetClass, int count, BlenderFile blend) throws IOException {
		BlockTable blockTable = blend.getBlockTable();
		try {
			Field field__dna__sdnaIndex = facetClass.getDeclaredField("__dna__sdnaIndex");
			int sdnaIndex = field__dna__sdnaIndex.getInt(null);
			Block block = blockTable.allocate(blockCode, DNAFacet.__dna__sizeof(facetClass, blend.getEncoding().getAddressWidth()), sdnaIndex, count);
			return new DNAPointer<T>(block.header.getAddress(), new Class[]{facetClass}, blockTable);
		} catch (IllegalArgumentException | IllegalAccessException | SecurityException e) {
			throw new IOException(e);
		} catch (NoSuchFieldException e) {
			throw new IOException("you cannot instantiate pointers or arrays this way. Use the appropriate factory methods for the respective types instead.", e);
		}
	}

	public static <T> DNAArray<T> newDNAArrayBlock(Identifier blockCode, Class<T> componentType, int arrayLength, BlenderFile blend) throws IOException {
		if (DNAFacet.__dna__subclassof(componentType, DNAArray.class)) {
			throw new IOException("Multi-dimensional arrays have to be instantiated giving all component types of the embedded arrays and their lengths.");
		} else if (DNAFacet.__dna__subclassof(componentType, DNAPointer.class)) {
			throw new IOException("Arrays of pointers have to be instantiated giving all types of the pointer, too.");
		} else {
			int[] dimensions = new int[]{arrayLength};
			Class<?>[] typeList = new Class<?>[]{componentType};
			return newDNAArrayBlock(blockCode, typeList, dimensions, blend);
		}
	}
	
	public static <T> DNAArray<T> newDNAArrayBlock(Identifier blockCode, Class<?>[] typeList, int[] dimensions, BlenderFile blend) throws IOException {
		BlockTable blockTable = blend.getBlockTable();
		// TODO: ist der sdnaIndex bei DATA blocks eventuell der typeIndex?
		int sdnaIndex = 0; // not a struct
		Class<?> elementaryType = typeList[dimensions.length-1];
		long size = DNAArray.__dna__sizeof(elementaryType, dimensions, blend.getEncoding());
		Block block = blockTable.allocate(blockCode, size);
		block.header.setCount(dimensions[0]);
		block.header.setSdnaIndex(sdnaIndex);
		return new DNAArray<T>(block.header.getAddress(), typeList, dimensions, blockTable);
	}
	
	/**
	 * This method creates a block with a single pointer in it. The returned pointer
	 * is a pointer on this created pointer. To change the value of that pointer use
	 * the method {@link DNAPointer#set(Object)} of the pointer on the pointer.
	 * 
	 * @param blockCode
	 * @param typeList
	 * @param blend
	 * @return a pointer on the pointer stored in the block.
	 * @throws IOException
	 */
	public static <T> DNAPointer<DNAPointer<T>> newDNAPointerBlock(Identifier blockCode, Class<?>[] typeList, BlenderFile blend) throws IOException {
		BlockTable blockTable = blend.getBlockTable();
		// TODO: ist der sdnaIndex bei DATA blocks eventuell der typeIndex?
		int sdnaIndex = 0; // not a struct
		int count = 0;
		int size = blend.getEncoding().getAddressWidth();
		Block block = blockTable.allocate(blockCode, size);
		block.header.setCount(count);
		block.header.setSdnaIndex(sdnaIndex);
		Class<?>[] typeListExtended = new Class<?>[typeList.length+1];
		System.arraycopy(typeList, 0, typeListExtended, 1, typeList.length);
		typeListExtended[0] = DNAPointer.class;
		return new DNAPointer<DNAPointer<T>>(block.header.getAddress(), typeList, blockTable);
	}
	
	/**
	 * This method creates a block with a set of pointers and returns a mutable pointer
	 * on the first pointer in it. The pointer is mutable to allow iteration over the
	 * set of pointers in the block.  To change the value of that pointer use
	 * the method {@link DNAPointerMutable#set(Object)} of the pointer on the pointer.
	 * 
	 * @param blockCode
	 * @param typeList
	 * @param count
	 * @param blend
	 * @return
	 * @throws IOException
	 */
	public static <T> DNAPointerMutable<DNAPointer<T>> newDNAPointerBlock(Identifier blockCode, Class<?>[] typeList, int count, BlenderFile blend) throws IOException {
		BlockTable blockTable = blend.getBlockTable();
		// TODO: ist der sdnaIndex bei DATA blocks eventuell der typeIndex?
		int sdnaIndex = 0; // not a struct
		int size = blend.getEncoding().getAddressWidth();
		Block block = blockTable.allocate(blockCode, size);
		block.header.setCount(count);
		block.header.setSdnaIndex(sdnaIndex);
		Class<?>[] typeListExtended = new Class<?>[typeList.length+1];
		System.arraycopy(typeList, 0, typeListExtended, 1, typeList.length);
		typeListExtended[0] = DNAPointer.class;
		return new DNAPointerMutable<DNAPointer<T>>(new DNAPointer<DNAPointer<T>>(block.header.getAddress(), typeList, blockTable));
	}
	
	/**
	 * This method provides the StructDNA from a given sdna.blend 
	 * image resource path. Every generated Java Blend data model
	 * has such an sdnaImage resource (actually a .blend file) which
	 * contains just the sdna meta data of the blender version the
	 * model was generated from.
	 * 
	 * @return 
	 * @throws IOException
	 */
	protected static StructDNA createStructDNA(String resourcePathTo_sdna_blend) throws IOException {
		InputStream in = BlenderFactoryBase.class.getClassLoader().getResourceAsStream("org/blender/resources/sdna.blend");
		BlenderFileImplBase sdnaImage = new BlenderFileImplBase(new BigEndianInputStreamWrapper(in, 8));
		StructDNA sdna = sdnaImage.getStructDNA();
		sdnaImage.close();
		return sdna;
	}
}
