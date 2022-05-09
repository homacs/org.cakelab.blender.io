package org.cakelab.blender.doc;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.cakelab.blender.io.FileHeader.Version;
import org.cakelab.blender.io.FileVersionInfo;
import org.cakelab.blender.io.dna.DNAField;
import org.cakelab.blender.io.dna.DNAStruct;
import org.cakelab.json.JSONArray;
import org.cakelab.json.JSONException;
import org.cakelab.json.JSONObject;
import org.cakelab.json.JSONDefaults;
import org.cakelab.json.parser.JSONParser;

/**
 * <p>
 * This class is used to manage and retrieve documentation 
 * for generated classes.
 * </p><p>
 * Documentation is mainly received from blender source and
 * can be manually extended by additional information. Documentation
 * is stored in external documentation files (see "resources/dnadoc").
 * </p><p>
 * This class parses the documentation files and establishes lookup 
 * tables for structs and fields of structs. During the generation
 * of the data model, the class and method generators use this 
 * class to lookup available documentation for classes and its members.
 * </p>
 * 
 * 
 * 
 * 
 * 
 * @author homac
 *
 */
public class Documentation implements DocumentationProvider {
	
	protected Map<String, StructDoc> structdocs;
	protected String[] authors;

	protected String system;
	protected String module;
	protected String version;
	protected String source;
	protected File includePath;
	
	transient final File origin;
	
	private boolean debug;
	
	public Documentation(File docfile, boolean debug) throws IOException, JSONException{
		this.debug = debug;
		JSONParser parser = JSONDefaults.createDefaultParser();
		JSONObject docjson = parser.parse(new FileInputStream(docfile));
		origin = docfile;
		includePath = docfile.getCanonicalFile().getParentFile();
		system = docjson.getString("system");
		module = docjson.getString("module");
		version = docjson.getString("version");
		source = docjson.getString("source");
		JSONArray authors = docjson.getArray("authors");
		if (authors != null) {
			this.authors = authors.toArray(new String[0]);
		}
		
		structdocs = new HashMap<>();
		
		/*
		 * Inherits documentation of its parent.
		 * Inherited struct documentation may be overridden by this documentation.
		 */
		String filename = docjson.getString("inherits");
		if (filename != null) {
			try {
				Documentation parent = new Documentation(new File(includePath, filename), debug);
				structdocs = parent.structdocs;
			} catch (IOException | JSONException e) {
				warn("couldn't read base documentation'" + filename + "' inherited by '" + docfile.getName() + "'.");
				warn("reason: " + e.getMessage());
			}
		}
		
		JSONArray includeFiles = docjson.getArray("includes");
		if (includeFiles != null) {
			for (int i = 0; i < includeFiles.size(); i++) {
				filename = includeFiles.getString(i);
				try {
					File includeFile = new File(includePath, filename);
					Documentation include = new Documentation(includeFile, debug);
					includeStructs(include.structdocs, includeFile);
				} catch (IOException | JSONException e) {
					warn("couldn't read doc file '" + filename + "' included by '" + docfile.getName() + "'.");
					warn("reason: " + e.getMessage());
				}
			}
		}
		
		JSONObject mydocs = docjson.getObject("structs");
		if (mydocs != null) {
			addNewStructs(mydocs, docfile);
		}
		
		resolveStructInheritance();
	}

	private void addNewStructs(JSONObject extensions, File origin) {
		for (Entry<String, Object> structDocEntry : extensions.entrySet()) {
			String structName = structDocEntry.getKey();
			StructDoc newStructDoc = new StructDoc((JSONObject) structDocEntry.getValue(), origin);
			StructDoc existingStructDoc = structdocs.get(structName);
			
			if (existingStructDoc != null) {
				boolean modified = existingStructDoc.override(newStructDoc);
				if (modified) {
					warn("new struct overrides exsting: " + structName);
				} else {
					warn("duplicate definition of struct: " + structName);
				}
			} else {
				structdocs.put(structName, newStructDoc);
			}
			
		}
	}

	private void includeStructs(Map<String, StructDoc> includes, File includeFile) throws IOException {
		for (Entry<String, StructDoc> include : includes.entrySet()) {
			String structname = include.getKey();
			StructDoc includedStructDoc = include.getValue();
			StructDoc existingStructDoc = structdocs.get(structname);
			if (existingStructDoc != null) {
				boolean modified = existingStructDoc.override(includedStructDoc);
				if (modified) {
					warn("include overrides '" + structname + "': " + includeFile.getName());
				} else {
					debug("include already merged: " + includeFile.getName());
				}
			} else {
				structdocs.put(structname, includedStructDoc);
			}
			
		}
	}

	/**
	 * Creates an empty documentation.
	 * Use it for documentation construction only such as in converters.
	 */
	public Documentation () {
		structdocs = null;
		origin = null;
	}
	

	public String[] getAuthors() {
		return authors;
	}

	public String getSystem() {
		return system;
	}

	public String getModule() {
		return module;
	}

	public String getVersion() {
		return version;
	}

	public String getStructDoc(DNAStruct struct) {
		return getStructDoc(struct.getType().getName());
	}
	
	public String getFieldDoc(DNAStruct struct, DNAField field) {
		return getFieldDoc(struct.getType().getName(), field.getName());
	}
	
	public String getStructDoc(String structname) {
		String result = null;
		StructDoc structdoc = structdocs.get(structname);
		if (structdoc != null) {
			result = structdoc.getString("doc");
		}
		return result;
	}
	
	public String getFieldDoc(String structname, String fieldname) {
		String result = null;
		if (structdocs == null)  return result;
		
		StructDoc structdoc = structdocs.get(structname);
		if (structdoc != null) {
			JSONObject fieldsdoc = structdoc.getObject("fields");
			if (fieldsdoc != null) {
				result = fieldsdoc.getString(fieldname);
			}
		}
	
		return result;
	}
	

	private void resolveStructInheritance() {
		for (Entry<String, StructDoc> doc : structdocs.entrySet()) {
			resolveStructInheritance(doc.getKey());
		}
	}
	
	private StructDoc resolveStructInheritance(String structname) {
		StructDoc structdoc =  structdocs.get(structname);
		if (structdoc != null) {
			JSONArray inherits = structdoc.getArray("inherits");
			if (inherits != null) {
				for (String base : inherits.toArray(new String[0])) {
					StructDoc basestructdoc = resolveStructInheritance(base);
					if (basestructdoc != null) {
						structdoc.inherit(basestructdoc);
					} else {
						warn("unresolved inheritance: " + base + " <-- " + structname);
					}
				}
			}
		}
		return structdoc;
	}

	public String getPath() {
		return includePath.toString();
	}

	public void write(File out) throws UnsupportedEncodingException, IOException {
		JSONObject root = new JSONObject();
		
		root.put("system", system);
		root.put("module", module);
		root.put("version", version);
		root.put("source", source);
		JSONObject structsjson = new JSONObject();
		structsjson.putAll(structdocs);
		root.put("structs", structsjson);
		try (FileOutputStream fout = new FileOutputStream(out)) {
			fout.write(root.toString().getBytes("UTF-8"));
		}
	}

	public String getSource() {
		return source;
	}

	public static File getDocFolder(File docfolder, FileVersionInfo versionInfo) {
		File[] folders = docfolder.listFiles(new FileFilter() {
			@Override
			public boolean accept(File pathname) {
				return pathname.isDirectory();
			}
		});
		File subfolder = null;
		if (folders != null && folders.length > 0) {
			int maxVersion = versionInfo.getVersion().getCode();
			int minVersion = versionInfo.getMinversion().getCode();
			int subfolderVer = -1;
			for (File folder : folders) {
				try {
					int version = new Version(folder.getName()).getCode();
					if (version <= maxVersion && version >= minVersion && version > subfolderVer) {
						subfolder = folder;
						subfolderVer = version;
					}
				} catch (NumberFormatException e) {
					// not a version directory --> ignore
				}
			}
		}
		return subfolder;
	}

	private void warn(String message) {
		if (debug) {
			System.err.println("docgen [debug]: " + message);
		}
	}

	private void debug(String message) {
		if (debug) {
			System.err.println("docgen [debug]: " + message);
		}
	}



}
