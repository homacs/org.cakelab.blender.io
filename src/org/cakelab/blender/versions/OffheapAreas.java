package org.cakelab.blender.versions;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.cakelab.blender.io.FileHeader.Version;

/**
 * This class is just required internally (e.g. by the class {@link BlenderFile).
 * 
 * The class contains a list of so-called offheap areas 
 * (refer to Java .Blend documentation for more information on offheap areas).
 * Basically, offheap areas refer to data in blocks that may or may not overlap
 * the address space of other blocks. Thus, those blocks can only be retrieved
 * by an exact match to its start address (which is still unique). The I/O subsystem
 * identifies offheap areas by the SDNA index of the struct stored in the blocks 
 * of those areas.
 * 
 * @author homac
 *
 */
public class OffheapAreas {
	
	static class Entry {
		public Entry(int versionCode, String[] value) {
			this.key = new Version(versionCode);
			this.value = value;
		}
		public Entry(int versionCode) {
			this(versionCode, null);
		}
		Version key;
		String[] value;
	}
	
	
	private static final List<Entry> map;

	static {
		map = new ArrayList<Entry>();
		//
		// All entries have to be sorted in ascending order!
		//
		map.add(new Entry(  0, new String[]{"FileGlobal"}));
		map.add(new Entry(276, new String[]{"FileGlobal", "TreeStoreElem"}));
	}
	
	private static final Comparator<Entry> COMPARATOR = new Comparator<Entry>() {
		@Override
		public int compare(Entry o1, Entry o2) {
			return o1.key.getCode() - o2.key.getCode();
		}
		
	};
	
	/**
	 * Retrieve the list of offheap areas (struct names) for a given version code.
	 * @param versionCode Blenders version specifier: MAJOR*100 + MINOR
	 * @return List of struct names of offheap areas.
	 */
	public static String[] get(int versionCode) {
		Entry key = new Entry(versionCode);
		int index = Collections.binarySearch(map, key, COMPARATOR);
		if (index < 0) {
			// No exact match -> take the entry less equal to the one we are searching
			index = -index -2;
		}
		
		return map.get(index).value;
	}
	
}
