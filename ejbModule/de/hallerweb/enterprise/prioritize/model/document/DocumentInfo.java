package de.hallerweb.enterprise.prioritize.model.document;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.OrderBy;
import javax.persistence.Version;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;

import de.hallerweb.enterprise.prioritize.model.Department;
import de.hallerweb.enterprise.prioritize.model.event.Event;
import de.hallerweb.enterprise.prioritize.model.event.PEventConsumer;
import de.hallerweb.enterprise.prioritize.model.event.PEventProducer;
import de.hallerweb.enterprise.prioritize.model.event.PObjectType;
import de.hallerweb.enterprise.prioritize.model.search.PSearchable;
import de.hallerweb.enterprise.prioritize.model.search.SearchProperty;
import de.hallerweb.enterprise.prioritize.model.search.SearchResult;
import de.hallerweb.enterprise.prioritize.model.search.SearchResultType;
import de.hallerweb.enterprise.prioritize.model.security.PAuthorizedObject;
import de.hallerweb.enterprise.prioritize.model.security.User;

/**
 * JPA entity to represent a reference to a {@link Document} and it's history.
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
@NamedQueries({
		@NamedQuery(name = "findDocumentInfosByDocumentGroup", query = "select di FROM DocumentInfo di WHERE di.documentGroup.id = :dgid"),
		@NamedQuery(name = "findDocumentInfoById", query = "select di FROM DocumentInfo di WHERE di.id = :docInfoId"),
		@NamedQuery(name = "findAllDocumentInfos", query = "select di FROM DocumentInfo di"),
		@NamedQuery(name = "findDocumentInfoByDocumentGroupAndName", query = "select di FROM DocumentInfo di WHERE di.documentGroup.id = :groupId AND di.currentDocument.name = :name"),
		@NamedQuery(name = "findDocumentGroupByNameAndDepartment", query = "select dg FROM DocumentGroup dg WHERE dg.name = :name AND dg.department.id = :deptId") })
public class DocumentInfo implements PAuthorizedObject, PSearchable, PEventProducer, PEventConsumer {

	transient List<SearchProperty> searchProperties;

	@Override
	public List<SearchResult> find(String phrase) {
		ArrayList<SearchResult> results = new ArrayList<SearchResult>();
		// Search document name
		if (this.currentDocument.getName().toLowerCase().indexOf(phrase.toLowerCase()) != -1) {
			// Match found
			SearchResult result = generateResult();
			results.add(result);
			return results;
		}
		if (this.getCurrentDocument().getChanges().toLowerCase().indexOf(phrase.toLowerCase()) != -1) {
			SearchResult result = generateResult();
			results.add(result);
		}
		return results;
	}

	private SearchResult generateResult() {
		SearchResult result = new SearchResult();
		result.setResult(this);
		result.setResultType(SearchResultType.DOCUMENT);
		result.setExcerpt(this.currentDocument.getName() + " Version: " + this.getCurrentDocument().getVersion() + " "
				+ this.getCurrentDocument().getChanges());
		result.setProvidesExcerpt(true);
		result.setSubresults(new HashSet<SearchResult>());
		return result;
	}

	@Override
	public List<SearchResult> find(String phrase, SearchProperty property) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<SearchProperty> getSearchProperties() {
		if (this.searchProperties == null) {
			searchProperties = new ArrayList<SearchProperty>();
			SearchProperty prop = new SearchProperty("VERSION");
			prop.setName("Version");
			SearchProperty prop2 = new SearchProperty("NAME");
			prop.setName("Name");
			searchProperties.add(prop);
			searchProperties.add(prop2);
		}
		return this.searchProperties;
	}

	@Id
	@GeneratedValue
	int id;

	@OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
	private Document currentDocument;

	@OneToOne
	@JsonBackReference
	private DocumentGroup documentGroup;

	@OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
	@OrderBy(value = "version")
	private SortedSet<Document> recentDocuments;

	private boolean locked;

	@OneToOne
	private User lockedBy;

	@Version
	private int entityVersion; // For optimistic locks

	public DocumentGroup getDocumentGroup() {
		return documentGroup;
	}

	public void setDocumentGroup(DocumentGroup documentGroup) {
		this.documentGroup = documentGroup;
	}

	public Set<Document> getRecentDocuments() {
		return recentDocuments;
	}

	public void setRecentDocuments(SortedSet<Document> recent) {
		this.recentDocuments = recent;
	}

	public Document getCurrentDocument() {
		return currentDocument;
	}

	public void setCurrentDocument(Document currentDocument) {
		this.currentDocument = currentDocument;
	}

	public boolean isLocked() {
		return locked;
	}

	public void setLocked(boolean locked) {
		this.locked = locked;
	}

	public User getLockedBy() {
		return lockedBy;
	}

	public void setLockedBy(User lockedBy) {
		this.lockedBy = lockedBy;
	}

	public int getId() {
		return id;
	}

	@Override
	@JsonIgnore
	public Department getDepartment() {
		return documentGroup.getDepartment();
	}

	@Override
	public void consumeEvent(Event evt) {
		System.out.println("Object " + evt.getSourceType() + " with ID " + evt.getSourceId() + " raised event: " + evt.getPropertyName()
				+ " with new Value: " + evt.getNewValue());

	}

	@Override
	public void raiseEvent(String name, Object oldValue, Object newValue) {
		// TODO Auto-generated method stub

	}

	@Override
	public PObjectType getObjectType() {
		return PObjectType.DOCUMENTINFO;
	}

}
