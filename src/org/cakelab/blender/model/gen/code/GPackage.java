package org.cakelab.blender.model.gen.code;

import java.io.File;

public class GPackage {
	private String name;
	private File dir;
	
	
	
	public GPackage(File parentDir, String packageName) {
		name = packageName;
		dir = new File(parentDir, packageName.replace('.', File.separatorChar));
		dir.mkdirs();
	}

	public String getName() {
		return name;
	}

	public File getDir() {
		return dir;
	}
	
	@Override
	public String toString() {
		return name;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		GPackage other = (GPackage) obj;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		return true;
	}

}
