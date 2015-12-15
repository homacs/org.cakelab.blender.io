package org.cakelab.blender.generator.utils;

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
		GCodeSection imports = new GCodeSection(0);
		for (GPackage pkg : packages) {
			imports.appendln("import " + pkg + ".*;");
		}
		for (Class<?> clazz : classes) {
			if (!packages.contains(clazz.getPackage().getName())) {
				imports.appendln("import " + clazz.getCanonicalName() + ";");
			}
		}
		imports.sortLines();
		return imports.toString(0);
	}


	public void add(Class<?> clazz) {
		classes.add(clazz);
	}


	public void add(GPackage package2bImported) {
		packages.add(package2bImported);
	}

}
