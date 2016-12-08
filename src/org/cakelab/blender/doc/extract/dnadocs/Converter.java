package org.cakelab.blender.doc.extract.dnadocs;

import java.io.File;
import java.io.IOException;
import java.util.ListIterator;

import org.cakelab.blender.doc.Documentation;
import org.cakelab.jdoxml.Factory;
import org.cakelab.jdoxml.api.ICompound;
import org.cakelab.jdoxml.api.IDocRoot;
import org.cakelab.jdoxml.api.IDocumentedElement;
import org.cakelab.jdoxml.api.IDoxygen;
import org.cakelab.jdoxml.api.IMember;
import org.cakelab.jdoxml.api.ISection;
import org.cakelab.jdoxml.api.IStruct;
import org.cakelab.jdoxml.api.IUserDefined;
import org.cakelab.json.JSONObject;
import org.xml.sax.SAXException;

/**
 * This class takes XLM output from doxygen and converts it into Java .Blend json doc format.
 * @author homac
 *
 */
public class Converter extends Documentation {

	// XXX: losing notes during conversion!
	private File input;
	private File out;
	private IDoxygen dox;
	private JavaDocEncoder jdocEncoder = new JavaDocEncoder();

	public Converter(File input, String version, File output) {
		this.input = input;
		this.out = output;
		
		
		
		this.includePath = null;
		this.system = "Blender";
		this.module = "DNA";
		this.source = "Blender Source Code";
		this.version = version;

		this.structdocs = new JSONObject();

		dox = Factory.createObjectModel();


	}

	public void run() throws SAXException, IOException {
		dox.readXMLDir(input);
		
		ListIterator<ICompound> cli = dox.compounds();
		while (cli.hasNext()) {
			ICompound comp = cli.next();
			switch(comp.kind()) {
			case Class:
			case Interface:
			case Struct:
				addStructDocs((IStruct)comp);
				break;
			default:
				break;
			
			}
		}
		
		super.write(out);

		System.out.println("finished.");
	}

	private void addStructDocs(IStruct compound) {
		boolean addStructDocs = false;
		boolean hasDocs = false;
		
		System.out.println("processing struct " + compound.name());
		
		JSONObject struct = (JSONObject) structdocs.get(compound.name());
		if (struct == null) {
			struct = new JSONObject();
			addStructDocs = true;
		}
		String doc = getDoc(compound);
		if (!doc.isEmpty()) {
			doc = stripEmbeddedComments(doc);
			struct.put("doc", doc);
			hasDocs = true;
		}

		ListIterator<IUserDefined> sli = compound.sections();
		while (sli.hasNext()) {
			ISection section = sli.next();
			ListIterator<IMember> mli = section.members();
			while (mli.hasNext()) {
				IMember member = mli.next();
				switch(member.kind()) {
				case Function:
				case Prototype:
				case Signal:
				case Slot:
				case DCOP:
					// addMethodDoc(struct, member);
					break;
				case Variable:
				case Property:
					hasDocs |= addFieldDoc(struct, member);
					break;
				default:
					break;
				
				}
			}
		}
		
		if (addStructDocs && hasDocs) {
			structdocs.put(compound.name(), struct);
		}
	}

	/** This is a hack to get rid of comments embedded in other comments which end with "* /"
	 * since this would interfere with the comment we will generate later.
	 * @param doc
	 * @return stripped comment
	 */
	private String stripEmbeddedComments(String doc) {
		if (doc.contains("/*")) {
			doc = doc.replaceAll("/\\*(\\*[<!]?)?", "//");
			doc = doc.replaceAll("\\*/", "");
		}
		return doc;
	}

	private boolean addFieldDoc(JSONObject struct, IMember member) {
		boolean addDocs = false;
		JSONObject fieldsdoc = (JSONObject) struct.get("fields");
		
		if (fieldsdoc == null) {
			fieldsdoc = new JSONObject();
			addDocs = true;
		}
		String doc = getDoc(member);
		if (!doc.isEmpty()) {
			doc = stripEmbeddedComments(doc);

			fieldsdoc.put(member.name(), doc);
			if (addDocs) {
				struct.put("fields", fieldsdoc);
			}
		}
		return !doc.isEmpty();
	}

	private String getDoc(IDocumentedElement compound) {
		String allDocs = "";
		IDocRoot doc = compound.briefDescription();
		if (doc != null) allDocs += toJavaDoc(doc);

		doc = compound.detailedDescription();
		if (doc != null) allDocs += toJavaDoc(doc);
		
		return allDocs.trim();
	}

	private String toJavaDoc(IDocRoot doc) {
		return jdocEncoder.encode(doc);
	}

}
