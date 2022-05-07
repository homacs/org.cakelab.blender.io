package org.cakelab.blender.generator.utils;

import org.cakelab.blender.generator.Mangeling;
import org.cakelab.blender.metac.CField;
import org.cakelab.blender.metac.CMetaModel;
import org.cakelab.blender.metac.CStruct;
import org.cakelab.blender.metac.CType;
import org.cakelab.blender.typemap.Renaming;

public class JavaDocPostprocessor {
	private final CMetaModel model;


	public JavaDocPostprocessor(CMetaModel model) {
		this.model = model;
	}
	

	public String postprocess(String text, String context) {
		return repairDanglingLinks(text, context);
	}

	public String repairDanglingLinks(String text, String context) {
		String phrase = "{@link ";
		int i = text.indexOf(phrase);
		if (i != -1) {
			int last = 0; // end of last replacement in text
			StringBuffer b = new StringBuffer();
			boolean modified = false;
			for (; i != -1 && i < text.length(); i = text.indexOf(phrase, i)) {
				int begin = i;
				int end = text.indexOf('}', i);
				if (end == -1) {
					// missing closing bracket -> just remove the phrase
					return text.replace(phrase, "");
				} else {
					String reference = text.substring(i + phrase.length(), end);
					String resolved = getResolvedReference(reference, context);
					if (resolved == null) {
						modified = true;
						// remove {@link but keep reference
						b.append(text.substring(last, begin));
						b.append(reference);
						last = end + 1;
					} else {
						modified = true;
						// keep the @link and correct its reference
						b.append(text.substring(last, begin + phrase.length()));
						b.append(resolved);
						last = end;
					}
				}
				i = end + 1;
			}
			
			if (modified) {
				b.append(text.substring(last, text.length()));
				return b.toString();
			}
		}
		return text;
	}


	private String getResolvedReference(String reference, String context) {
		assert(reference != null);
		assert(reference.equals(reference.trim()));
		assert(context != null && !context.isEmpty());
		
		if (reference.startsWith("DNA_") && reference.endsWith(".h")) {
			// reference on C header file
			return null;
		}

		
		// attempt to resolve the given reference
		int dot = reference.lastIndexOf('.');
		if (dot == -1) dot = reference.indexOf('#');
		String struct = reference;
		String member = null;
		if (dot != -1) {
			// reference on a field
			struct = reference.substring(0, dot);
			if (struct.isEmpty()) // local reference
				struct = null;
			
			member = reference.substring(dot+1, reference.length());
			if (member.length() == 0)
				member = null;
		}

		
		String structName = (struct != null) ? struct : context;
		CType type = model.getType(structName);
		if (type == null) {
			type = model.getType(Renaming.mapClass2Struct(structName));
		}
		if (type == null && struct != null && member == null) {
			// test if given name is a member and not a struct
			CType ctype = model.getType(context);
			if (ctype != null && ctype.getKind() == CType.CKind.TYPE_STRUCT) {
				CStruct cstruct = (CStruct)ctype;
				CField field = cstruct.getField(struct);
				if (field != null) {
					type = cstruct;
					structName = context;
					member = struct;
					struct = null;
				}
			}
		}
		
		// still no result???
		if (type == null || type.getKind() != CType.CKind.TYPE_STRUCT) {
			if (isLibraryClass(structName)) {
				// added by us - supposed to be correct
				return reference;
			} else {
				// actually unknown
				// FIXME: debug output
				System.err.println("[" + context + "] unkown @link: " + reference);
				return null;
			}
		}
		
		CStruct cstruct = (CStruct) type;
		if (member != null) {
			CField field = cstruct.getField(member);
			if (field != null) {
				return ((struct != null) ? struct :  "") + '#' + Mangeling.mangle(member);
			}
			else 
				return null;
		}
		
		return struct;
	}


	private boolean isLibraryClass(String structName) {
		final String[] packages = new String[] {
				"org.cakelab.blender.io",
				"org.cakelab.blender.io.block"
		};
		if (!isFullyQualifiedName(structName)) {
			for (String p : packages) {
				try {
					Class.forName(p + "." + structName);
					// class exists
					return true;
				} catch (ClassNotFoundException e) {
				}
			}
		}
		// class not found in class path
		return false;
	}


	private boolean isFullyQualifiedName(String structName) {
		return structName.contains(".");
	}



}
