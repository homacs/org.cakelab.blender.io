package org.cakelab.blender.file;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;

import org.cakelab.blender.file.block.Block;
import org.cakelab.blender.file.block.BlockHeader;
import org.cakelab.blender.file.dna.BlendModel;
import org.cakelab.blender.file.dna.internal.StructDNA;
import org.cakelab.blender.file.util.CDataReadAccess;


/**
 * A blend-file always start with the file-header followed by file-blocks. 
 * The default blend file of Blender 2.48 contains more than 400 of these 
 * file-blocks. Each file-block has a file-block-header and data. 
 * 
 * <p>
 * Saving complex scenes in Blender is done within seconds. Blender achieves 
 * this by saving data in memory to disk without any transformations or 
 * translations. Blender only adds file-block-headers to this data. 
 * A file-block-header contains clues on how to interpret the data. 
 * After the data, all internally Blender structures are stored. These 
 * structures will act as blue-prints when Blender loads the file. 
 * Blend-files can be different when stored on different hardware platforms 
 * or Blender releases. There is no effort taken to make blend-files 
 * binary the same. Blender creates the blend-files in this manner 
 * since release 1.0. Backward and upwards compatibility is not implemented 
 * when saving the file, this is done during loading.
 * </p><p>
 * When Blender loads a blend-file, the DNA-structures are read first. 
 * Blender creates a catalog of these DNA-structures. Blender uses this 
 * catalog together with the data in the file, the internal Blender 
 * structures of the Blender release you're using and a lot of 
 * transformation and translation logic to implement the backward 
 * and upward compatibility. In the source code of blender there is 
 * actually logic which can transform and translate every structure used 
 * by a Blender release to the one of the release you're using (see 
 * <a href="http://download.blender.org/source">blender sources</a>
 * /source/blender/blenloader/intern/readfile.c for reference).
 * The more difference between releases the more logic is executed.
 * </p>
 */
public class BlenderFile implements Closeable {

	private FileHeader header;
	
	
	private CDataReadAccess cin;
	private long firstBlockOffset;


	public BlenderFile(File file) throws IOException {
		CDataReadAccess in = CDataReadAccess.create(new RandomAccessFile(file, "r"), ByteOrder.BIG_ENDIAN, 0);
		header = new FileHeader();
		try {
			try {
				header.read(in);
				// proceed from here with an input stream which decodes data according to its endianess
				cin = CDataReadAccess.create(new RandomAccessFile(file, "r"), header.getByteOrder(), header.pointerSize.getSize());
				firstBlockOffset = in.offset();
				in.close();
			} catch (IOException e) {
				// it might be a compressed file
				throw new IOException("file is either corrupted or uses the compressed format (not yet supported).\n"
						+ "In the latter case, please uncompress it first (i.e. gunzip <file>.");
			}
			
		
		} finally {
			try {in.close();} catch (Throwable suppress){}
		}
	}
	
	
	public BlendModel readBlenderModel() throws IOException {
		StructDNA sdna = null;
		
		cin.offset(firstBlockOffset);
		BlockHeader blockHeader = new BlockHeader();
		blockHeader.read(cin);
		
		while (!blockHeader.getCode().equals(BlockHeader.CODE_ENDB)) {
			if (blockHeader.getCode().equals(BlockHeader.CODE_DNA1)) {
				sdna = new StructDNA();
				sdna.read(cin);
				break;
			}
			cin.skip(blockHeader.getSize());
			blockHeader.read(cin);
		}
		if (sdna == null) throw new IOException("corrupted file. Can't find block DNA1");
		
		return new BlendModel(sdna);
	}
	
	public ArrayList<Block> readBlocks() throws IOException {
		ArrayList<Block> blocks = new ArrayList<Block>();
		cin.offset(firstBlockOffset);
		BlockHeader blockHeader = new BlockHeader();
		blockHeader.read(cin);
		while (!blockHeader.getCode().equals(BlockHeader.CODE_ENDB)) {
			CDataReadAccess data = readBlockData(blockHeader);
			
			Block block = new Block(blockHeader, data);
			blocks.add(block);
			
			blockHeader = new BlockHeader();
			blockHeader.read(cin);
		}
		return blocks;
	}


	private CDataReadAccess readBlockData(BlockHeader blockHeader) throws IOException {
		byte[] data = new byte[blockHeader.getSize()];
		cin.readFully(data);
		ByteBuffer buffer = ByteBuffer.wrap(data);
		buffer.order(header.getByteOrder());
		return CDataReadAccess.create(buffer, blockHeader.getAddress(), header.getPointerSize());
	}


	@Override
	public void close() throws IOException {
		cin.close();
	}


	public int getPointerSize() {
		return header.getPointerSize();
	}


}
