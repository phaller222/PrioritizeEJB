package de.hallerweb.enterprise.prioritize.model;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToOne;
import javax.persistence.Version;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;

import de.hallerweb.enterprise.prioritize.model.document.DocumentGroup;
import de.hallerweb.enterprise.prioritize.model.resource.ResourceGroup;
import de.hallerweb.enterprise.prioritize.model.search.PSearchable;
import de.hallerweb.enterprise.prioritize.model.search.SearchProperty;
import de.hallerweb.enterprise.prioritize.model.search.SearchResult;
import de.hallerweb.enterprise.prioritize.model.search.SearchResultType;
import de.hallerweb.enterprise.prioritize.model.security.PAuthorizedObject;

/**
 * JPA entity to represent a {@link Department} of a {@link Company}. A Department has a key role in the Prioritize authorization mechanism.
 * All objects which implement PAuthorizedObject must provide a Department. Usually this is the department the object belongs to (e.G. User
 * X works for Department Y). If the Department is null for any {@link PAuthorizedObject} this is handled as a special case.
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
@Entity(name = "Department")
@NamedQueries({
		@NamedQuery(name = "findAllDepartments", query = "SELECT d FROM Department d ORDER BY d.company.name"),
		@NamedQuery(name = "findDepartmentById", query = "SELECT d FROM Department d WHERE d.id = :deptId"),
		@NamedQuery(name = "findDepartmentByToken", query = "SELECT d FROM Department d WHERE d.token = :token"),
		@NamedQuery(name = "findResourceGroupInDepartment", query = "SELECT g FROM ResourceGroup g WHERE g.department.id= :deptId AND g.name=:groupName"),
		@NamedQuery(name = "findDefaultDepartmentAndCompany", query = "SELECT d FROM Department d WHERE d.company.name='Default Company' and d.name = 'Default Department'") })
public class Department implements PAuthorizedObject, PSearchable {

	@Id
	@GeneratedValue
	int id;

	@OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
	Address address;

	@ManyToOne(fetch = FetchType.EAGER)
	@JsonBackReference
	Company company;

	@ManyToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
	List<DocumentGroup> documentGroups;

	@ManyToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
	List<ResourceGroup> resourceGroups;

	String name;

	@Column(length = 3000)
	String description;

	@Version
	private int entityVersion; // For optimistic locks

	@JsonIgnore
	String token; // Secret token for things to be placed in this department

	transient List<SearchProperty> searchProperties;

	@Override
	public List<SearchResult> find(String phrase) {
		ArrayList<SearchResult> results = new ArrayList<SearchResult>();
		// Search document name
		if (name.toLowerCase().indexOf(phrase.toLowerCase()) != -1) {
			// Match found
			SearchResult result = generateResult();
			results.add(result);
			return results;
		}
		if (description.toLowerCase().indexOf(phrase.toLowerCase()) != -1) {
			SearchResult result = generateResult();
			results.add(result);
		}
		return results;
	}

	private SearchResult generateResult() {
		SearchResult result = new SearchResult();
		result.setResult(this);
		result.setResultType(SearchResultType.DEPARTMENT);
		result.setExcerpt(name + " : " + description + " Company: " + this.getCompany().getName());
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
			SearchProperty prop = new SearchProperty("DOCUMENT");
			prop.setName("DocumentInfo");
			searchProperties.add(prop);
		}
		return this.searchProperties;
	}

	public String getToken() {
		return token;
	}

	public void setToken(String token) {
		this.token = token;
	}

	public Department() {
		super();
		this.documentGroups = new ArrayList<DocumentGroup>();
		this.resourceGroups = new ArrayList<ResourceGroup>();
	}

	public int getId() {
		return id;
	}

	public List<DocumentGroup> getDocumentGroups() {
		return documentGroups;
	}

	public void setDocumentGroups(List<DocumentGroup> documentGroups) {
		this.documentGroups = documentGroups;
	}

	public void addDocumentGroup(DocumentGroup documentGroup) {
		if (this.documentGroups.isEmpty()) {
			documentGroups.add(documentGroup);
			return;
		} else {
			for (DocumentGroup g : documentGroups) {
				if (g.getId() == documentGroup.getId()) {
					return;
				}
			}
			if (!documentGroups.contains(documentGroup)) {
				documentGroups.add(documentGroup);
			}
		}

	}

	public List<ResourceGroup> getResourceGroups() {
		return resourceGroups;
	}

	public void setResourceGroups(List<ResourceGroup> resourceGroups) {
		this.resourceGroups = resourceGroups;
	}

	public void addResourceGroup(ResourceGroup resourceGroup) {
		if (this.resourceGroups.isEmpty()) {
			resourceGroups.add(resourceGroup);
			return;
		} else {
			for (ResourceGroup g : resourceGroups) {
				if (g.getId() == resourceGroup.getId()) {
					return;
				}
			}
			if (!resourceGroups.contains(resourceGroup)) {
				resourceGroups.add(resourceGroup);
			}
		}

	}

	public void setId(int id) {
		this.id = id;
	}

	/**
	 * If {@link Address} is null then this department is located at the companies mainAddress
	 * 
	 * @return The {@link Address} of the department
	 */
	public Address getAddress() {
		return address;
	}

	/**
	 * Sets the {@link Address} of the department. Set to <code>null</code> if department is located at the companies mainAddress.
	 * 
	 * @param address
	 *            - the {@link Address} to set for the department.
	 */
	public void setAddress(Address address) {
		this.address = address;
	}

	public Company getCompany() {
		return company;
	}

	public void setCompany(Company company) {
		this.company = company;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((address == null) ? 0 : address.hashCode());
		result = prime * result + ((company == null) ? 0 : company.hashCode());
		result = prime * result + ((description == null) ? 0 : description.hashCode());
		result = prime * result + ((documentGroups == null) ? 0 : documentGroups.hashCode());
		result = prime * result + entityVersion;
		result = prime * result + id;
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		result = prime * result + ((resourceGroups == null) ? 0 : resourceGroups.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Department other = (Department) obj;
		if (address == null) {
			if (other.address != null)
				return false;
		} else if (!address.equals(other.address))
			return false;
		if (company == null) {
			if (other.company != null)
				return false;
		} else if (!company.equals(other.company))
			return false;
		if (description == null) {
			if (other.description != null)
				return false;
		} else if (!description.equals(other.description))
			return false;
		if (documentGroups == null) {
			if (other.documentGroups != null)
				return false;
		} else if (!documentGroups.equals(other.documentGroups))
			return false;
		if (entityVersion != other.entityVersion)
			return false;
		if (id != other.id)
			return false;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		if (resourceGroups == null) {
			if (other.resourceGroups != null)
				return false;
		} else if (!resourceGroups.equals(other.resourceGroups))
			return false;
		return true;
	}

	@Override
	public Department getDepartment() {
		return this;
	}

}
