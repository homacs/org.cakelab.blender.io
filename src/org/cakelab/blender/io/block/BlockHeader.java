package org.cakelab.blender.io.block;

import java.io.IOException;

import org.cakelab.blender.io.dna.internal.StructDNA;
import org.cakelab.blender.io.util.CDataReadWriteAccess;
import org.cakelab.blender.io.util.Identifier;

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
 * Each block has a block code (see {@link BlockCodes}).
 * The type of data stored in the block is determined by the
 * {@link BlockHeader#sdnaIndex}. This is the index of the type information 
 * stored in {@link StructDNA#structs}. Thus, you always have to
 * decode the {@link StructDNA} first before you can read other data. 
 * </p>
 * 
 * 
 */
public class BlockHeader {

	
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
	

	public BlockHeader() {
	}

	public BlockHeader(Identifier code, int size, long address) {
		this.code = code;
		this.size = size;
		this.address = address;
	}

	public BlockHeader(Identifier code, int size, long address, int sdnaIndex,
			int count) {
		super();
		this.code = code;
		this.size = size;
		this.address = address;
		this.sdnaIndex = sdnaIndex;
		this.count = count;
	}

	public void read(CDataReadWriteAccess in) throws IOException {
		code.read(in);
		size = in.readInt();
		address = in.readLong();
		sdnaIndex = in.readInt();
		count = in.readInt();
	}

	public void write(CDataReadWriteAccess out) throws IOException {
		code.write(out);
		out.writeInt(size);
		out.writeLong(address);
		out.writeInt(sdnaIndex);
		out.writeInt(count);
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

	public void setSdnaIndex(int sdnaIndex) {
		this.sdnaIndex = sdnaIndex;
	}

	public int getCount() {
		return count;
	}

	public void setCount(int count) {
		this.count = count;
	}

	public String toString() {
		return code + ": size=" + size + ", address=" + address + ", sdnaIndex=" + sdnaIndex + ", count=" + count;
	}


	public static long getHeaderSize(int pointerSize) {
		return 16 + pointerSize;
	}

}
