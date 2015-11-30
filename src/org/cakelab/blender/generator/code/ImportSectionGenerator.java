package org.cakelab.blender.generator.code;

import java.util.HashSet;

public class ImportSectionGenerator extends CodeGenerator {

	private HashSet<Class<?>> classes;
	private HashSet<GPackage> packages;


	public ImportSectionGenerator() {
		super(0);
		this.classes = new HashSet<Class<?>>();
		this.packages = new HashSet<GPackage>();
	}
	
	
	public void reset() {
		this.classes.clear();
	}
	
	public String toString() {
		StringBuffer imports = new StringBuffer();
		for (GPackage pkg : packages) {
			imports.append("import " + pkg + ".*;").append(NL);
		}
		for (Class<?> clazz : classes) {
			if (!packages.contains(clazz.getPackage().getName())) {
				imports.append("import " + clazz.getCanonicalName() + ";").append(NL);
			}
		}
		return imports.toString();
	}


	public void add(Class<?> clazz) {
		classes.add(clazz);
	}


	public void add(GPackage package2bImported) {
		packages.add(package2bImported);
	}

}
