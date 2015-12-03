package org.cakelab.blender.generator.code;

import java.util.ArrayList;

import org.cakelab.blender.doc.DocumentationProvider;
import org.cakelab.blender.generator.ModelGenerator;

public class ClassGenerator extends CodeGenerator {



	protected ModelGenerator modelgen;
	protected GPackage gpackage;
	protected ImportSectionGenerator imports;
	protected ArrayList<GField> fields;
	protected ArrayList<String> methods;
	protected DocumentationProvider docs;
	
	public ClassGenerator(ModelGenerator modelgen, GPackage gpackage, DocumentationProvider docs2) {
		super(0);
		this.modelgen = modelgen;
		this.gpackage = gpackage;
		this.docs = docs2;
		this.imports = new ImportSectionGenerator();
		this.fields = new ArrayList<GField>();
		this.methods = new ArrayList<String>();
	}


	public void addImport(Class<?> clazz) {
		imports.add(clazz);
	}

	public void addImport(GPackage package2bImported) {
		imports.add(package2bImported);
	}
	
	public void addMethod(String method) {
		methods.add(method);
	}

	public GField addField(String modifiers, String type, String name,
			String comment) {
		GField field = new GField(modifiers, type, name, comment);
		fields.add(field);
		return field;
	}


	public GField addField(String modifiers, String type, String name) {
		return addField(modifiers, type, name, null);
	}

	@Override
	public void reset() {
		imports.reset();
		fields.clear();
		methods.clear();
	}


	public DocumentationProvider getDocs() {
		return docs;
	}

}
