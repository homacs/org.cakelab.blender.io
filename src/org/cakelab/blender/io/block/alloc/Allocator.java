package org.cakelab.blender.io.block.alloc;

import static org.cakelab.blender.io.block.alloc.Chunk.State.ALLOCATED;
import static org.cakelab.blender.io.block.alloc.Chunk.State.FREE;

import org.cakelab.blender.nio.UnsignedLong;

/**
 * The allocator manages regions of allocated and free memory (chunks).
 * <p>
 * Purpose of the allocator is basically to determine an appropriate 
 * address for a new block. The allocator will not allocate memory.
 * </p>
 * <p>
 * The allocator implemented here uses a simple algorithm with a single 
 * linked list of allocated and free chunks. To find free chunks of 
 * appropriate size it uses the next fit algorithm. Neighbouring chunks
 * of the same type (either allocated or free) get merged to reduce the
 * amount of chunks in the list.
 * </p>
 * 
 * @author homac
 *
 */
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

	/** 
	 * Allocate memory of given size and return its address.
	 * @param size
	 * @return
	 */
	public long alloc(long size) {
		long address = 0;
		// Note: we don't need to care about issuing out of memory 
		// exceptions, because the system will run out of memory 
		// earlier, since we consider a memory space which is much 
		// larger than system memory can actually be.
		
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
	
	/** free the given size of memory at the given address */
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
