package org.cakelab.blender.io;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Arrays;
import java.util.List;

import org.cakelab.blender.io.FileHeader.Version;
import org.cakelab.blender.io.block.Block;
import org.cakelab.blender.io.block.BlockCodes;
import org.cakelab.blender.io.block.BlockHeader;
import org.cakelab.blender.io.block.BlockList;
import org.cakelab.blender.io.block.BlockTable;
import org.cakelab.blender.io.block.OverlappingBlocksException;
import org.cakelab.blender.io.dna.DNAModel;
import org.cakelab.blender.io.dna.DNAStruct;
import org.cakelab.blender.io.dna.internal.StructDNA;
import org.cakelab.blender.io.util.CDataReadWriteAccess;
import org.cakelab.blender.io.util.Identifier;
import org.cakelab.blender.metac.CMetaModel;
import org.cakelab.blender.metac.CStruct;
import org.cakelab.blender.versions.OffheapAreas;



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
	
	protected FileHeader header;
	
	
	protected CDataReadWriteAccess io;
	protected long firstBlockOffset;

	private StructDNA sdna;
	private DNAModel model;


	private BlockTable blockTable;


	private BlockList blocks;


	private File file;


	public BlenderFile(File file) throws IOException {
		readFileHeader(CDataReadWriteAccess.create(new RandomAccessFile(file, "r"), Encoding.JAVA_NATIVE));
		this.file = file;
		// proceed from here with an input stream which decodes data according to its endianess
		io = CDataReadWriteAccess.create(new RandomAccessFile(file, "rw"), getEncoding());
		readStructDNA();
		String[] offheapAreas = OffheapAreas.get(header.version.getCode());
		initBlockTable(getEncoding(), readBlocks(), getSdnaIndices(offheapAreas));
	}

	protected BlenderFile(File file, StructDNA sdna, int blenderVersion, String[] offheapAreas) throws IOException {
		// Unfortunately, blender has a bug in byte order conversion, so we use the
		// systems native byte order as default.
		this(file, sdna, blenderVersion, Encoding.nativeEncoding(), offheapAreas);
	}
	
	protected BlenderFile(File file, StructDNA sdna, int blenderVersion, Encoding encoding, String[] offheapAreas) throws IOException {
		this.sdna = sdna;
		
		io = CDataReadWriteAccess.create(new RandomAccessFile(file, "rw"), encoding);

		header = new FileHeader();
		header.endianess = FileHeader.Endianess.from(io.getByteOrder());
		header.pointerSize = FileHeader.PointerSize.from(io.getPointerSize());
		header.version = new Version(blenderVersion);
		header.write(io);
		
		firstBlockOffset = io.offset();
		
		blocks = new BlockList();
		
		
		initBlockTable(getEncoding(), blocks, getSdnaIndices(offheapAreas));
		
	}
	
	
	private void initBlockTable(Encoding encoding, BlockList blocks, int[] sdnaIndices) throws IOException {
		try {
			
			blockTable = new BlockTable(encoding, blocks, sdnaIndices);
		} catch (OverlappingBlocksException e) {
			e.addDetailedInfo(model);
			throw new IOException(e);
		}
	}

	protected BlenderFile() {}

	
	
	/**
	 * Just basic read initialisation. Reading file header.
	 * (byte order doesn't matter in this case).
	 * 
	 * @param in
	 * @throws IOException
	 */
	protected void readFileHeader(CDataReadWriteAccess in) throws IOException {
		header = new FileHeader();
		try {
			header.read(in);
			firstBlockOffset = in.offset();
			in.close();
			
		} catch (IOException e) {
			// it might be a compressed file
			throw new IOException("file is either corrupted or uses the compressed format (not yet supported).\n"
					+ "In the latter case, please uncompress it first (i.e. gunzip <file>.");
		} finally {
			try {in.close();} catch (Throwable suppress){}
		}
	}
	
	/** Retrieve indices for given names from Struct DNA. */
	protected int[] getSdnaIndices(String[] structNames) throws IOException {
		if (structNames == null) return null;
		model = getBlenderModel();
		
		int[] indexes = new int[structNames.length];
		int length = 0;
		for (String structName : structNames) {
			DNAStruct struct = model.getStruct(structName);
			if (struct == null) {
				System.err.println("warning: The list of offheap areas (see Java .Blend documentation) contains a struct name '" + structName + "' which does not exist in the blender version of the given file. This entry will be ignored.");
			} else {
				indexes[length++] = struct.getIndex();
			}
		}
		return Arrays.copyOf(indexes, length);
	}

	/**
	 * Write all blocks to the file. This method calls {@link #write(List)} whith
	 * all blocks stored in this instance of BlenderFile.
	 * @throws IOException
	 */
	public void write() throws IOException {
		write(blocks);
	}
	
	/** Write given blocks to the file. This reorders only the Struct DNA (DNA1)
	 * block and the End (ENDB) block. All other blocks have to be in the order 
	 * expected by blender. */
	public void write(List<Block> blocks) throws IOException {
		io.offset(firstBlockOffset);
		
		boolean sdnaWritten = false;
		Block endBlock = null;
		
		io.offset(firstBlockOffset);
		// flush all blocks to disk
		for (Block block : blocks) {
			System.out.println("writing " + block.header.getCode().toString());
			if (block.header.getCode().equals(BlockCodes.ID_ENDB)) {
				endBlock = block;
				continue;
			}
			block.flush(io);
			
			if (block.header.getCode().equals(BlockCodes.ID_DNA1)) {
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
		BlockHeader endb = new BlockHeader(BlockCodes.ID_ENDB, 0, 0, 0, 0);
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
		header = new BlockHeader(BlockCodes.ID_DNA1, size, address, sdnaIndex, count);
		
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
		CMetaModel meta = getMetaModel();
		
		FileVersionInfo versionInfo = null;
		BlockHeader blockHeader = seekFirstBlock(BlockCodes.ID_GLOB);

		if (blockHeader != null) {
			CStruct struct = (CStruct) meta.getType("FileGlobal");
			versionInfo = new FileVersionInfo();
			versionInfo.read(struct, io);
		} else {
			throw new IOException("Can't find block GLOB (file global version info)");
		}
		versionInfo.version = header.version;
		return versionInfo;
	}
	
	
	protected void readStructDNA() throws IOException {
		sdna = null;
		BlockHeader blockHeader = seekFirstBlock(BlockCodes.ID_DNA1);

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
		while (!blockHeader.getCode().equals(BlockCodes.ID_ENDB)) {
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
		BlockHeader blockHeader;
		Block block;
		// We read all blocks until we hit ENDB.
		// There is always at least the DNA block in a .blend file.
		do {
			blockHeader = new BlockHeader();
			blockHeader.read(io);
			CDataReadWriteAccess data = readBlockData(blockHeader);
			
			block = new Block(blockHeader, data);
			blocks.add(block);
			
		} while (!blockHeader.getCode().equals(BlockCodes.ID_ENDB));
		
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

	public FileHeader getHeader() {
		return header;
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

	public File getFile() {
		return file;
	}
}
