package de.hallerweb.enterprise.prioritize.view.document;

import de.hallerweb.enterprise.prioritize.model.document.Document;
import de.hallerweb.enterprise.prioritize.model.document.DocumentInfo;

public class DocumentTreeInfo {

	private boolean leaf;
	private String name;
	private DocumentInfo documentInfo;

	public String getName() {
		return this.name;
	}

	public DocumentInfo getDocumentInfo() {
		return this.documentInfo;
	}

	public boolean isLeaf() {
		return this.leaf;
	}

	public DocumentTreeInfo(String name, boolean isLeaf, DocumentInfo info) {
		this.leaf = isLeaf;
		this.name = name;
		this.documentInfo = info;
//		if (info == null) {
//			info = new DocumentInfo();
//			info.setCurrentDocument(new Document());
//		}
	}

}