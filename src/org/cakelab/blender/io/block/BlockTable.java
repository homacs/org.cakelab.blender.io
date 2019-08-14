package org.cakelab.blender.io.block;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.cakelab.blender.io.Encoding;
import org.cakelab.blender.io.block.alloc.Allocator;
import org.cakelab.blender.io.util.CDataReadWriteAccess;
import org.cakelab.blender.io.util.Identifier;
import org.cakelab.blender.nio.CArrayFacade;
import org.cakelab.blender.nio.CFacade;
import org.cakelab.blender.nio.CPointer;
import org.cakelab.blender.nio.UnsignedLong;

/**
 * A block table is like a page table for blocks in a blender file. 
 * <p>
 * A block table provides access to {@link Block}s of a given 
 * {@link BlenderFile} instance based on memory addresses 
 * (see {@link BlockHeader#getAddress()}) and supports in 
 * instantiation of new blocks.
 * </p>
 * <p>
 * All blocks of a block table have the same encoding 
 * which is also the same encoding used by the entire 
 * blender file (see {@link Encoding} and {@link BlenderFile}).
 * </p>
 * <p>
 * Blocks in a block list are ordered by their address to improve
 * lookup performance. Blocks in a blender file have to be stored 
 * in a different order which is supposed to be maintained in a 
 * {@link BlockList} (required only when writing to .blend files).
 * </p>
 * <p>
 * To support allocation of blocks and issue addresses to them, 
 * the block table has access to an {@link Allocator}.
 * </p>
 * <p>
 * A block table may or may not reference so-called offheap areas 
 * (see {@link org.cakelab.blender.versions.OffheapAreas}). Each 
 * offheap area contains a list of blocks of a specific struct type.
 * When retrieving a block for a given address, the block table uses
 * the SDNA index to potentially search the assigned offheap area, for the block
 * by an exact match of the start address.
 * If no offheap area exists for the given struct type, it will search 
 * the block in regular heap area.
 * </p>
 * 
 * @author homac
 *
 */
public class BlockTable {

	/** Heap base is considered as the lowest possible address */
	private static final long HEAPBASE = UnsignedLong.plus(UnsignedLong.MIN_VALUE, 4096L);
	/** Heap size is the maximum amount of memory to be stored in a blender file. */
	private static final long HEAPSIZE = UnsignedLong.minus(UnsignedLong.MAX_VALUE, HEAPBASE);
	
	/** This comparator can be used with methods such as {@link Collections#sort(List, Comparator)}
	 * to sort blocks ascending by address.
	 */
	public static final Comparator<? super Block> BLOCKS_ASCENDING_ADDRESS = new Comparator<Block>() {
		@Override
		public int compare(Block b1, Block b2) {
			return b1.compareTo(b2.header.getAddress());
		}
	};
	
	
	/** list of blocks sorted by block.header.address */
	private List<Block> sorted = new ArrayList<Block>();

	/** encoding used by all blocks of this block table. */
	private Encoding encoding;
	
	/** allocator used by this block table */
	private Allocator allocator;
	
	/** 
	 * We do lazy initialisation of the allocator. 
	 * This improves performance for people who don't 
	 * need allocation at all.
	 */
	private boolean allocatorInitialised;
	
	/**
	 * Lookup table of offheap areas identified by SDNA indices of the 
	 * structs contained in affected, potentially overlapping blocks.
	 */
	private HashMap<Integer, BlockTable> offheapAreas;
	
	
	/**
	 * Instantiates a new block table with the given encoding.
	 * 
	 * @param encoding
	 */
	public BlockTable(Encoding encoding) {
		allocator = new Allocator(HEAPBASE, HEAPSIZE);
		allocatorInitialised = false;
		this.encoding = encoding;
	}
	
	/**
	 * Instantiates a new block table with the given encoding and
	 * initialises it with the blocks of the given list.
	 * @param encoding
	 * @param offheapStructs List of SDNA indices which are in offheap areas
	 * @param blocks to be inserted in the new block table.
	 */
	public BlockTable(Encoding encoding, List<Block> blocks, int[] offheapStructs) {
		this(encoding);
		
		this.sorted.addAll(blocks);
		Collections.sort(this.sorted, BLOCKS_ASCENDING_ADDRESS);
		
		initOffheapAreas(offheapStructs);

		// SANITY CHECK HERE
		// Check if the first (actual) address is reasonable
		// We assume, that addresses are at least greater than HEAPBASE.
		// So far, even offheap areas have kind of reasonable start addresses.
		// The only exception is the ENDB block, which has an address of NULL.
		// TODO: consider offheap areas beyond HEAPBASE
		if (!sorted.isEmpty()) {
			Block first = sorted.get(0);
			if (first.header.code.equals(BlockCodes.ID_ENDB)) {
				if (sorted.size()>1) first = sorted.get(1);
				else first = null;
			}
			assert (first != null && UnsignedLong.ge(first.header.address, HEAPBASE));
		}
	}


	/**
	 * Creates offheap areas and moves all blocks of structs which are declared 
	 * to be not in the heap address space to their respective offheap areas.
	 * @param offheap List of offheap areas.
	 */
	private void initOffheapAreas(int[] offheap) {
		if (offheap == null) return;
		
		offheapAreas = new HashMap<Integer, BlockTable>(offheap.length);
		for (int sdna : offheap) {
			offheapAreas.put(sdna, new BlockTable(encoding));
		}
		for (Iterator<Block> it = sorted.iterator(); it.hasNext();) {
			Block b = it.next();
			for (int sdna : offheap) {
				if (b.header.sdnaIndex == sdna) {
					offheapAreas.get(sdna).add(b);
					it.remove();
					break;
				}
			}
		}
		
		if (null == System.getProperty("org.cakelab.blender.NoChecks")) {
			checkBlockOverlaps();
		}
	}

	/**
	 * Method to check for overlapping blocks in heap address space for debugging purposes.
	 */
	private void checkBlockOverlaps() {
		boolean valid = true;
		
		OverlappingBlocksException overlapping = new OverlappingBlocksException();
		
		for (int i = 0; i < sorted.size(); i++) {
			Block cur = sorted.get(i);
			for (int j=i+1; j < sorted.size(); j++) {
				Block b = sorted.get(j);
				if (cur.contains(b.header.address)) {
					overlapping.add(cur, b);
					valid = false;
				} else {
					break;
				}
			}
		}
		if (!valid) {
			throw overlapping;
		}
	}

	/**
	 * Returns the block for a given address and type (array, pointer, struct or scalar).
	 * 
	 * @param address
	 * @param type
	 * @return
	 */
	public Block getBlock(long address, Class<?>[] type) {
		if (type[0].equals(CPointer.class) || type[0].equals(CArrayFacade.class)) {
			return getBlock(address, type[1]);
		} else {
			return getBlock(address, type[0]);
		}
		
	}
	
	/** returns the block which contains the data of the given address and type (struct or scalar).
	 * @param address
	 * @param type
	 * @return
	 */
	public Block getBlock(long address, Class<?> type) {
		int sdnaIndex = -1;
		Class<?> superClass = type.getSuperclass();
		if (superClass != null && superClass.equals(CFacade.class)) {
			try {
				Field f = type.getDeclaredField("__DNA__SDNA_INDEX");
				sdnaIndex = f.getInt(null);
			} catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException e) {
				throw new RuntimeException("internal error", e);
			}
		}
		return getBlock(address, sdnaIndex);
	}
	
	/**
	 * Returns the block which contains the given address.
	 * The sdnaIndex is required to access offheap areas. 
	 * The method identifies whether the struct is in an 
	 * offheap area or not. If the data is know to be on heap, 
	 * sdnaIndex can be -1 too.
	 * 
	 * @param address
	 * @param sdnaIndex
	 * @return
	 */
	public Block getBlock(long address, int sdnaIndex) {
		BlockTable table = this;
		if (offheapAreas != null && sdnaIndex >= 0) {
			BlockTable t = offheapAreas.get(sdnaIndex);
			if (t != null) {
				return t.findBlock(sdnaIndex);
			}
		}
		return table.getBlock(address);
	}
	
	
	/** Returns the block which contains the given address.
	 * @param address
	 * @return
	 */
	protected Block getBlock(long address) {
		if (address == 0) return null;
		
		Block block = null;
		
		int i = Collections.binarySearch(sorted, address);
		if (i >= 0) {
			block = sorted.get(i);
		} else {
			// if the address lies between two block start addresses, then 
			// -i-1 is the pos of the block with start address larger
			// than address. But we need the block with a start address
			// lower than address. Thus, -i-2
			i = -i-2;
			if (i >= 0) {
				Block b = sorted.get(i);
				if (address < (b.header.getAddress() + b.header.getSize())) {
					// block found
					block = b;
				}
			}
		}
		
		return block;
	}

	/** 
	 * Returns the block, which is associated with the given start address.
	 * Thus, it will only return a block which has an exact match with the given address.
	 * @param startAddress Start address of the block to search for.
	 * @return The block associated with the given address or null if none was found.
	 */
	public Block findBlock(long startAddress) {
		int i = Collections.binarySearch(sorted, startAddress);
		Block block = null;
		if (i >= 0) {
			block = sorted.get(i);
		}
		return block;
	}
	
	/**
	 * This method allocates memory and assigns it to a block with the given code.
	 * 
	 * @param blockCode
	 * @param size
	 * @return
	 */
	public Block allocate(Identifier blockCode, int size) {
		checkAllocator();
		long address = allocator.alloc(size);

		
		CDataReadWriteAccess rwAccess = CDataReadWriteAccess.create(new byte[size], address, encoding);
		Block block = new Block(new BlockHeader(blockCode, size, address), rwAccess);
		
		add(block);
		
		return block;
	}

	/**
	 * Method to add a block to the ascending sorted list.
	 * @param block
	 */
	protected void add(Block block) {
		// insert block in list
		int i = Collections.binarySearch(sorted, block.header.address);
		assert(i < 0);
		i = -i -1;
		sorted.add(i, block);
	}
	
	
	/**
	 * This method allocates memory for 'count' structs of type 'sdnaIndex' 
	 * and assigns it to a new block with the given blockCode.
	 * 
	 * @param blockCode
	 * @param size
	 * @return
	 */
	public Block allocate(Identifier blockCode, long size,
			int sdnaIndex, int count) {
		Block block = allocate(blockCode, size*count);
		block.header.sdnaIndex = sdnaIndex;
		block.header.count = count;
		return block;
	}


	
	/**
	 * This method allocates memory on heap and assigns it to a new block with the given code.
	 * <p>
	 * <em>Note: If you have declared offheap areas, and want to allocate a block for a struct 
	 * which is declared to be offheap, then use the method 
	 * {@link #allocate(Identifier, long, int, int) instead!</p>
	 * @see #allocate(Identifier, long, int, int)
	 * 
	 * @param blockCode Block code to be assigned to the block
	 * @param size Size of the block body in bytes.
	 * @return Allocated block.
	 */
	public Block allocate(Identifier code, long size) {
		return allocate(code, (int)size);
	}

	/** This method removes the given block from the block list, and releases
	 * its allocated memory region (to be available for allocation again).
	 * @param block
	 */
	public void free(Block block) {
		BlockTable offheapArea = offheapAreas.get(block.header.sdnaIndex);
		if (offheapArea != null) {
			offheapArea.free(block);
		} else {
			// When the allocator gets initialised, it will receive all blocks
			// that still exist. Thus, we don't need to do anything
			// if it is not initialised.
			if (allocatorInitialised) {
				allocator.free(block.header.address, block.header.size);
			}
			
			// remove block from table
			int i = Collections.binarySearch(sorted, block.header.address);
			assert(i >= 0);
			sorted.remove(i);
		}
	}
	
	/**
	 * Lazy initialisation of the allocator.
	 * This method checks whether the allocator has been initialised.
	 * If not it initialised it by declaring the memory areas of all blocks
	 * as allocated.
	 */
	private void checkAllocator() {
		if (!allocatorInitialised) {
			for (Block block : sorted) {
				allocator.declareAllocated(block.header.address, block.header.size);
			}
			allocatorInitialised = true;
		}
	}

	/**
	 * Determines if a block, which contains this address, exists on heap.
	 * 
	 * <p>
	 * <em>Use {@link #exists(long, int)} to check offheap areas too.</em>
	 * </p>
	 * @see #exists(long, int)
	 * @param address
	 * @return
	 */
	public boolean exists(long address) {
		return getBlock(address) != null;
	}

	/**
	 * Determines if a block with this startAddress exists either on or off heap.
	 */
	public boolean exists(long startAddress, int sdnaIndex) {

		BlockTable offheapArea = null;
		if (offheapAreas != null) {
			offheapArea = offheapAreas.get(sdnaIndex);
		}
		
		if (offheapArea != null) {
			return offheapArea.findBlock(startAddress) != null;
		} else {
			return findBlock(startAddress) != null;
		}

	}

	/** 
	 * @return encoding used by all blocks of this block table.
	 */
	public Encoding getEncoding() {
		return encoding;
	}

	/**
	 * Returns a list of blocks which have the given block code.
	 * @param blockCode
	 * @return
	 */
	public List<Block> getBlocks(Identifier blockCode) {
		List<Block> result = new ArrayList<Block>();
		getBlocks(blockCode, result);
		if (offheapAreas != null) {
			for (BlockTable offheapArea : offheapAreas.values()) {
				offheapArea.getBlocks(blockCode, result);
			}
		}
		return result;
	}
	
	/**
	 * Retrieve all blocks with the given block code which are on heap.
	 * <em>This does not include offheap areas!</em>
	 * @param blockCode
	 * @param list 
	 */
	public void getBlocks(Identifier blockCode, List<Block> list) {
		for (Block block : sorted) {
			if (block.header.code.equals(blockCode)) {
				list.add(block);
			}
		}
	}
	

	/** Returns the allocator used by this block table. 
	 * <p>
	 * <em>This allocator does not know about offheap areas.</em>
	 * </p>
	 * */
	public Allocator getAllocator() {
		checkAllocator();
		return allocator;
	}

	/**
	 * This method returns the internal list of blocks which are on heap 
	 * and sorted by their address.
	 * <p>
	 * Please note, that the list of blocks returned by this method 
	 * is sorted by block.header.address and does not contain blocks from
	 * offheap areas. If you are looking for the <b>complete</b> list of blocks 
	 * in their original sequence in the file than refer
	 * to {@link BlenderFile#getBlocks()}
	 * </p>
	 * @return
	 */
	public List<Block> getBlocksSorted() {
		return sorted;
	}

	
}
