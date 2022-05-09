package org.cakelab.blender.doc;

import java.io.File;
import java.util.HashSet;
import java.util.Set;

import org.cakelab.json.JSONObject;

public class StructDoc extends JSONObject {
	private static final long serialVersionUID = 1L;
	final transient Set<File> origins = new HashSet<File>();
	
	public StructDoc(JSONObject doc, File origin) {
		this(origin);
		putAll(doc);
	}
	public StructDoc(File origin) {
		this.origins.add(origin);
	}
	
	@Deprecated
	public StructDoc() {
	}

	
	
	public boolean containsOrigin(File origin) {
		return origins.contains(origin);
	}
	
	public boolean sameOrigins(StructDoc base) {
		for (File origin : base.origins) {
			if (!containsOrigin(origin))
				return false;
		}
		return true;
	}

	/** only inherit values which are not yet set */
	public boolean inherit(StructDoc base) {
		if (sameOrigins(base)) 
			return false;
		boolean modified = addFields(base.getObject("fields"));
		String doc = getString("doc");
		if (doc == null) {
			put("doc", doc);
			modified = true;
		}
		if (modified) origins.addAll(base.origins);
		return modified;
	}
	
	/** override all values from existing values of overrides */
	public boolean override(StructDoc overrides) {
		if (sameOrigins(overrides)) 
			return false;
		boolean modified = false;
		String doc = overrides.getString("doc");
		if (doc != null) {
			put("doc", doc);
			modified = true;
		}
		modified = addFields(overrides.getObject("fields")) || modified;
		
		if (modified) origins.addAll(overrides.origins);
		return modified;
	}

	/** add all non-existing fields */
	public boolean addFields(JSONObject basefields) {
		if (basefields == null) 
			return false;
		
		boolean modified = false;
		JSONObject fields = getObject("fields");
		if (fields == null) {
			put("fields", basefields);
			modified = true;
		} else {
			for (Entry<String, Object> entry : basefields.entrySet()) {
				if (!fields.containsKey(entry.getKey())) {
					fields.put(entry.getKey(), entry.getValue());
					modified = true;
				}
			}
		}
		return modified;
	}
	
	/** add non-existing and replace existing fields
	 * @return true, if modified */
	public boolean overrideFields(JSONObject overriding) {
		if (overriding == null) 
			return false;
		
		JSONObject fields = getObject("fields");
		if (fields == null) {
			put("fields", overriding);
		} else {
			fields.putAll(overriding);
		}
		return true;
	}
	
}
