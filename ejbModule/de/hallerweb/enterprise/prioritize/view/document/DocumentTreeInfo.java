/*
 * Copyright 2015-2020 Peter Michael Haller and contributors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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