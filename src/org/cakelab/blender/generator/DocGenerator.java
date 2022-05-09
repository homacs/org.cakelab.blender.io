package org.cakelab.blender.generator;

import java.io.File;
import java.io.IOException;

import org.cakelab.blender.doc.Documentation;
import org.cakelab.blender.doc.DocumentationProvider;
import org.cakelab.blender.generator.utils.JavaDocPostprocessor;
import org.cakelab.blender.metac.CMetaModel;
import org.cakelab.json.JSONException;

/**
 * DocGenerator is basically a hub to multiple documentations 
 * on the same subject. It searches for documentation in 
 * all sources and combines it into a single documentation.
 * 
 * @author homac
 *
 */
public class DocGenerator implements DocumentationProvider {
	private static final String DOCSRC_JAVA_BLEND = "Java .Blend";
	
	private Documentation[] docs;
	private JavaDocPostprocessor postprocessor;
	
	public DocGenerator(File[] docfiles, CMetaModel model, boolean debug) throws IOException, JSONException {
		postprocessor = new JavaDocPostprocessor(model, debug);
		docs = new Documentation[docfiles.length];
		int i = 0;
		for (File f : docfiles) {
			docs[i++] = new Documentation(f, debug);
		}
	}

	@Override
	public String getStructDoc(String struct) {
		StringBuffer lines = new StringBuffer();
		for (Documentation doc : docs) {
			String docentry = doc.getStructDoc(struct);
			appendDocEntry(doc, lines, struct, docentry);
		}
		return lines.toString();
	}

	@Override
	public String getFieldDoc(String struct, String field) {
		StringBuffer lines = new StringBuffer();
		
		for (Documentation doc : docs) {
			String docentry = doc.getFieldDoc(struct, field);
			appendDocEntry(doc, lines, struct, docentry);
		}
		return lines.toString();
	}

	private void appendDocEntry(Documentation doc, StringBuffer lines, String struct, String docentry) {
		if (docentry != null && docentry.trim().length() > 0) {
			String source = doc.getSource();
			if (!source.equals(DOCSRC_JAVA_BLEND))
				docentry = postprocessor.postprocess(docentry, struct);
			lines.append("\n<h4>" + doc.getSource() + "</h4>").append("\n");
			lines.append(docentry);
		}
	}

}
