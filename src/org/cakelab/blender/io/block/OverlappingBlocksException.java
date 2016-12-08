package org.cakelab.blender.io.block;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;

import org.cakelab.blender.io.dna.DNAModel;
import org.cakelab.blender.io.dna.DNAStruct;

/**
 * Thrown if a .blend file contains overlapping blocks not handled in offheap areas.
 * @author homac
 *
 */
public class OverlappingBlocksException extends RuntimeException {
	private static final long serialVersionUID = 1L;

	private HashMap<Block, ArrayList<Block>> overlaps = new HashMap<Block, ArrayList<Block>>();

	private StringBuffer message;

	public OverlappingBlocksException() {
		super();
		message = new StringBuffer("File contains overlapping blocks which are not properly handled by this version of Java .Blend");
	}
	
	
	
	@Override
	public String getMessage() {
		return toString();
	}



	@Override
	public String getLocalizedMessage() {
		return toString();
	}



	@Override
	public String toString() {
		return message.toString() + "\nPlease refer to Java .Blend's documentation to find a solution or contact the developer (http://homac.cakelab.org/contact/).";
	}



	public HashMap<Block,ArrayList<Block>> getOverlappingBlocks() {
		return overlaps;
	}
	
	public void add(Block a, Block b) {
		ArrayList<Block> list = overlaps.get(a);
		if (list == null) {
			list = new ArrayList<Block>();
			overlaps.put(a, list);
		}
		list.add(b);
	}

	public void addDetailedInfo(DNAModel model) {
		
		message.append("\n");
		
		try {
			for (Entry<Block, ArrayList<Block>> e : overlaps.entrySet()) {
				Block a = e.getKey();
				
				message.append("block " + getBlockInfo(a, model) + " overlaps the following blocks: \n");
				for (Block b : e.getValue()) {
					message.append("\t").append(getBlockInfo(b, model));
				}
				
			}
		} catch (Throwable t) {
			// prevent us from running in another exception without reporting the current exception.
			message.append("\nmissing information due to another internal exception during gathering of information");
		}
	}

	private String getBlockInfo(Block block, DNAModel model) {
		String info = "";
		DNAStruct s = model.getStruct(block.header.sdnaIndex);
		if (s != null) {
			info += s.getType().getName();
		}
		info += "(" + block.header.sdnaIndex + ")";
		return info;
	}
	
}
