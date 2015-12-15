package org.cakelab.blender.io.block.alloc;

import java.util.Iterator;

import org.cakelab.blender.nio.UnsignedLong;


public class ChunkList implements Iterable<Chunk>{

	// TODO: ZZZ use red black tree instead of a list
	Chunk head;
	Chunk tail;
	
	public ChunkList(Chunk head) {
		this.head = this.tail = head;
	}
	
	
	public Chunk find(long address) {
		for (Chunk partition : this) {
			if (partition.contains(address)) {
				return partition;
			}
		}
		return null;
	}

	@Override
	public Iterator<Chunk> iterator() {
		return new ChunkIterator(this);
	}


	public Chunk split(Chunk chunk, long address, long size) {
		if (chunk.address < address) {
			long addrDiff = UnsignedLong.minus(address, chunk.address);
			Chunk newChunk = new Chunk(address, UnsignedLong.minus(chunk.size, addrDiff), chunk.state);
			insertAfter(chunk, newChunk);
			chunk.size = addrDiff;
			chunk = newChunk;
		}
		
		//
		// from here on: partition.address == address
		//
		
		if (UnsignedLong.lt(size, chunk.size)) {
			long sizeDiff = UnsignedLong.minus(chunk.size, size);
			Chunk newChunk = new Chunk(UnsignedLong.plus(address, size), sizeDiff, chunk.state);
			insertAfter(chunk, newChunk);
			chunk.size = size;
		}
		
		return chunk;
	}


	private void insertAfter(Chunk prev, Chunk next) {
		if (prev == tail) tail = next;
		next.next = prev.next;
		if (next.next != null) next.next.prev = next;
		link(prev, next);
	}


	private void link(Chunk prev, Chunk next) {
		next.prev = prev;
		prev.next = next;
	}


	public Chunk merge(Chunk prev, Chunk next) {
		
		assert(prev.next == next && next.prev == prev);
		// We always merge with the follower in case an 
		// iterator points on one of the chunks. Just 
		// improves performance.
		if (prev == head) head = next;
		next.size = UnsignedLong.plus(prev.size, next.size);
		next.address = prev.address;
		next.prev = prev.prev;
		if (next.prev != null) {
			next.prev.next = next;
		}
		return next;
	}


	public void remove(Chunk current) {
		// list is supposed to be never empty
		assert(head != tail);
		if (current.prev == null) {
			head = current.next;
			head.prev = null;
		} else if (current.next == null) {
			tail = current.prev;
			tail.next = null;
		} else {
			link(current.prev, current.next);
		}
	}
	
}
