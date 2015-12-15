package org.cakelab.blender.io;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;

import org.cakelab.blender.io.FileHeader.Version;
import org.cakelab.blender.io.block.Block;
import org.cakelab.blender.io.block.BlockHeader;
import org.cakelab.blender.io.block.BlockTable;
import org.cakelab.blender.io.dna.DNAModel;
import org.cakelab.blender.io.dna.internal.StructDNA;
import org.cakelab.blender.io.util.CDataReadWriteAccess;
import org.cakelab.blender.io.util.Identifier;
import org.cakelab.blender.metac.CMetaModel;


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
	
	// TODO: ZZZ improve data management in BlenderFile


	private FileHeader header;
	
	
	protected CDataReadWriteAccess io;
	private long firstBlockOffset;


	private ArrayList<Block> blocks;

	private StructDNA sdna;
	private DNAModel model;


	private BlockTable blockTable;

	
	public BlenderFile(File file) throws IOException {
		this(CDataReadWriteAccess.create(new RandomAccessFile(file, "r"), Encoding.JAVA_NATIVE));
		// proceed from here with an input stream which decodes data according to its endianess
		io = CDataReadWriteAccess.create(new RandomAccessFile(file, "r"), getEncoding());
		readStructDNA();
		readBlocks();
	}
	
	/**
	 * Just basic read initialisation. Reading file header.
	 * (byte order doesn't matter in this case).
	 * 
	 * @param in
	 * @throws IOException
	 */
	protected BlenderFile(CDataReadWriteAccess in) throws IOException {
		header = new FileHeader();
		try {
			try {
				header.read(in);
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
	
	protected BlenderFile(File file, StructDNA sdna, int blenderVersion) throws IOException {
		this.sdna = sdna;
		
		io = CDataReadWriteAccess.create(new RandomAccessFile(file, "rw"), Encoding.JAVA_NATIVE);
		header = new FileHeader();
		header.endianess = FileHeader.Endianess.from(io.getByteOrder());
		header.pointerSize = FileHeader.PointerSize.from(io.getPointerSize());
		header.version = new Version(blenderVersion);
		
		//
		// determine firstBlockOffset
		//
		header.write(io);
		firstBlockOffset = io.offset();
		
		blocks = new ArrayList<Block>();
		blockTable = getBlockTable();
	}
	
	public void write() throws IOException {
		io.offset(firstBlockOffset);
		
		boolean sdnaWritten = false;
		Block endBlock = null;
		
		io.offset(firstBlockOffset);
		// flush all blocks to disk
		for (Block block : blocks) {
			if (block.header.getCode().equals(BlockHeader.CODE_ENDB)) {
				endBlock = block;
				continue;
			}
			block.flush(io);
			
			if (block.header.getCode().equals(BlockHeader.CODE_DNA1)) {
				sdnaWritten = true;
			}
		}
		
		if (!sdnaWritten) {
			// sdna never existed in a block. Thus, we create one now on disk.
			writeSdnaBlock();
		}
		
		if (endBlock != null) {
			endBlock.flush(io);
		} else {
			writeEndBlock();
		}
		
	}
	
	private void writeEndBlock() throws IOException {
		BlockHeader endb = new BlockHeader(BlockHeader.CODE_ENDB, 0, 0, 0, 0);
		endb.write(io);
	}

	private void writeSdnaBlock() throws IOException {
		// TODO: ZZZ calculate size of snda block beforehand
		long headerOffset = io.offset();
		
		//
		// write a dummy header (lazy way to determine its size)
		//
		BlockHeader header = new BlockHeader();
		header.write(io);

		//
		// write sdna to disk
		//
		long dataOffset = io.offset();
		sdna.write(io);
		
		long end = io.offset();
		
		//
		// create the actual header and write it to 'headerOffset'
		//
		
		int size = (int) (end - dataOffset);
		/// receive an address for the block from allocator
		long address = blockTable.alloc(size);
		int sdnaIndex = 0;
		int count = 1;
		header = new BlockHeader(BlockHeader.CODE_DNA1, size, address, sdnaIndex, count);
		
		io.offset(headerOffset);
		header.write(io);
		
		io.offset(end);
	}

	public DNAModel getBlenderModel() throws IOException {
		if (model == null) {
			model = new DNAModel(sdna);
		}
		return model;
	}
	
	public FileVersionInfo readFileGlobal() throws IOException {
		FileVersionInfo versionInfo = null;
		BlockHeader blockHeader = seekFirstBlock(BlockHeader.CODE_GLOB);

		if (blockHeader != null) {
			versionInfo = new FileVersionInfo();
			versionInfo.read(io);
		} else {
			throw new IOException("Can't find block GLOB (file global version info)");
		}
		versionInfo.version = header.version;
		return versionInfo;
	}
	
	
	private void readStructDNA() throws IOException {
		sdna = null;
		BlockHeader blockHeader = seekFirstBlock(BlockHeader.CODE_DNA1);

		if (blockHeader != null) {
			sdna = new StructDNA();
			sdna.read(io);
		} else {
			throw new IOException("corrupted file. Can't find block DNA1");
		}
	}
	
	public BlockHeader seekFirstBlock(Identifier code) throws IOException {
		BlockHeader result = null;

		io.offset(firstBlockOffset);
		BlockHeader blockHeader = new BlockHeader();
		blockHeader.read(io);
		while (!blockHeader.getCode().equals(BlockHeader.CODE_ENDB)) {
			if (blockHeader.getCode().equals(code)) {
				result = blockHeader;
				break;
			}
			io.skip(blockHeader.getSize());
			blockHeader.read(io);
		}
		return result;
	}


	
	
	
	public ArrayList<Block> getBlocks() throws IOException {
		if (blocks == null) {
			readBlocks();
		}
		return blocks;
	}

	public BlockTable getBlockTable () throws IOException {
		if (blockTable == null) {
			blockTable = new BlockTable(getEncoding(), getBlocks());
		}
		return blockTable;
	}
	
	
	private void readBlocks() throws IOException {
		blocks = new ArrayList<Block>();
		io.offset(firstBlockOffset);
		BlockHeader blockHeader = new BlockHeader();
		blockHeader.read(io);
		while (!blockHeader.getCode().equals(BlockHeader.CODE_ENDB)) {
			CDataReadWriteAccess data = readBlockData(blockHeader);
			
			Block block = new Block(blockHeader, data);
			blocks.add(block);
			
			blockHeader = new BlockHeader();
			blockHeader.read(io);
		}
	}

	private CDataReadWriteAccess readBlockData(BlockHeader blockHeader) throws IOException {
		byte[] data = new byte[blockHeader.getSize()];
		io.readFully(data);
		return CDataReadWriteAccess.create(data, blockHeader.getAddress(), getEncoding());
	}


	@Override
	public void close() throws IOException {
		io.close();
		io = null;
	}

	/**
	 * @return Encoding according to the files header
	 */
	public Encoding getEncoding() {
		return Encoding.get(header.getByteOrder(), header.getPointerSize());
	}


	public CMetaModel getMetaModel() throws IOException {
		return new CMetaModel(getBlenderModel());
	}


	public Version getVersion() {
		return header.version;
	}

	public StructDNA getStructDNA() {
		return sdna;
	}


}
