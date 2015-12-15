package org.cakelab.blender.io.alloc;

import org.cakelab.blender.io.block.alloc.Allocator;
import org.cakelab.blender.nio.UnsignedLong;

public class Test {
	public static void main(String[] args) {
		Allocator allocator = new Allocator(UnsignedLong.MIN_VALUE + 4096L, UnsignedLong.MAX_VALUE);
		long a1 = allocator.alloc(1024);
		long a2 = allocator.alloc(4096);
		
		assert(UnsignedLong.lt(a1, a2));
		assert(UnsignedLong.minus(a2, a1) == 1024);
		
	}
}
