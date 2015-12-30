package org.cakelab.blender.io.block;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.cakelab.blender.io.Encoding;
import org.cakelab.blender.io.block.alloc.Allocator;
import org.cakelab.blender.io.util.CDataReadWriteAccess;
import org.cakelab.blender.io.util.Identifier;
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
 * {@link BlockList}.
 * </p>
 * <p>
 * To support allocation of blocks and issue addresses to them, 
 * the block table has access to an {@link Allocator}.
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
	 * @param blocks to be inserted in the new block table.
	 */
	public BlockTable(Encoding encoding, List<Block> blocks) {
		this(encoding);
		this.sorted.addAll(blocks);
		Collections.sort(this.sorted, new Comparator<Block>() {
			@Override
			public int compare(Block b1, Block b2) {
				return b1.compareTo(b2.header.getAddress());
			}
		});
		if (!blocks.isEmpty()) {
			Block first = blocks.get(0);
			assert (UnsignedLong.ge(first.header.address, HEAPBASE));
		}
	}


	/** Returns the block which contains the given address.
	 * @param address
	 * @return
	 */
	public Block getBlock(long address) {
		Block block = null;
		
		int i = Collections.binarySearch(sorted, address);
		if (i >= 0) {
			block = sorted.get(i);
		} else {
			// if the address lies between two block starts addresses, then 
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

		
		// insert block in list
		int i = Collections.binarySearch(sorted, address);
		assert(i < 0);
		i = -i -1;
		sorted.add(i, block);
		return block;
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
	 * This method allocates memory and assigns it to new a block with the given code.
	 * 
	 * @param blockCode
	 * @param size
	 * @return
	 */
	public Block allocate(Identifier code, long size) {
		return allocate(code, (int)size);
	}

	/** This method removes the given block from the block list, and releases
	 * the its allocated memory region (to be available for allocation again).
	 * @param block
	 */
	public void free(Block block) {
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
	 * Determines if a block with this address exists.
	 * @param address
	 * @return
	 */
	public boolean exists(long address) {
		return getBlock(address) != null;
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
		for (Block block : sorted) {
			if (block.header.code.equals(blockCode)) {
				result.add(block);
			}
		}
		return result;
	}

	/** Returns the allocator used by this block table. */
	public Allocator getAllocator() {
		checkAllocator();
		return allocator;
	}

	/**
	 * This method returns the internal list of blocks which is sorted by
	 * their address.
	 * 
	 * Please note, that the list of blocks returned by this method 
	 * is sorted by block.header.address. If you are looking for the
	 * list of blocks in their original sequence in the file than refer
	 * to {@link BlenderFile#getBlocks()}
	 * @return
	 */
	public List<Block> getBlocksSorted() {
		return sorted;
	}



	
}
