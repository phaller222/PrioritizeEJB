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
package de.hallerweb.enterprise.prioritize.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import de.hallerweb.enterprise.prioritize.model.document.DocumentGroup;
import de.hallerweb.enterprise.prioritize.model.resource.ResourceGroup;
import de.hallerweb.enterprise.prioritize.model.search.PSearchable;
import de.hallerweb.enterprise.prioritize.model.search.SearchProperty;
import de.hallerweb.enterprise.prioritize.model.search.SearchResult;
import de.hallerweb.enterprise.prioritize.model.search.SearchResultType;
import de.hallerweb.enterprise.prioritize.model.security.PAuthorizedObject;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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
@NamedQueries({ @NamedQuery(name = "findAllDepartments", query = "SELECT d FROM Department d ORDER BY d.company.name"),
		@NamedQuery(name = "findDepartmentById", query = "SELECT d FROM Department d WHERE d.id = ?1 ORDER BY d.name"),
		@NamedQuery(name = "findDepartmentByToken", query = "SELECT d FROM Department d WHERE d.token = ?1 ORDER BY d.name"),
		@NamedQuery(name = "findDepartmentsByCompany", query = "SELECT d FROM Department d WHERE d.company.id = ?1 ORDER BY d.name"),
		@NamedQuery(name = "findResourceGroupInDepartment", query = "SELECT g FROM ResourceGroup g WHERE g.department.id= :deptId AND g.name=:groupName") })
public class Department extends PObject implements PAuthorizedObject, PSearchable {

	public static final String PROPERTY_NAME = "name";
	public static final String PROPERTY_DESCRIPTION = "description";
	public static final String PROPERTY_ADDRESS = "address";

	@OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
	Address address;

	@ManyToOne(fetch = FetchType.EAGER)
	@JsonBackReference
	Company company;
	
	@ManyToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
	Set<DocumentGroup> documentGroups;

	@ManyToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
	Set<ResourceGroup> resourceGroups;

	String name;


	String description;

	@JsonIgnore
	String token; // Secret token for things to be placed in this department

	transient List<SearchProperty> searchProperties;

	@Override
	public List<SearchResult> find(String phrase) {
		ArrayList<SearchResult> results = new ArrayList<>();
		// Search document name
		if (name.toLowerCase().contains(phrase.toLowerCase())) {
			// Match found
			SearchResult result = generateResult();
			results.add(result);
			return results;
		}
		if (description.toLowerCase().contains(phrase.toLowerCase())) {
			SearchResult result = generateResult();
			results.add(result);
		}
		return results;
	}

	@Override
	public List<SearchResult> find(String phrase, SearchProperty property) {
		return new ArrayList<>();
	}

	@Override
	public List<SearchProperty> getSearchProperties() {
		if (this.searchProperties == null) {
			searchProperties = new ArrayList<>();
			SearchProperty prop = new SearchProperty("DOCUMENT");
			prop.setName("DocumentInfo");
			searchProperties.add(prop);
		}
		return this.searchProperties;
	}

	private SearchResult generateResult() {
		SearchResult result = new SearchResult();
		result.setResult(this);
		result.setResultType(SearchResultType.DEPARTMENT);
		result.setExcerpt(name + " : " + description + " Company: " + this.getCompany().getName());
		result.setProvidesExcerpt(true);
		result.setSubresults(new HashSet<>());
		return result;
	}

	public String getToken() {
		return token;
	}

	public void setToken(String token) {
		this.token = token;
	}

	public Department() {
		super();
		this.documentGroups = new HashSet<>();
		this.resourceGroups = new HashSet<>();
	}

	public int getId() {
		return id;
	}

	public Set<DocumentGroup> getDocumentGroups() {
		return documentGroups;
	}

	public void setDocumentGroups(Set<DocumentGroup> documentGroups) {
		this.documentGroups = documentGroups;
	}

	public void addDocumentGroup(DocumentGroup documentGroup) {
		if (this.documentGroups.isEmpty()) documentGroups.add(documentGroup);
		else {
			for (DocumentGroup g : documentGroups) {
				if (g.getId() == documentGroup.getId()) {
					return;
				}
			}
			documentGroups.add(documentGroup);
		}

	}

	public Set<ResourceGroup> getResourceGroups() {
		return resourceGroups;
	}

	public void setResourceGroups(Set<ResourceGroup> resourceGroups) {
		this.resourceGroups = resourceGroups;
	}

	public void addResourceGroup(ResourceGroup resourceGroup) {
		if (this.resourceGroups.isEmpty()) {
			resourceGroups.add(resourceGroup);
		} else {
			for (ResourceGroup g : resourceGroups) {
				if (g.getId() == resourceGroup.getId()) {
					return;
				}
			}
			resourceGroups.add(resourceGroup);
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
	@JsonBackReference
	public Department getDepartment() {
		return this;
	}

	@Override
	public String toString() {
		return this.getName();
	}

}
