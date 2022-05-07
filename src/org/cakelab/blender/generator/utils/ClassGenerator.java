package org.cakelab.blender.generator.utils;

import java.util.ArrayList;

import org.cakelab.blender.doc.DocumentationProvider;
import org.cakelab.blender.generator.ModelGenerator;

public abstract class ClassGenerator extends CodeGenerator {



	protected final ModelGenerator modelgen;
	protected final GPackage gpackage;
	protected final ImportSectionGenerator imports;
	protected final ArrayList<GField> constFields;
	protected final ArrayList<GField> fields;
	protected final ArrayList<GMethod> methods;
	protected final DocumentationProvider docs;
	
	public ClassGenerator(ModelGenerator modelgen, GPackage gpackage, DocumentationProvider docs2) {
		super(0);
		this.modelgen = modelgen;
		this.gpackage = gpackage;
		this.docs = docs2;
		this.imports = new ImportSectionGenerator();
		this.fields = new ArrayList<>();
		this.constFields = new ArrayList<>();
		this.methods = new ArrayList<>();
	}


	public void addImport(Class<?> clazz) {
		imports.add(clazz);
	}

	public void addImport(GPackage package2bImported) {
		imports.add(package2bImported);
	}
	
	public void addMethod(GMethod method) {
		methods.add(method);
	}

	public GField addField(String modifiers, String type, String name,
			GComment comment) {
		GField field = new GField(modifiers, type, name, null, comment);
		fields.add(field);
		return field;
	}


	public GField addField(String modifiers, String type, String name) {
		return addField(modifiers, type, name, GComment.EMPTY);
	}

	public GField addConstField(String modifiers, String type, String name,
			String initialiser, GComment javadoc) {
		GField field = new GField(modifiers, type, name, initialiser, javadoc);
		constFields.add(field);
		return field;
	}


	public GField addConstField(String modifiers, String type, String name, String initialiser) {
		return addConstField(modifiers, type, name, initialiser, null);
	}

	@Override
	public void reset() {
		imports.reset();
		fields.clear();
		constFields.clear();
		methods.clear();
	}


	public DocumentationProvider getDocs() {
		return docs;
	}

	
	public abstract String getClassName();

}

