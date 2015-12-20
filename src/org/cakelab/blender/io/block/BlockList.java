package org.cakelab.blender.io.block;

import java.lang.reflect.Array;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

public class BlockList implements List<Block> {

	public class BlockListIterator implements ListIterator<Block> {

		private Block current;
		private BlockList list;

		public BlockListIterator(BlockList list) {
			this.list = list;
			// fake start
			current = new Block();
			current.next = list.first;
		}

		@Override
		public boolean hasNext() {
			return current.next != null;
		}

		@Override
		public Block next() {
			current = current.next;
			return current;
		}

		@Override
		public void remove() {
			list.remove(this);
		}

		@Override
		public boolean hasPrevious() {
			return current.prev != null;
		}

		@Override
		public Block previous() {
			current = current.prev;
			return current;
		}

		@Override
		public int nextIndex() {
			throw new UnsupportedOperationException();
		}

		@Override
		public int previousIndex() {
			throw new UnsupportedOperationException();
		}

		@Override
		public void set(Block e) {
			list.replace(current, e);
		}

		@Override
		public void add(Block e) {
			list.insert(e, current);
		}

	}

	private Block first;
	private int size;
	private Block last;

	
	public BlockList() {
		size = 0;
		first = last = null;
	}
	
	@Override
	public int size() {
		return size;
	}

	@Override
	public boolean isEmpty() {
		return size == 0;
	}

	@Override
	public boolean contains(Object o) {
		if (o instanceof Block) {
			for (Block block : this) {
				if (((Block)o) == block) {
					return true;
				}
			}
		}
		return false;
	}

	@Override
	public Iterator<Block> iterator() {
		return new BlockListIterator(this);
	}

	@Override
	public Object[] toArray() {
		Block[] array = new Block[size];
		int i = 0;
		for (Block block : this) {
			array[i++] = block;
		}
		return array;
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> T[] toArray(T[] array) {
		if (array.getClass().getComponentType() != Block.class) {
			throw new ArrayStoreException("has to be Block[]");
		}
		if (array.length < size) {
			array = (T[]) Array.newInstance(Block.class, size);
		}
		int i = 0;
		for (Block block : this) {
			array[i++] = (T) block;
		}
		return array;
	}

	@Override
	public boolean add(Block e) {
		size++;
		if (last == null) {
			first = e;
		} else {
			last.next = e;
			e.prev = last;
		}
		last = e;
		return true;
	}

	/**
	 * newBlock gets inserted before nextBlock.
	 * @param newBlock
	 * @param nextBlock
	 */
	public void insert(Block newBlock, Block nextBlock) {
		if (nextBlock.prev != null) {
			nextBlock.prev.next = newBlock;
			newBlock.prev = nextBlock.prev;
		}
		newBlock.next = nextBlock;
		nextBlock.prev = newBlock;
	}

	public void replace(Block oldBlock, Block newBlock) {
		if (first == oldBlock) {
			first = newBlock;
		}
		if (last == oldBlock) {
			last = newBlock;
		}
		
		newBlock.prev = oldBlock.prev;
		newBlock.next = oldBlock.next;
		
		if (newBlock.prev != null) {
			newBlock.prev.next = newBlock;
		}
		if (newBlock.next != null) {
			newBlock.next.prev = newBlock;
		}
	}


	
	@Override
	public boolean remove(Object o) {
		Block current = (Block)o;
		if (current.next != null) {
			if (current.next.prev != current) return false;
			current.next.prev = current.prev;
		}
		if (current.prev != null) {
			if (current.prev.next != current) return false;
			current.prev.next = current.next;
		}
		if (current == first) {
			first = current.next;
		}
		if (current == last) {
			last = current.prev;
		}
		size--;
		
		return true;
	}

	@Override
	public boolean containsAll(Collection<?> c) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean addAll(Collection<? extends Block> c) {
		for (Block block : c) {
			add(block);
		}
		return true;
	}

	@Override
	public boolean addAll(int index, Collection<? extends Block> c) {
		Block next = get(index);

		for (Block b : c) {
			insert(b, next);
		}
		return true;
	}

	@Override
	public boolean removeAll(Collection<?> c) {
		boolean result = false;
		for (Object b : c) {
			result |= remove(b);
		}
		return result;
	}

	@Override
	public boolean retainAll(Collection<?> c) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void clear() {
		size = 0;
		first = null;
		last = null;
	}

	@Override
	public Block get(int index) {
		Iterator<Block> it = iterator();
		for (int i = 0; i < index; i++) {
			if (it.hasNext()) {
				it.next();
			} else {
				throw new IndexOutOfBoundsException();
			}
		}
		if (!it.hasNext()) {
			throw new IndexOutOfBoundsException();
		}
		return it.next();
	}

	@Override
	public Block set(int index, Block element) {
		Block old = null;
		if (size == 0) {
			first = last = element;
		} else {
			old = get(index);
			replace(old, element);
		}
		return old;
	}

	@Override
	public void add(int index, Block element) {
		Block pos = get(index);
		insert(element, pos);
	}

	@Override
	public Block remove(int index) {
		Block result = get(index);
		remove(result);
		return result;
	}

	@Override
	public int indexOf(Object o) {
		int index;
		Iterator<Block> it = iterator();
		for (index = 0; index < size; index++) {
			if (o == it.next()) {
				return index;
			}
		}
		return 0;
	}

	@Override
	public int lastIndexOf(Object o) {
		throw new UnsupportedOperationException();
	}

	@Override
	public ListIterator<Block> listIterator() {
		return new BlockListIterator(this);
	}

	@Override
	public ListIterator<Block> listIterator(int index) {
		BlockListIterator it = new BlockListIterator(this);
		for (int i = 0; i <= index; i++) {
			if (it.hasNext()) {
				it.next();
			} else {
				throw new IndexOutOfBoundsException();
			}
		}
		return it;
	}

	@Override
	public List<Block> subList(int fromIndex, int toIndex) {
		throw new UnsupportedOperationException();
	}

}
