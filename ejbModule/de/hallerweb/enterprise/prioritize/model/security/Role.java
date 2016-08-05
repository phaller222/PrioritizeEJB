package de.hallerweb.enterprise.prioritize.model.security;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToMany;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.Version;

import de.hallerweb.enterprise.prioritize.model.Department;
import de.hallerweb.enterprise.prioritize.model.project.task.PActor;
import de.hallerweb.enterprise.prioritize.model.search.PSearchable;
import de.hallerweb.enterprise.prioritize.model.search.SearchProperty;
import de.hallerweb.enterprise.prioritize.model.search.SearchResult;
import de.hallerweb.enterprise.prioritize.model.search.SearchResultType;

/**
 * JPA entity to represent a {@link Role}. Each User must be assigned one or more Roles. A Role defines by one or more
 * {@link PermissionRecord} objects what a User is allowed to do, where and on which kinds of objects.
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
@NamedQueries({@NamedQuery(name = "findRolesForPermissionRecord", query = "select r FROM Role r JOIN r.permissions p WHERE p.id = :recId"),
	@NamedQuery(name = "findRoleByRolename", query = "select r FROM Role r WHERE r.name=?1 ORDER BY r.name")
})
public class Role extends PActor implements PAuthorizedObject, PSearchable {


	String name;

	@Column(length = 3000)
	String description;

	@OneToMany(fetch = FetchType.EAGER, orphanRemoval = true)
	Set<PermissionRecord> permissions;

	@ManyToMany(fetch = FetchType.EAGER)
	Set<User> users;

	transient List<SearchProperty> searchProperties;

	@Override
	public List<SearchResult> find(String phrase) {
		ArrayList<SearchResult> results = new ArrayList<SearchResult>();
		// Search role name
		if (name.toLowerCase().indexOf(phrase.toLowerCase()) != -1) {
			// Match found
			SearchResult result = generateResult();
			results.add(result);
			return results;
		}
		if (this.description != null && this.description.toLowerCase().indexOf(phrase.toLowerCase()) != -1) {
			// Match found
			SearchResult result = generateResult();
			results.add(result);
		}
		return results;
	}

	private SearchResult generateResult() {
		SearchResult result = new SearchResult();
		result.setResult(this);
		result.setResultType(SearchResultType.ROLE);
		result.setExcerpt(name + " : " + this.getDescription());
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
			SearchProperty prop = new SearchProperty("RESOURCE");
			prop.setName("Resource");
			searchProperties.add(prop);
		}
		return this.searchProperties;
	}

	public Role() {
		super();
		this.users = new HashSet<User>();
	}

	public Set<PermissionRecord> getPermissions() {
		return permissions;
	}

	public void setPermissions(Set<PermissionRecord> permissions) {
		this.permissions = permissions;
	}

	public void addPermission(PermissionRecord rec) {
		if (this.permissions == null) {
			permissions = new HashSet<PermissionRecord>();
		}
		this.permissions.add(rec);
	}

	public Set<User> getUsers() {
		return users;
	}

	public void setUsers(Set<User> users) {
		this.users = users;
	}

	public void addUser(User user) {
		this.users.add(user);
	}

	public void removeUser(User user) {
		users.remove(user);
	}

	public int getId() {
		return id;
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

	/**
	 * Necessary to be an PAuthorizedObject. Department for Roles always return null here.
	 */
	public Department getDepartment() {
		return null;
	}

}
