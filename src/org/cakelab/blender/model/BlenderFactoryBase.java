package org.cakelab.blender.model;

import static org.cakelab.blender.generator.DNAFacetMembers.__dna__sdnaIndex;

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


/**
 * This is the base class for generated factory classes of a specific blender 
 * version.
 * <p>
 * Factory classes are part of the utils package and optional. This base class 
 * contains the public factory methods to create blocks for facets, arrays and 
 * pointers. It also contains some utilities to create a new blender file in a
 * derived factory class. Thus, if the generated factory is not available, one
 * can derive from this class to achive required functionality.
 * </p>
 * 
 * @author homac
 *
 */
public class BlenderFactoryBase {
	// TODO: ZZZ merge similar functionalities in factory methods
	protected static class BlenderFileImplBase extends BlenderFile {

		protected BlenderFileImplBase(BigEndianInputStreamWrapper in) throws IOException {
			super(in);
			this.io = in;
		}

		protected BlenderFileImplBase(File file, StructDNA sdna, int blenderVersion) throws IOException {
			super(file, sdna, blenderVersion);
		}
		
	}

	/**
	 * Allocate a new block for one instance of a C struct.
	 * <p>
	 * This method allocates a new block in the given blender file
	 * and assigns it to a facet of the given class (facetClass).
	 * </p>
	 * <b>Example:</b>
	 * <pre>
	 * BlenderFile blend = BlenderFactory.newBlenderFile(new File("my.blend"));
	 * Scene scene = BlenderFactory.newDNAStructBlock(BlockHeader.CODE_SCE, Scene.class, blend);
	 * </pre>
	 * 
	 * @param blockCode
	 * @param facetClass
	 * @param blend
	 * @return
	 * @throws IOException
	 */
	@SuppressWarnings("unchecked")
	public static <T extends DNAFacet> T newDNAStructBlock(Identifier blockCode, Class<T> facetClass, BlenderFile blend) throws IOException {
		BlockTable blockTable = blend.getBlockTable();
		try {
			Field field__dna__sdnaIndex = facetClass.getDeclaredField(__dna__sdnaIndex);
			int sdnaIndex = field__dna__sdnaIndex.getInt(null);
			Block block = blockTable.allocate(blockCode, DNAFacet.__dna__sizeof(facetClass, blend.getEncoding().getAddressWidth()), sdnaIndex, 1);
			return (T)DNAFacet.__dna__newInstance(facetClass, block.header.getAddress(), blockTable);
		} catch (IllegalArgumentException | InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException | SecurityException e) {
			throw new IOException(e);
		} catch (NoSuchFieldException e) {
			throw new IOException("you cannot instantiate pointers or arrays this way. Use the appropriate factory methods for the respective types instead.", e);
		}
	}
	
	/**
	 * Allocate a new block for multiple instances of a C struct.
	 * <p>
	 * This method allocates a new block of apropriate size to fit 'count'
	 * instances of the given C struct in the given blender file
	 * and assigns it to an array facet of the given class type (facetClass).
	 * </p>
	 * <b>Example:</b>
	 * <pre>
	 * BlenderFile blend = BlenderFactory.newBlenderFile(new File("my.blend"));
	 * DNAArray&lt;Scene&gt; scene = BlenderFactory.newDNAStructBlock(BlockHeader.CODE_SCE, Scene.class, 2, blend);
	 * </pre>
	 * 
	 * 
	 * @param blockCode
	 * @param facetClass
	 * @param count
	 * @param blend
	 * @return
	 * @throws IOException
	 */
	public static <T extends DNAFacet> DNAArray<T> newDNAStructBlock(Identifier blockCode, Class<T> facetClass, int count, BlenderFile blend) throws IOException {
		BlockTable blockTable = blend.getBlockTable();
		try {
			Field field__dna__sdnaIndex = facetClass.getDeclaredField(__dna__sdnaIndex);
			int sdnaIndex = field__dna__sdnaIndex.getInt(null);
			Block block = blockTable.allocate(blockCode, DNAFacet.__dna__sizeof(facetClass, blend.getEncoding().getAddressWidth()), sdnaIndex, count);
			return new DNAArray<T>(block.header.getAddress(), new Class[]{facetClass}, new int[]{count}, blockTable);
		} catch (IllegalArgumentException | IllegalAccessException | SecurityException e) {
			throw new IOException(e);
		} catch (NoSuchFieldException e) {
			throw new IOException("you cannot instantiate pointers or arrays this way. Use the appropriate factory methods for the respective types instead.", e);
		}
	}

	/**
	 * Allocate a new block for one instance of a <b>one-dimensional</b> array of 
	 * any <b>non-pointer</b> component type which is either a scalar or a DNA struct.
	 * <p>
	 * This method allocates a new block of appropriate size to fit an array of 
	 * the given arrayLength and the given componentType in the given blender file
	 * and assigns it to an array facet.
	 * </p>
	 * <b>Example:</b>
	 * <pre>
		// 1-dim float array with 4 elems
		DNAArray&lt;Float&gt; rgba = BlenderFactory.newDNAArrayBlock(BlockHeader.CODE_DATA, Float.class, 4, blend);
	 * </pre>
	 * 
	 * @param blockCode Code of the new block. Frequently CODE_DATA for arrays.
	 * @param componentType Component type of the array.
	 * @param arrayLength length of the array
	 * @param blend blender file to add block to.
	 * @return DNAArray facet to access array data in the new block.
	 * @throws IOException
	 */
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
	
	/**
	 * Allocate a new block for one instance of a <b>multi-dimensional</b> array of 
	 * any component type supported by blender.
	 * <p>
	 * This method allocates a new block of appropriate size to fit an array of 
	 * the given arrayLength and the given component type specification (typeList) 
	 * and the given lengths for each dimension, in the given blender file and 
	 * assigns it to an array facet. Refer to {@link DNAArray} and {@link DNAPointer} 
	 * to understand the concept of type specifications by type lists.
	 * </p>
	 * <b>Example:</b>
	 * <pre>
	 *	// 2-dim 4x4 float array
	 *	Class&lt;?&gt;[] typeList = new Class[]{DNAArray.class, Float.class};
	 *	int[] dimensions = new int[]{4,4};
	 *	DNAArray&lt;DNAArray&lt;Float&gt;&gt; mv_mat4x4 = BlenderFactory.newDNAArrayBlock(BlockHeader.CODE_DATA, typeList, dimensions, blend);
	 *	
	 *	// 1-dim array of pointers on bytes (e.g. strings)
	 *	typeList = new Class[]{DNAArray.class, DNAPointer.class, Byte.class};
	 *	dimensions = new int[]{4};
	 *	DNAArray&lt;DNAPointer&lt;Byte&gt;&gt; fileList = BlenderFactory.newDNAArrayBlock(BlockHeader.CODE_DATA, typeList, dimensions, blend);
	 * </pre>
	 * @param blockCode Code of the new block. Frequently CODE_DATA for arrays.
	 * @param typeList type specification for all referenced types
	 * @param dimensions length of each array dimension
	 * @param blend blender file to add block to.
	 * @return
	 * @throws IOException
	 */
	public static <T> DNAArray<T> newDNAArrayBlock(Identifier blockCode, Class<?>[] typeList, int[] dimensions, BlenderFile blend) throws IOException {
		BlockTable blockTable = blend.getBlockTable();
		// TODO: ZZZ ist der sdnaIndex bei DATA blocks eventuell der typeIndex?
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
	 * @param blend blender file to add block to.
	 * @return a pointer on the pointer stored in the block.
	 * @throws IOException
	 */
	public static <T> DNAPointer<DNAPointer<T>> newDNAPointerBlock(Identifier blockCode, Class<?>[] typeList, BlenderFile blend) throws IOException {
		BlockTable blockTable = blend.getBlockTable();
		// TODO: ZZZ ist der sdnaIndex bei DATA blocks eventuell der typeIndex?
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
	 * This method creates a block with a set of pointers and returns an array facet to 
	 * access them. To change the value of the pointers use for example
	 * the method {@link DNAArray#set(int, Object)} of the pointer on the pointer.
	 * 
	 * @param blockCode Usually CODE_DATA for lists of pointers.
	 * @param typeList type specification of the pointer.
	 * @param count number of pointers to fit in block.
	 * @param blend blender file to add block to.
	 * @return
	 * @throws IOException
	 */
	public static <T> DNAArray<DNAPointer<T>> newDNAPointerBlock(Identifier blockCode, Class<?>[] typeList, int count, BlenderFile blend) throws IOException {
		BlockTable blockTable = blend.getBlockTable();
		// TODO: ZZZ ist der sdnaIndex bei DATA blocks eventuell der typeIndex?
		int sdnaIndex = 0; // not a struct
		int size = blend.getEncoding().getAddressWidth();
		Block block = blockTable.allocate(blockCode, size);
		block.header.setCount(count);
		block.header.setSdnaIndex(sdnaIndex);
		Class<?>[] typeListExtended = new Class<?>[typeList.length+1];
		System.arraycopy(typeList, 0, typeListExtended, 1, typeList.length);
		typeListExtended[0] = DNAPointer.class;
		return new DNAArray<DNAPointer<T>>(block.header.getAddress(), typeList, new int[]{count}, blockTable);
	}
	
	/**
	 * This method provides the StructDNA from a given sdna.blend 
	 * image resource path. Every generated Java Blend data model
	 * has such an sdnaImage resource (to be found in "your/package/name/ultils/resources/sdna.blend") 
	 * which contains just the sdna meta data of the blender version the
	 * model was generated from.
	 * 
	 * @param resourcePathTo_sdna_blend "your/package/name/ultils/resources/sdna.blend"
	 * @return 
	 * @throws IOException
	 */
	protected static StructDNA createStructDNA(String resourcePathTo_sdna_blend) throws IOException {
		InputStream in = BlenderFactoryBase.class.getClassLoader().getResourceAsStream(resourcePathTo_sdna_blend);
		BlenderFileImplBase sdnaImage = new BlenderFileImplBase(new BigEndianInputStreamWrapper(in, 8));
		StructDNA sdna = sdnaImage.getStructDNA();
		sdnaImage.close();
		return sdna;
	}
}
