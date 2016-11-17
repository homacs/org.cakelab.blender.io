package org.cakelab.blender.doc.extract.dnadocs;

import java.util.ListIterator;

import org.cakelab.jdoxml.api.*;
import org.cakelab.jdoxml.impl.dochandler.ParameterHandler;

public class JavaDocEncoder {

	private String ignoreFollowingTextStart;

	public String encode(IDocRoot doc) {
		StringBuffer buf = new StringBuffer();
		
		encode(buf, doc.contents());
		
		return buf.toString();
	}

	private void encode(StringBuffer buf, ListIterator<? extends IDoc> contents) {
		while (contents.hasNext()) {
			IDoc doc = contents.next();
			encode(buf, doc);
		}
	}

	private void encode(StringBuffer buf, IDoc doc) {
		switch(doc.kind()) {
		case Anchor:
			encode(buf, ((IDocAnchor)doc));
			break;
		case CodeLine:
			encode(buf, ((IDocCodeLine)doc));
			break;
		case Copy:
			encode(buf, ((IDocCopy)doc));
			break;
		case DotFile:
			encode(buf, ((IDocDotFile)doc));
			break;
		case EMail:
			encode(buf, ((IDocEMail)doc));
			break;
		case Entry:
			encode(buf, ((IDocEntry)doc));
			break;
		case Formula:
			encode(buf, ((IDocFormula)doc));
			break;
		case HRuler:
			encode(buf, ((IDocHRuler)doc));
			break;
		case Highlight:
			encode(buf, ((IDocHighlight)doc));
			break;
		case Image:
			encode(buf, ((IDocImage)doc));
			break;
		case IndexEntry:
			encode(buf, ((IDocIndexEntry)doc));
			break;
		case Internal:
			encode(buf, ((IDocInternal)doc));
			break;
		case ItemizedList:
			encode(buf, ((IDocItemizedList)doc));
			break;
		case LineBreak:
			encode(buf, ((IDocLineBreak)doc));
			break;
		case Link:
			encode(buf, ((IDocLink)doc));
			break;
		case ListItem:
			encode(buf, ((IDocListItem)doc));
			break;
		case MDash:
			encode(buf, ((IDocMDash)doc));
			break;
		case MarkupModifier:
			encode(buf, ((IDocMarkupModifier)doc));
			break;
		case NDash:
			encode(buf, ((IDocNDash)doc));
			break;
		case OrderedList:
			encode(buf, ((IDocOrderedList)doc));
			break;
		case Para:
			encode(buf, ((IDocPara)doc));
			break;
		case Parameter:
			encode(buf, ((IDocParameter)doc));
			break;
		case ParameterItem:
			encode(buf, ((IDocParameterItem)doc));
			break;
		case ParameterList:
			encode(buf, ((IDocParameterList)doc));
			break;
		case ProgramListing:
			encode(buf, ((IDocProgramListing)doc));
			break;
		case Ref:
			encode(buf, ((IDocRef)doc));
			break;
		case Root:
			encode(buf, ((IDocRoot)doc));
			break;
		case Row:
			encode(buf, ((IDocRow)doc));
			break;
		case Section:
			encode(buf, ((IDocSection)doc));
			break;
		case SimpleSect:
			encode(buf, ((IDocSimpleSect)doc));
			break;
		case XRefSect:
			encode(buf, ((IDocXRefSect)doc));
			break;
		case Symbol:
			encode(buf, ((IDocSymbol)doc));
			break;
		case Table:
			encode(buf, ((IDocTable)doc));
			break;
		case Text:
			encode(buf, ((IDocText)doc));
			break;
		case Title:
			encode(buf, ((IDocTitle)doc));
			break;
		case XRefTitle:
			encode(buf, ((IDocXRefTitle)doc));
			break;
		case TocItem:
			encode(buf, ((IDocTocItem)doc));
			break;
		case TocList:
			encode(buf, ((IDocTocList)doc));
			break;
		case ULink:
			encode(buf, ((IDocULink)doc));
			break;
		case VariableList:
			encode(buf, ((IDocVariableList)doc));
			break;
		case VariableListEntry:
			encode(buf, ((IDocVariableListEntry)doc));
			break;
		case Verbatim:
			encode(buf, ((IDocVerbatim)doc));
			break;
		case Invalid:
			throw new IllegalArgumentException("illegal element in documentation");
		default:
			break;
		}		
	}

	private void encode(StringBuffer buf, IDocVerbatim doc) {
		html(buf, doc.text(), "pre");
	}

	private void html(StringBuffer buf, String contents, String tag) {
		if (contents == null || contents.isEmpty()) return;
		buf.append("<").append(tag).append(">");
		buf.append(contents);
		buf.append("</").append(tag).append(">");
	}

	private void html(StringBuffer buf, ListIterator<? extends IDoc> contents, String tag) {
		buf.append("<").append(tag).append(">");
		encode(buf, contents);
		buf.append("</").append(tag).append(">");
	}


	private void encode(StringBuffer buf, IDocVariableListEntry doc) {
		ignore(buf, doc);
	}

	private void encode(StringBuffer buf, IDocVariableList doc) {
		ignore(buf, doc);
	}

	private void encode(StringBuffer buf, IDocULink doc) {
		ignore(buf, doc);
	}

	private void encode(StringBuffer buf, IDocTocList doc) {
		ignore(buf, doc);
	}

	private void encode(StringBuffer buf, IDocTocItem doc) {
		ignore(buf, doc);
	}

	private void encode(StringBuffer buf, IDocTitle doc) {
		ignore(buf, doc);
	}

	private void encode(StringBuffer buf, IDocXRefTitle doc) {
		ignore(buf, doc);
	}

	private void encode(StringBuffer buf, IDocText doc) {
		String text = doc.text();
		if (text == null || text.isEmpty()) return;
		
		if (ignoreFollowingTextStart != null && text.startsWith(ignoreFollowingTextStart)) {
			text = text.substring(ignoreFollowingTextStart.length());
		}
		buf.append(' ').append(text);
	}

	private void encode(StringBuffer buf, IDocTable doc) {
		html(buf, doc.rows(), "table");
	}

	private void encode(StringBuffer buf, IDocSymbol doc) {
		ignore(buf, doc);
	}

	private void encode(StringBuffer buf, IDocXRefSect doc) {
		switch (doc.type()) {
		case Deprecated:
			encode(buf,doc.description());
			buf.append("\n@deprecated\n");
			break;
		default:
			html(buf, "h2", doc.title().text());
			encode(buf,doc.description());
			break;
		}
	}

	private void encode(StringBuffer buf, IDocSimpleSect doc) {
		switch (doc.type()) {
		case Deprecated:
			encode(buf,doc.description());
			buf.append("\n@deprecated\n");
			break;
		default:
			html(buf, doc.type().toString(), "h2");
			encode(buf,doc.description());
			break;
		}
	}

	private void encode(StringBuffer buf, IDocSection doc) {
		encode(buf, doc.paragraphs());
	}

	private void encode(StringBuffer buf, IDocRow doc) {
		buf.append("<tr>");
		encode(buf, doc.entries());
		buf.append("</tr>");
	}

	private void encode(StringBuffer buf, IDocRoot doc) {
		encode(buf, doc.contents());
	}

	private void encode(StringBuffer buf, IDocRef doc) {
		buf.append("{@link ").append(doc.text()).append("}");
		ignoreFollowingTextStart = doc.text();
	}

	private void encode(StringBuffer buf, IDocProgramListing doc) {
		html(buf, doc.codeLines(), "code");
	}

	private void encode(StringBuffer buf, IDocParameterList doc) {
		encode(buf, doc.params());
	}

	private void encode(StringBuffer buf, IDocParameterItem doc) {
		ListIterator<ParameterHandler> paramNames = doc.paramNames();
		while (paramNames.hasNext()) {
			ParameterHandler param = paramNames.next();
			String name = param.name();
			buf.append("@param ").append(name).append(doc.description());
		}
	}

	private void encode(StringBuffer buf, IDocParameter doc) {
		ignore(buf, doc);
	}

	private void encode(StringBuffer buf, IDocPara doc) {
		if (doc == null) return;
		html(buf, doc.contents(), "p");
	}

	private void encode(StringBuffer buf, IDocOrderedList doc) {
		html(buf, doc.elements(), "ol");
	}

	private void encode(StringBuffer buf, IDocNDash doc) {
		buf.append("<ndash/>");
	}

	private void encode(StringBuffer buf, IDocMarkupModifier doc) {
		IDocMarkup.Markup markup = IDocMarkup.Markup.enumFor(doc.markup());
		
		String html = null;
		switch(markup) {
		case Bold:
			html = "b";
			break;
		case Center:
			html="center";
			break;
		case ComputerOutput:
			html = "code";
			break;
		case Emphasis:
			html = "em";
			break;
		case Heading:
			html = "h1";
			break;
		case Normal:
			html = "";
			break;
		case Preformatted:
			html = "pre";
			break;
		case SmallFont:
			html = "small";
			break;
		case Subscript:
			html = "sub";
			break;
		case Superscript:
			html = "sup";
			break;
		default:
			break;
		
		}
		if (html != null) {
			buf.append("<");
			if (!doc.enabled()) {
				buf.append("/");
			}
			buf.append(html);
			buf.append(">");
		}		
	}

	private void encode(StringBuffer buf, IDocMDash doc) {
		buf.append("<mdash/>");
	}

	private void encode(StringBuffer buf, IDocListItem doc) {
		html(buf, doc.contents(), "li");
	}

	private void encode(StringBuffer buf, IDocLink doc) {
		ignore(buf, doc);
	}

	private void encode(StringBuffer buf, IDocLineBreak doc) {
		buf.append("<br/>");
	}

	private void encode(StringBuffer buf, IDocItemizedList doc) {
		html(buf, doc.elements(), "ul");
	}

	private void encode(StringBuffer buf, IDocInternal doc) {
		ignore(buf, doc);
	}

	private void encode(StringBuffer buf, IDocIndexEntry doc) {
		ignore(buf, doc);
	}

	private void encode(StringBuffer buf, IDocImage doc) {
		buf.append("<img ").append("src=\"").append(doc.name()).append("\"/>");
	}

	private void encode(StringBuffer buf, IDocHighlight doc) {
		switch(doc.highlightKind()) {
		case CharLiteral:
			break;
		case Comment:
			break;
		case Invalid:
			break;
		case Keyword:
			break;
		case KeywordFlow:
			break;
		case KeywordType:
			break;
		case Preprocessor:
			break;
		case StringLiteral:
			break;
		default:
			break;
		
		}
		encode(buf, doc.codeElements());
	}

	private void encode(StringBuffer buf, IDocHRuler doc) {
		buf.append("<hr/>");
	}

	private void encode(StringBuffer buf, IDocFormula doc) {
		buf.append(doc.text());
	}

	private void encode(StringBuffer buf, IDocEntry doc) {
		html(buf, doc.contents(), "td");
	}

	private void encode(StringBuffer buf, IDocEMail doc) {
		buf.append(doc.address());
	}

	private void encode(StringBuffer buf, IDocDotFile doc) {
		ignore(buf, doc);
	}

	private void encode(StringBuffer buf, IDocCopy doc) {
		encode(buf, doc.contents());
	}

	private void encode(StringBuffer buf, IDocCodeLine doc) {
		encode(buf, doc.codeElements());
		buf.append("\n");
	}

	private void encode(StringBuffer buf, IDocAnchor doc) {
		ignore(buf, doc);
	}

	private void ignore(StringBuffer buf, IDoc doc) {
		System.out.println("ignored: " + doc.kind());
	}


}
