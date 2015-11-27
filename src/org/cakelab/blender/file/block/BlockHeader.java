package org.cakelab.blender.file.block;

import java.io.IOException;

import org.cakelab.blender.file.dna.internal.StructDNA;
import org.cakelab.blender.file.util.CDataReadAccess;
import org.cakelab.blender.file.util.Identifier;

/**
 * <p>
 * File-blocks contain a file-block-header and data. 
 * The start of a file-block is always implicitly aligned 
 * at 4 bytes. 
 * The file-block-header describes the total length of the 
 * data, the type of information stored in the file-block, 
 * the number of items of this information and the old 
 * memory pointer at the moment the data was written 
 * to disk. Depending on the pointer-size stored in the 
 * file-header, a file-block-header can be 20 or 24 
 * bytes long.
 * </p>
 * <p>
 * Each block has a block code (see {@link BlockHeader#code}).
 * Block codes associate a block with a certain group of data
 * such as Library, Object, Scene etc. but most of the blocks
 * nowadays have a generic code called 'DATA'. The most 
 * important codes are 'ENDB' and 'DNA1' which are both unique
 * in a Blender file.
 * </p>
 * <b>List of known block codes:</b>
 * <table border="1">
 * <tr><td>code</td><td>description</td></tr>
 * <tr><td>ENDB</td><td>Marks the end of the Blender file.</td></tr>
 * <tr><td>DNA1</td><td>Contains the {@link StructDNA}</td></tr>
 * <tr><td>DATA</td><td>Arbitrary data.</td></tr>
 * </table>
 * <p>
 * The type of data stored in the block is determined by the
 * {@link BlockHeader#sdnaIndex}. This is the index of the type information 
 * stored in {@link StructDNA#structs}. Thus, you always have to
 * decode the {@link StructDNA} first before you can read other data. 
 * </p>
 * 
 * 
 */
public class BlockHeader {

	/* all known block codes as of v2.69 */
	
	/** Scene */
	public static final Identifier CODE_SCE = new Identifier(new byte[]{'S', 'C', 0, 0});
	/** Library */
	public static final Identifier CODE_LI = new Identifier(new byte[]{'L', 'I', 0, 0});
	/** Object */
	public static final Identifier CODE_OB = new Identifier(new byte[]{'O', 'B', 0, 0});
	/** Mesh */
	public static final Identifier CODE_ME = new Identifier(new byte[]{'M', 'E', 0, 0});
	/** Curve */
	public static final Identifier CODE_CU = new Identifier(new byte[]{'C', 'U', 0, 0});
	/** MetaBall */
	public static final Identifier CODE_MB = new Identifier(new byte[]{'M', 'B', 0, 0});
	/** Material */
	public static final Identifier CODE_MA = new Identifier(new byte[]{'M', 'A', 0, 0});
	/** Texture */
	public static final Identifier CODE_TE = new Identifier(new byte[]{'T', 'E', 0, 0});
	/** Image */
	public static final Identifier CODE_IM = new Identifier(new byte[]{'I', 'M', 0, 0});
	/** Lattice */
	public static final Identifier CODE_LT = new Identifier(new byte[]{'L', 'T', 0, 0});
	/** Lamp */
	public static final Identifier CODE_LA = new Identifier(new byte[]{'L', 'A', 0, 0});
	/** Camera */
	public static final Identifier CODE_CA = new Identifier(new byte[]{'C', 'A', 0, 0});
	/** Ipo (depreciated, replaced by FCurves) */
	public static final Identifier CODE_IP = new Identifier(new byte[]{'I', 'P', 0, 0});
	/** Key (shape key) */
	public static final Identifier CODE_KE = new Identifier(new byte[]{'K', 'E', 0, 0});
	/** World */
	public static final Identifier CODE_WO = new Identifier(new byte[]{'W', 'O', 0, 0});
	/** Screen */
	public static final Identifier CODE_SCR = new Identifier(new byte[]{'S', 'R', 0, 0});
	/** (depreciated?) */
	public static final Identifier CODE_SCRN = new Identifier(new byte[]{'S', 'N', 0, 0});
	/** VectorFont */
	public static final Identifier CODE_VF = new Identifier(new byte[]{'V', 'F', 0, 0});
	 /** Text */
	public static final Identifier CODE_TXT = new Identifier(new byte[]{'T', 'X', 0, 0});
	/** Speaker */
	public static final Identifier CODE_SPK = new Identifier(new byte[]{'S', 'K', 0, 0});
	/** Sound */
	public static final Identifier CODE_SO = new Identifier(new byte[]{'S', 'O', 0, 0});
	/** Group */
	public static final Identifier CODE_GR = new Identifier(new byte[]{'G', 'R', 0, 0});
	/** (internal use only) */
	public static final Identifier CODE_ID = new Identifier(new byte[]{'I', 'D', 0, 0});
	/** Armature */
	public static final Identifier CODE_AR = new Identifier(new byte[]{'A', 'R', 0, 0});
	/** Action */
	public static final Identifier CODE_AC = new Identifier(new byte[]{'A', 'C', 0, 0});
	/** Script (depreciated) */
	public static final Identifier CODE_SCRIPT = new Identifier(new byte[]{'P', 'Y', 0, 0});
	/** NodeTree */
	public static final Identifier CODE_NT = new Identifier(new byte[]{'N', 'T', 0, 0});
	/** Brush */
	public static final Identifier CODE_BR = new Identifier(new byte[]{'B', 'R', 0, 0});
	/** ParticleSettings */
	public static final Identifier CODE_PA = new Identifier(new byte[]{'P', 'A', 0, 0});
	/** GreasePencil */
	public static final Identifier CODE_GD = new Identifier(new byte[]{'G', 'D', 0, 0});
	/** WindowManager */
	public static final Identifier CODE_WM = new Identifier(new byte[]{'W', 'M', 0, 0});
	/** MovieClip */
	public static final Identifier CODE_MC = new Identifier(new byte[]{'M', 'C', 0, 0});
	/** Mask */
	public static final Identifier CODE_MSK = new Identifier(new byte[]{'M', 'S', 0, 0});
	/** FreestyleLineStyle */
	public static final Identifier CODE_LS = new Identifier(new byte[]{'L', 'S', 0, 0}); 
	/** NOTE! Fake IDs, needed for g.sipo->blocktype or outliner */
	public static final Identifier CODE_SEQ = new Identifier(new byte[]{'S', 'Q', 0, 0});
	/** constraint.
	 * <br/>NOTE! Fake IDs, needed for g.sipo->blocktype or outliner. */
	public static final Identifier CODE_CO = new Identifier(new byte[]{'C', 'O', 0, 0});
	/** pose (action channel, used to be ID_AC in code, so we keep code for backwards compat)
	 * <br/>NOTE! Fake IDs, needed for g.sipo->blocktype or outliner. */
	public static final Identifier CODE_PO = new Identifier(new byte[]{'A', 'C', 0, 0});
	/** used in outliner... 
	 * <br/>NOTE! Fake IDs, needed for g.sipo->blocktype or outliner.*/
	public static final Identifier CODE_NLA = new Identifier(new byte[]{'N', 'L', 0, 0});
	/** fluidsim Ipo 
	 * <br/>NOTE! Fake IDs, needed for g.sipo->blocktype or outliner.*/
	public static final Identifier CODE_FLUIDSIM = new Identifier(new byte[]{'F', 'S', 0, 0});
	
	/** block code of the last block. */
	public static final Identifier CODE_ENDB = new Identifier("ENDB");
	/** block code of the block containing the {@link StructDNA} struct. */
	public static final Identifier CODE_DNA1 = new Identifier("DNA1");
	/** Block code of a block containing struct {@link Link}. */
	public static final Identifier CODE_REND = new Identifier("REND");
	/** Block code of a block containing struct {@link Link}. */
	public static final Identifier CODE_TEST = new Identifier("TEST");
	/** Block code of a block containing struct {@link FileGlobal}. */
	public static final Identifier CODE_GLOB = new Identifier("GLOB");
	/** Block code of a block containing data related to other blocks. */
	public static final Identifier CODE_DATA = new Identifier("DATA");

	
	/* ************************************************ */
	/*          BEGIN of BLOCK HEADER DATA              */
	/* ************************************************ */
	
	/** 4 byte ASCII string {@link Identifier} of the file-block.
	 * Code describes different types of file-blocks. These codes 
	 * allow fast lookup of data like Library, Scenes, Object or 
	 * Materials as they have a specific code. 
	 * The last file-block in the file has code 'ENDB'.*/
	Identifier code = new Identifier();
	/** Total (int32) length of the data after the file-block-header.
	 * The size contains the total length of data after the 
	 * file-block-header. After the data a new file-block 
	 * starts. */
	int size;
	/** Memory address the structure was located when written to disk.
	 * The old memory address contains the memory address when 
	 * the structure was last stored. This information is used to
	 * resolve pointers on data in this block. */
	long address;
	/** Index of the SDNA structure into {@link StructDNA#structs}.
	 * SDNA index contains the index in the DNA structures to 
	 * be used when reading this file-block-data. */
	int sdnaIndex;
	/** Number of structures located in this file-block.
	 * Count tells how many elements of the specific SDNA structure
	 * can be found in the data.  */
	int count;

	
	/* ************************************************ */
	/*           END of BLOCK HEADER DATA               */
	/* ************************************************ */
	

	/** This is the start position of the block header in the blend file. 
	 * This variable is not part of the blender blockheader, but is 
	 * required to allow random access to structures on disk. */
	long filePosition;
	
	public void read(CDataReadAccess in) throws IOException {
		filePosition = in.offset();
		code.read(in);
		size = in.readInt();
		address = in.readLong();
		sdnaIndex = in.readInt();
		count = in.readInt();
	}

	
	public Identifier getCode() {
		return code;
	}


	public int getSize() {
		return size;
	}

	public long getAddress() {
		return address;
	}


	public int getSdnaIndex() {
		return sdnaIndex;
	}


	public int getCount() {
		return count;
	}


	public long getFilePosition() {
		return filePosition;
	}


	public String toString() {
		return code + ": size=" + size + ", address=" + address + ", sdnaIndex=" + sdnaIndex + ", count=" + count;
	}


	public static long getHeaderSize(int pointerSize) {
		return 16 + pointerSize;
	}
}
