package org.cakelab.blender.io.block;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.cakelab.blender.io.Encoding;
import org.cakelab.blender.io.block.alloc.Allocator;
import org.cakelab.blender.io.util.CDataReadWriteAccess;
import org.cakelab.blender.io.util.Identifier;
import org.cakelab.blender.model.UnsignedLong;


public class BlockTable extends Allocator {

	// TODO: ZZZ consider actual heap base?
	private static final long HEAPBASE = UnsignedLong.plus(UnsignedLong.MIN_VALUE, 4096L);
	private static final long HEAPSIZE = UnsignedLong.minus(UnsignedLong.MAX_VALUE, HEAPBASE);
	private List<Block> blocks = new ArrayList<Block>();
	private Encoding encoding;
	/** 
	 * We do lazy initialisation of the allocator. 
	 * This improves performance for people who don't 
	 * need allocation at all.
	 */
	private boolean allocatorInitialised;
	
	
	
	public BlockTable(Encoding encoding) {
		super(HEAPBASE, HEAPSIZE);
		allocatorInitialised = false;
		this.encoding = encoding;
	}
	
	
	public BlockTable(Encoding encoding, List<Block> blocks) {
		this(encoding);
		addAll(blocks);
		if (!blocks.isEmpty()) {
			Block first = blocks.get(0);
			assert (UnsignedLong.ge(first.header.address, HEAPBASE));
		}
	}


	public void addAll(List<Block> blocks) {
		this.blocks.addAll(blocks);
		Collections.sort(this.blocks, new Comparator<Block>() {
			@Override
			public int compare(Block b1, Block b2) {
				return b1.compareTo(b2.header.getAddress());
			}
		});
	}

	public Block getBlock(long address) {
		Block block = null;
		
		int i = Collections.binarySearch(blocks, address);
		if (i >= 0) {
			block = blocks.get(i);
		} else {
			// if the address lies between two start blocks, then 
			// -i-1 is the pos of the block with start address larger
			// than address. But we need the block with a start address
			// lower than address. Thus, -i-2
			i = -i-2;
			if (i >= 0) {
				Block b = blocks.get(i);
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
		long address = alloc(size);

		
		CDataReadWriteAccess rwAccess = CDataReadWriteAccess.create(new byte[size], address, encoding);
		Block block = new Block(new BlockHeader(blockCode, size, address), rwAccess);
		
		// insert block in list
		int i = Collections.binarySearch(blocks, address);
		assert(i < 0);
		i = -i -1;
		blocks.add(i, block);
		
		return block;
	}
	
	public Block allocate(Identifier blockCode, long size,
			int sdnaIndex, int count) {
		Block block = allocate(blockCode, size);
		block.header.sdnaIndex = sdnaIndex;
		block.header.count = count;
		return block;
	}


	
	public Block allocate(Identifier code, long size) {
		return allocate(code, (int)size);
	}

	public void free(Block block) {
		// When the allocator gets initialised, it will receive all blocks
		// that still exist. Thus, we don't need to do anything
		// if it is not initialised.
		if (allocatorInitialised) {
			super.free(block.header.address, block.header.size);
		}
		
		// remove block from table
		int i = Collections.binarySearch(blocks, block.header.address);
		assert(i >= 0);
		blocks.remove(i);
	}
	
	/**
	 * Lazy initialisation of the allocator.
	 * This method checks whether the allocator has been initialised.
	 * If not it initialised it by declaring the memory areas of all blocks
	 * as allocated.
	 */
	private void checkAllocator() {
		if (!allocatorInitialised) {
			for (Block block : blocks) {
				declareAllocated(block.header.address, block.header.size);
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
		for (Block block : blocks) {
			if (block.header.code.equals(blockCode)) {
				result.add(block);
			}
		}
		return result;
	}


	
}
