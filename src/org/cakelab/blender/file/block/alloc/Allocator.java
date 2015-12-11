package org.cakelab.blender.file.block.alloc;

import static org.cakelab.blender.file.block.alloc.Chunk.State.FREE;
import static org.cakelab.blender.file.block.alloc.Chunk.State.ALLOCATED;

import org.cakelab.blender.model.UnsignedLong;

public class Allocator {

	ChunkList chunks;
	ChunkIterator cursor;
	
	public Allocator(long heapBase, long heapSize) {
		chunks = new ChunkList(new Chunk(heapBase, heapSize, FREE));
		cursor = (ChunkIterator) chunks.iterator();
	}
	
	/**
	 * This method is used during initialisation only to declare
	 * regions of memory to be allocated beforehand.
	 * 
	 * It defines a partition (address, size) to be 
	 * allocated.
	 * 
	 * 
	 * @param address
	 * @param size
	 */
	public void declareAllocated(long address, long size) {
		assert(address != 0);
		Chunk chunk = chunks.find(address);
		
		// some debugging: if this fails, then there are overlapping allocations/frees
		assert(chunk.contains(UnsignedLong.plus(address, size-1)));
		assert(chunk.state == FREE);
		chunk = chunks.split(chunk, address, size);
		chunk.state = ALLOCATED;
	}

	public long alloc(long size) {
		long address = 0;
		// Note: we don't need to care about issuing out of memory 
		// exceptions, because the system will do before we can.
		
		do {
			if (!cursor.hasNext()) {
				cursor = (ChunkIterator) chunks.iterator();
			}
			Chunk chunk = cursor.next();
			if (chunk.state == FREE && UnsignedLong.ge(chunk.size, size)) {
				address = chunk.address;
				chunk = chunks.split(chunk, chunk.address, size);
				chunk.state = ALLOCATED;
				tryMerge(chunk);
			}
		} while (address == 0);
		return address;
	}
	
	public void free(long address, long size) {
		assert(address != 0);

		Chunk chunk = chunks.find(address);
		// some debugging: if this fails, then there are overlapping allocations/frees
		assert(chunk.contains(UnsignedLong.plus(address, size-1)));
		assert(chunk.state == ALLOCATED);
		chunk = chunks.split(chunk, address, size);
		chunk.state = FREE;
	}
	
	/**
	 * This is an internal maintenance method which tries 
	 * to merge chunks of the same state.
	 * @param chunk
	 */
	private void tryMerge(Chunk chunk) {
		if (chunk.prev != null && chunk.prev.state == chunk.state) {
			chunk = chunks.merge(chunk.prev, chunk);
		}
		
		if (chunk.next != null && chunk.next.state == chunk.state) {
			chunk = chunks.merge(chunk, chunk.next);
		}
	}

}
