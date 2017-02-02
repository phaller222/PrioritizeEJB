package de.hallerweb.enterprise.prioritize.model;

import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Version;

import com.fasterxml.jackson.annotation.JsonIgnore;

import de.hallerweb.enterprise.prioritize.model.security.PAuthorizedObject;

/**
 * JPA entity to represent a {@link Company}. A Company is the object at the
 * very top of the Prioritize hierarchie. A Company can have one or more
 * {@link Department} objects.
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
@NamedQueries({ @NamedQuery(name = "findAllCompanies", query = "SELECT c FROM Company c ORDER BY c.name"),
		@NamedQuery(name = "findCompanyByName", query = "SELECT c FROM Company c WHERE c.name= ?1 ORDER BY c.name") })
public class Company implements PAuthorizedObject {

	@Id
	@GeneratedValue
	int id;

	String name;

	@Column(length = 3000)
	String description;

	@OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
	Address mainAddress;

	@JsonIgnore
	@OneToMany(fetch = FetchType.EAGER, mappedBy = "company", cascade = CascadeType.ALL)
	List<Department> departments;

	@Version
	private int entityVersion; // For optimistic locks

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public Address getMainAddress() {
		return mainAddress;
	}

	public void setMainAddress(Address mainAddress) {
		this.mainAddress = mainAddress;
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

	public List<Department> getDepartments() {
		return departments;
	}

	public void setDepartments(List<Department> departments) {
		this.departments = departments;
	}

	public void addDepartment(Department dept) {
		this.departments.add(dept);

	}

	public Department getDepartment() {
		return null;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((departments == null) ? 0 : departments.hashCode());
		result = prime * result + ((description == null) ? 0 : description.hashCode());
		result = prime * result + entityVersion;
		result = prime * result + id;
		result = prime * result + ((mainAddress == null) ? 0 : mainAddress.hashCode());
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		Company other = (Company) obj;
		if (departments == null) {
			if (other.departments != null) {
				return false;
			}
		} else if (!departments.equals(other.departments)) {
			return false;
		}
		if (description == null) {
			if (other.description != null) {
				return false;
			}
		} else if (!description.equals(other.description)) {
			return false;
		}
		if (entityVersion != other.entityVersion) {
			return false;
		}
		if (id != other.id) {
			return false;
		}
		if (mainAddress == null) {
			if (other.mainAddress != null) {
				return false;
			}
		} else if (!mainAddress.equals(other.mainAddress)) {
			return false;
		}
		if (name == null) {
			if (other.name != null) {
				return false;
			}
		} else if (!name.equals(other.name)) {
			return false;
		}
		return true;
	}

}
