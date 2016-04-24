package de.hallerweb.enterprise.prioritize.model.document;

import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Version;

import com.fasterxml.jackson.annotation.JsonBackReference;

import de.hallerweb.enterprise.prioritize.model.Department;
import de.hallerweb.enterprise.prioritize.model.security.PAuthorizedObject;

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
	@JsonBackReference
	private Department department;

	@ManyToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
	@JsonBackReference
	private Set<DocumentInfo> documents;

	@Version
	private int entityVersion; // For optimistic locks

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

}
