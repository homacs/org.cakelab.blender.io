package org.cakelab.blender.io.block.alloc;

import org.cakelab.blender.nio.UnsignedLong;



public class Chunk {
	public static enum State {
		FREE,
		ALLOCATED
	}
	State state;
	long address;
	long size;
	
	Chunk prev;
	Chunk next;
	
	
	Chunk(Chunk prev, Chunk next) {
		this.prev = prev;
		this.next = next;
	}

	public Chunk(long address, long size, State state) {
		this(null, null);
		this.address = address;
		this.size = size;
		this.state = state;
	}

	
	public long end() {
		return UnsignedLong.plus(address, size);
	}
	
	public boolean contains(long address) {
		return UnsignedLong.le(this.address, address) 
				&& UnsignedLong.gt(UnsignedLong.plus(this.address, this.size), address);
	}
	
}
