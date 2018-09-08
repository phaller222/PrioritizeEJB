package de.hallerweb.enterprise.prioritize.model.resource;

import java.util.Set;
import java.util.SortedSet;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OrderBy;
import javax.persistence.Version;

import com.fasterxml.jackson.annotation.JsonBackReference;

import de.hallerweb.enterprise.prioritize.model.Department;
import de.hallerweb.enterprise.prioritize.model.security.PAuthorizedObject;

/**
 * JPA entity to represent a {@link ResourceGroup}. A ResourceGroup groups Resources like a DocumentGroup or Directory can group documents.
 * All Resources belong to a ResourceGroup. If none has been specified, they are put to the default ResourceGroup.
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
@NamedQueries({ @NamedQuery(name = "findResourceGroupsForDepartment", query = "select rg FROM ResourceGroup rg WHERE rg.department.id = :deptId") })
public class ResourceGroup implements PAuthorizedObject {

	@Id
	@GeneratedValue
	private int id;
	private String name;

	@ManyToOne(fetch = FetchType.EAGER)
	@JsonBackReference
	private Department department;

	@ManyToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
	@OrderBy(value = "id")
	@JsonBackReference
	private SortedSet<Resource> resources;

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

	public Set<Resource> getResources() {
		return resources;
	}

	public void setResources(SortedSet<Resource> resources) {
		this.resources = resources;
	}

	public void addResource(Resource res) {
		this.resources.add(res);
	}

	public void removeResource(Resource res) {
		this.resources.remove(res);
	}

	public int getId() {
		return id;
	}

	@Override
	public String toString() {
		return this.getName();
	}

}
