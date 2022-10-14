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
package de.hallerweb.enterprise.prioritize.model.document;

import com.fasterxml.jackson.annotation.JsonBackReference;
import de.hallerweb.enterprise.prioritize.model.Department;
import de.hallerweb.enterprise.prioritize.model.security.PAuthorizedObject;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Set;

/**
 * JPA entity to represent a Directory of documents within a department.
 * 
 * <p>
 * Copyright: (c) 2014
 * </p>
 * <p>
 * Peter Haller
 * </p>
 * 
 * @author peter
 */
@Entity
@NamedQueries({ @NamedQuery(name = "findDocumentGroupById", query = "select dg FROM DocumentGroup dg WHERE dg.id = :groupId"),
		@NamedQuery(name = "findDocumentGroupsForDepartment", query = "select dg FROM DocumentGroup dg WHERE dg.department.id = :deptId") })
public class DocumentGroup implements PAuthorizedObject {

	@Id
	@GeneratedValue
	private int id;

	private String name;

	@ManyToOne(fetch = FetchType.EAGER)
	@JsonBackReference(value="departmentBackRef")
	private Department department;

	@ManyToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
	@JsonBackReference(value="documentsBackRef")
	private Set<DocumentInfo> documents;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Department getDepartment() {
		return department;
	}

	public void setDepartment(Department department) {
		this.department = department;
	}

	public Set<DocumentInfo> getDocuments() {
		return documents;
	}

	public void setDocuments(Set<DocumentInfo> documents) {
		this.documents = documents;
	}

	public void addDocument(DocumentInfo info) {
		if (this.documents == null) {
			this.documents = new HashSet<>();
		}
		this.documents.add(info);
	}

	/**
	 * Removed the given {@link DocumentInfo} and so the concrete {@link Document} From this {@link DocumentGroup}
	 * 
	 * @param info
	 */
	public void removeDocument(DocumentInfo info) {
		DocumentInfo documentToRemove = null;
		for (DocumentInfo docInfo : documents) {
			if (docInfo.getId() == info.getId()) {
				documentToRemove = docInfo;
			}
		}
		if (documentToRemove != null) {
			this.documents.remove(documentToRemove);
		}
	}

	public int getId() {
		return id;
	}

	@Override
	public String toString() {
		return this.name;
	}

}
