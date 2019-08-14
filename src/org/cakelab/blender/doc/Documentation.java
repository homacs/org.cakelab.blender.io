package org.cakelab.blender.doc;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Map.Entry;

import org.cakelab.blender.io.FileHeader.Version;
import org.cakelab.blender.io.FileVersionInfo;
import org.cakelab.blender.io.dna.DNAField;
import org.cakelab.blender.io.dna.DNAStruct;
import org.cakelab.json.JSONArray;
import org.cakelab.json.JSONException;
import org.cakelab.json.JSONObject;
import org.cakelab.json.codec.Parser;

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
	
	protected JSONObject structdocs;
	protected String[] authors;

	protected String system;
	protected String module;
	protected String version;
	protected String source;
	protected File includePath;
	private boolean debug;
	
	public Documentation(File docfile, boolean debug) throws IOException, JSONException{
		this.debug = debug;
		JSONObject docjson = new Parser(new FileInputStream(docfile)).parse();
		
		includePath = docfile.getCanonicalFile().getParentFile();
		system = docjson.getString("system");
		module = docjson.getString("module");
		version = docjson.getString("version");
		source = docjson.getString("source");
		JSONArray authors = (JSONArray) docjson.get("authors");
		if (authors != null) {
			this.authors = authors.toArray(new String[0]);
		}
		
		structdocs = new JSONObject();
		String filename = docjson.getString("inherits");
		if (filename != null) {
			try {
				structdocs = new Documentation(new File(includePath, filename), debug).structdocs;
			} catch (IOException | JSONException e) {
				warn("couldn't read base documentation'" + filename + "' inherited by '" + docfile.getName() + "'.");
				warn("reason: " + e.getMessage());
			}
		}
		
		JSONArray includeFiles = (JSONArray) docjson.get("includes");
		if (includeFiles != null) {
			for (int i = 0; i < includeFiles.size(); i++) {
				filename = (String)includeFiles.get(i);
				try {
					Documentation include = new Documentation(new File(includePath, filename), debug);
					includeStructs(include.structdocs, filename);
				} catch (IOException | JSONException e) {
					warn("couldn't read doc file '" + filename + "' included by '" + docfile.getName() + "'.");
					warn("reason: " + e.getMessage());
				}
			}
		}
		
		JSONObject mydocs = (JSONObject) docjson.get("structs");
		if (mydocs != null) {
			extendStructs(mydocs, docfile.getName());
		}
		
		resolveInheritance();
	}

	private void extendStructs(JSONObject extensions, String source) {
		for (Entry<String, Object> structdocEntry : extensions.entrySet()) {
			String structname = structdocEntry.getKey();
			JSONObject structdoc = (JSONObject) structdocs.get(structname);
			
			if (structdoc != null) {
				overrideStructDoc(structdoc, (JSONObject)structdocEntry.getValue());
			} else {
				structdocs.put(structname, structdocEntry.getValue());
			}
			
		}
	}

	private void overrideStructDoc(JSONObject structdoc, JSONObject overrides) {
		String doc = overrides.getString("doc");
		if (doc != null) {
			structdoc.put("doc", doc);
		}
		JSONObject fields = (JSONObject) structdoc.get("fields");
		if (fields == null) {
			structdoc.put("fields", overrides.get("fields"));
		} else {
			JSONObject overridingFields = (JSONObject) overrides.get("fields");
			fields.putAll(overridingFields);
		}
		
	}

	private void includeStructs(JSONObject includes, String source) throws IOException {
		for (Entry<String, Object> structdoc : includes.entrySet()) {
			String structname = structdoc.getKey();
			if (structdocs.containsKey(structname)) {
				warn("include '" + source + "' is overriding existing entry for '" + structname + "' found in include.");
				JSONObject ostructdoc = (JSONObject) structdocs.get(structname);
				overrideStructDoc(ostructdoc, (JSONObject) structdoc.getValue());
			} else {
				structdocs.put(structname, structdoc.getValue());
			}
			
		}
	}

	/**
	 * creates an empty documentation.
	 */
	public Documentation () {
		structdocs = null;
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
		JSONObject structdoc = (JSONObject) structdocs.get(structname);
		if (structdoc != null) {
			result = structdoc.getString("doc");
		}
		return result;
	}
	
	public String getFieldDoc(String structname, String fieldname) {
		String result = null;
		if (structdocs == null)  return result;
		
		JSONObject structdoc = (JSONObject) structdocs.get(structname);
		if (structdoc != null) {
			JSONObject fieldsdoc = (JSONObject) structdoc.get("fields");
			if (fieldsdoc != null) {
				result = fieldsdoc.getString(fieldname);
			}
		}
	
		return result;
	}
	

	private void resolveInheritance() {
		for (Entry<String, Object> doc : structdocs.entrySet()) {
			resolveInheritance(doc.getKey());

		}
	}
	
	private JSONObject resolveInheritance(String structname) {
		JSONObject structdoc = (JSONObject) structdocs.get(structname);
		if (structdoc != null) {
			JSONArray inherits = (JSONArray) structdoc.get("inherits");
			if (inherits != null) {
				for (String base : inherits.toArray(new String[0])) {
					JSONObject basestructdoc = resolveInheritance(base);
					if (basestructdoc != null) {
						inheritFields(structdoc, basestructdoc);
					} else {
						warn("could not resolve inheritance of " + base + " <-- " + structname + ".");
						warn("reason: base class " + base + " is not defined");
					}
				}
			}
		}
		return structdoc;
	}

	private void warn(String string) {
		if (debug) {
			System.err.println("docgen [debug]: " + string);
		}
	}

	private void inheritFields(JSONObject structdoc, JSONObject base) {
		JSONObject basefields = (JSONObject) base.get("fields");
		if (basefields != null) {
			JSONObject myfields = (JSONObject) structdoc.get("fields");
			if (myfields == null) {
				structdoc.put("fields", basefields);
			} else {
				for (Entry<String, Object> entry : basefields.entrySet()) {
					if (!myfields.containsKey(entry.getKey())) {
						myfields.put(entry.getKey(), entry.getValue());
					}
				}
			}
		}
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
		root.put("structs", structdocs);
		FileOutputStream fout = new FileOutputStream(out);
		fout.write(root.toString().getBytes("UTF-8"));
		fout.close();
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
			int minVersion = versionInfo.getMinversion();
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



}
