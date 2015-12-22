package org.cakelab.blender.io;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.List;

import org.cakelab.blender.io.FileHeader.Version;
import org.cakelab.blender.io.block.Block;
import org.cakelab.blender.io.block.BlockHeader;
import org.cakelab.blender.io.block.BlockList;
import org.cakelab.blender.io.block.BlockTable;
import org.cakelab.blender.io.dna.DNAModel;
import org.cakelab.blender.io.dna.internal.StructDNA;
import org.cakelab.blender.io.util.CDataReadWriteAccess;
import org.cakelab.blender.io.util.Identifier;
import org.cakelab.blender.metac.CMetaModel;


/**
 * Class BlenderFile implements various functionalities to support in
 * reading or writing blender files. 
 * <h2>Reading</h2>
 * <p>
 * To simply read a blender file you can use the constructor 
 * {@link #BlenderFile(File)}. If the data model was generated with 
 * the optional utilities (i.e. with the 'utils' package) then you will 
 * find a class MainLib in this package. The MainLib provides CFacades to 
 * all data available in the file. To instantiate the MainLib you need
 * block table received via {@link #getBlockTable()}. The block table is also
 * a way to access blocks directly. The block table maps virtual addresses 
 * to memory in blocks. Internally, it maintains a sorted list of the blocks.
 * If you prefer or need direct access to blocks in the order given in the 
 * blender file then you can use the list returned from {@link #getBlocks()} 
 * instead. 
 * </p>
 * <h2>Writing</h2>
 * <p>Unfortunately, writing a blender file can take a bit more effort, especially 
 * if you are adding new blocks. First of all, it is important to know, that the
 * list of blocks you may have received after reading, is not in sync with the block
 * table. Adding and removing blocks in the block table is not reflected in the list.
 * </p>
 * <p>The order of blocks usually doesn't matter except of the ENDB block and some special
 * cases I saw in blender code. The methods {@link #write()} and {@link #write(List)}
 * take care of ENDB and DNA1, i.e. they add a StructDNA block and an ENDB block at the end. 
 * Since those remaining special cases in blender code, where the order of blocks matters, 
 * are version specific, Java Blend cannot provide a generic solution and instead 
 * allows the API developer to provide his own block list to method {@link #write(List)}.
 * </p>
 * <p>To help maintain a certain order of blocks, Java Blend provides the 
 * class {@link BlockList} which is a linked list of blocks, and therefore optimised 
 * for fast adding and removing of blocks. But this list is <b>not</b> automatically updated
 * if blocks are added to or removed from the block table.
 * </p>
 */
public class BlenderFile implements Closeable {
	
	// TODO: ZZZ improve data management in BlenderFile


	protected FileHeader header;
	
	
	protected CDataReadWriteAccess io;
	protected long firstBlockOffset;

	private StructDNA sdna;
	private DNAModel model;


	private BlockTable blockTable;


	private BlockList blocks;

	
	public BlenderFile(File file) throws IOException {
		this(CDataReadWriteAccess.create(new RandomAccessFile(file, "r"), Encoding.JAVA_NATIVE));
		// proceed from here with an input stream which decodes data according to its endianess
		io = CDataReadWriteAccess.create(new RandomAccessFile(file, "rw"), getEncoding());
		readStructDNA();
		blockTable = new BlockTable(getEncoding(), readBlocks());
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
		
		// Unfortunately, blender has a bug in byte order conversion, so we use the
		// systems native byte order.
		Encoding encoding = Encoding.nativeEncoding();
		
		io = CDataReadWriteAccess.create(new RandomAccessFile(file, "rw"), encoding);

		header = new FileHeader();
		header.endianess = FileHeader.Endianess.from(io.getByteOrder());
		header.pointerSize = FileHeader.PointerSize.from(io.getPointerSize());
		header.version = new Version(blenderVersion);
		header.write(io);
		
		firstBlockOffset = io.offset();
		
		blocks = new BlockList();
		blockTable = new BlockTable(getEncoding(), blocks);
	}
	
	
	protected BlenderFile() {}

	public void write() throws IOException {
		write(blocks);
	}
	
	
	public void write(List<Block> blocks) throws IOException {
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
	
	protected void writeEndBlock() throws IOException {
		BlockHeader endb = new BlockHeader(BlockHeader.CODE_ENDB, 0, 0, 0, 0);
		endb.write(io);
	}

	protected void writeSdnaBlock() throws IOException {
		// TODO: ZZZ calculate size of snda block beforehand
		long headerOffset = io.offset();
		
		//
		// write a dummy header (lazy way to determine its size)
		//
		BlockHeader header = new BlockHeader();
		header.write(io);
		long dataOffset = io.offset();

		//
		// write sdna to disk
		//
		sdna.write(io);
		long end = io.offset();
		
		//
		// create the actual header and write it to 'headerOffset'
		//
		
		int size = (int) (end - dataOffset);
		/// receive an address for the block from allocator
		long address = blockTable.getAllocator().alloc(size);
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
	
	
	protected void readStructDNA() throws IOException {
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


	public BlockTable getBlockTable() throws IOException {
		return blockTable;
	}
	
	
	private BlockList readBlocks() throws IOException {
		blocks = new BlockList();
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
		return blocks;
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

	public BlockList getBlocks() {
		return blocks;
	}

	/**
	 * Adds a block to the end of the file. Please note, that method {@link #write()}
	 * will rearrange blocks eventually to move ENDB at the end.
	 * 
	 * @param block
	 */
	public void add(Block block) {
		blocks.add(block);
	}
}
