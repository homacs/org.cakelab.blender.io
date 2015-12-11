package org.cakelab.blender.io.block.alloc;

import java.util.Iterator;

public class ChunkIterator implements Iterator<Chunk> {

	private Chunk virtualHead;
	private Chunk current;
	private ChunkList list;

	public ChunkIterator(ChunkList list) {
		this.list = list;
		// create a virtual head pointing towards the start of the list.
		this.virtualHead = this.current = new Chunk(null, list.head);
	}

	@Override
	public boolean hasNext() {
		return current.next != null;
	}

	@Override
	public Chunk next() {
		return current = current.next;
	}

	@Override
	public void remove() {
		if (current == virtualHead) throw new UnsupportedOperationException("remove() removes the _last_ element returned by next(): You have to call next at least once.");
		// we remove it from the list, but keep 
		// it as current to find the next without further 
		// condition checking.
		list.remove(current);
	}

}
