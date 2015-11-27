package org.cakelab.blender.file.block;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;


public class BlockMap {

	ArrayList<Block> blocks = new ArrayList<Block>();
	
	public void addAll(ArrayList<Block> blocks) {
		Collections.sort(blocks, new Comparator<Block>() {

			@Override
			public int compare(Block b1, Block b2) {
				return b1.compareTo(b2.header.getAddress());
			}
			
		});
		
		this.blocks = blocks;
	}

	public Block getBlock(long address) {
		int i = Collections.binarySearch(blocks, address);
		if (i > 0) {
			return blocks.get(i);
		} else {
			// if the address lies between two start blocks, then 
			// -i-1 is the pos of the block with start address larger
			// than address. But we need the block with a start address
			// lower than address. Thus, -i-2
			i = -i-2;
			if (i >= 0) {
				Block block = blocks.get(i);
				if (address < (block.header.getAddress() + block.header.getSize())) {
					return block;
				}
			}
		}
		// not in list
		return null;
	}

	public boolean exists(long address) {
		return getBlock(address) != null;
	}
	
}
