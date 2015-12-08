package org.cakelab.blender.file.block;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import org.cakelab.blender.file.Encoding;
import org.cakelab.blender.file.util.CDataReadWriteAccess;
import org.cakelab.blender.file.util.Identifier;
import org.cakelab.blender.model.UnsignedLong;


public class BlockMap {

	// TODO: consider actual heap base?
	private long heapBase = 0x8800000L;
	private int cursorRR = -1;
	private ArrayList<Block> blocks = new ArrayList<Block>();
	private Encoding encoding;
	
	
	
	public BlockMap(Encoding encoding) {
		this.encoding = encoding;
	}
	
	
	public void addAll(ArrayList<Block> blocks) {
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
		if (i > 0) {
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
		
		if (block != null && block.header.address < heapBase) {
			heapBase = block.header.address;
		}
		
		return block;
	}

	
	public Block allocate(Identifier blockCode, int size) {
		Block block = null;
		if (blocks.isEmpty()) {
			long address = heapBase;
			block = createBlock(blockCode, address, size);
			blocks.add(block);
			cursorRR = 0;
		} else {
			Block lowerNeighbour = blocks.get(cursorRR);
			cursorRR = nextRRIndex(cursorRR);
			for (int i = 0; i < blocks.size(); i++) {
				Block upperNeighbour = blocks.get(cursorRR);
				long space = UnsignedLong.minus(upperNeighbour.header.address, lowerNeighbour.header.address) - lowerNeighbour.header.size;
				if (UnsignedLong.le(size, space)) {
					long address = UnsignedLong.plus(lowerNeighbour.header.address, lowerNeighbour.header.size);
					block = createBlock(blockCode, address, size);
					blocks.add(cursorRR, block);
					break;
				} else {
					lowerNeighbour = upperNeighbour;
					cursorRR = nextRRIndex(cursorRR);
				}
			}
		}
		return block;
	}
	
	
	private int nextRRIndex(int index) {
		return (index + 1) % blocks.size();
	}


	private Block createBlock(Identifier blockCode, long address, int size) {
		CDataReadWriteAccess rwAccess = CDataReadWriteAccess.create(new byte[size], address, encoding);
		return new Block(new BlockHeader(blockCode, size, address), rwAccess);
	}
	
	
	public boolean exists(long address) {
		return getBlock(address) != null;
	}


	public Encoding getEncoding() {
		return encoding;
	}

	
}
