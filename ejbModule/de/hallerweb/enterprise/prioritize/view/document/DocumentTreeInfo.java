package de.hallerweb.enterprise.prioritize.view.document;

import de.hallerweb.enterprise.prioritize.model.document.Document;
import de.hallerweb.enterprise.prioritize.model.document.DocumentInfo;

public class DocumentTreeInfo {

	private boolean leaf;
	private String name;
	private DocumentInfo documentInfo;
	private String documentGroupId;
	boolean documentGroupNode;

	public boolean isDocumentGroupNode() {
		return documentGroupNode;
	}

	public String getDocumentGroupId() {
		return documentGroupId;
	}

	public String getName() {
		return this.name;
	}

	public DocumentInfo getDocumentInfo() {
		return this.documentInfo;
	}

	public boolean isLeaf() {
		return this.leaf;
	}

	public DocumentTreeInfo(String name, boolean isLeaf, boolean isDocumentGroupNode, String documentGroupId,DocumentInfo info) {
		this.leaf = isLeaf;
		this.name = name;
		this.documentInfo = info;
		this.documentGroupId = documentGroupId;
		this.documentGroupNode = isDocumentGroupNode;
//		if (info == null) {
//			info = new DocumentInfo();
//			info.setCurrentDocument(new Document());
//		}
	}

}