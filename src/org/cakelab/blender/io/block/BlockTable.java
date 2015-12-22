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


public class BlockTable {


	// TODO: ZZZ consider actual heap base?
	private static final long HEAPBASE = UnsignedLong.plus(UnsignedLong.MIN_VALUE, 4096L);
	private static final long HEAPSIZE = UnsignedLong.minus(UnsignedLong.MAX_VALUE, HEAPBASE);
	
	
	/** list of blocks sorted by block.header.address */
	private List<Block> sorted = new ArrayList<Block>();

	/** encoding used by all blocks of this block table. */
	private Encoding encoding;
	
	private Allocator allocator;
	/** 
	 * We do lazy initialisation of the allocator. 
	 * This improves performance for people who don't 
	 * need allocation at all.
	 */
	private boolean allocatorInitialised;
	
	
	
	public BlockTable(Encoding encoding) {
		allocator = new Allocator(HEAPBASE, HEAPSIZE);
		allocatorInitialised = false;
		this.encoding = encoding;
	}
	
	
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



	public Block getBlock(long address) {
		Block block = null;
		
		int i = Collections.binarySearch(sorted, address);
		if (i >= 0) {
			block = sorted.get(i);
		} else {
			// if the address lies between two start sorted, then 
			// -i-1 is the pos of the block with start address larger
			// than address. But we need the block with a start address
			// lower than address. Thus, -i-2
			i = -i-2;
			if (i >= 0) {
				Block b = sorted.get(i);
				if (address < (b.header.getAddress() + b.header.getSize())) {
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
	
	public Block allocate(Identifier blockCode, long size,
			int sdnaIndex, int count) {
		Block block = allocate(blockCode, size*count);
		block.header.sdnaIndex = sdnaIndex;
		block.header.count = count;
		return block;
	}


	
	public Block allocate(Identifier code, long size) {
		return allocate(code, (int)size);
	}

	public void free(Block block) {
		// When the allocator gets initialised, it will receive all sorted
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
	 * If not it initialised it by declaring the memory areas of all sorted
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


	public boolean exists(long address) {
		return getBlock(address) != null;
	}


	public Encoding getEncoding() {
		return encoding;
	}

	public List<Block> getBlocks(Identifier blockCode) {
		List<Block> result = new ArrayList<Block>();
		for (Block block : sorted) {
			if (block.header.code.equals(blockCode)) {
				result.add(block);
			}
		}
		return result;
	}

	public Allocator getAllocator() {
		checkAllocator();
		return allocator;
	}

	/**
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
