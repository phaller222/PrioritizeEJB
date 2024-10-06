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
package de.hallerweb.enterprise.prioritize.model.security;

import de.hallerweb.enterprise.prioritize.model.Department;
import de.hallerweb.enterprise.prioritize.model.project.task.PActor;
import de.hallerweb.enterprise.prioritize.model.search.PSearchable;
import de.hallerweb.enterprise.prioritize.model.search.SearchProperty;
import de.hallerweb.enterprise.prioritize.model.search.SearchResult;
import de.hallerweb.enterprise.prioritize.model.search.SearchResultType;
import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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
@NamedQuery(name = "findRolesForPermissionRecord", query = "select r FROM Role r JOIN r.permissions p WHERE p.id = :recId")
@NamedQuery(name = "findRoleByRolename", query = "select r FROM Role r WHERE r.name=?1 ORDER BY r.name")
public class Role extends PActor implements PAuthorizedObject, PSearchable {


    String name;
    String description;

    @OneToMany(fetch = FetchType.EAGER, orphanRemoval = true)
    Set<PermissionRecord> permissions;

    @ManyToMany(fetch = FetchType.EAGER)
    Set<User> users;

    @OneToOne
    Department department;

    transient List<SearchProperty> searchProperties;

    private SearchResult generateResult() {
        SearchResult result = new SearchResult();
        result.setResult(this);
        result.setResultType(SearchResultType.ROLE);
        result.setExcerpt(name + " : " + this.getDescription());
        result.setProvidesExcerpt(true);
        result.setSubresults(new HashSet<>());
        return result;
    }

    @Override
    public List<SearchResult> find(String phrase) {
        ArrayList<SearchResult> results = new ArrayList<>();
        // Search role name
        if (name.toLowerCase().contains(phrase.toLowerCase())) {
            // Match found
            SearchResult result = generateResult();
            results.add(result);
            return results;
        }
        if (this.description != null && this.description.toLowerCase().contains(phrase.toLowerCase())) {
            // Match found
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
            SearchProperty prop = new SearchProperty("RESOURCE");
            prop.setName("Resource");
            searchProperties.add(prop);
        }
        return this.searchProperties;
    }

    public Role() {
        super();
        this.users = new HashSet<>();
    }

    public Set<PermissionRecord> getPermissions() {
        return permissions;
    }

    public void setPermissions(Set<PermissionRecord> permissions) {
        this.permissions = permissions;
    }

    public void addPermission(PermissionRecord rec) {
        if (this.permissions == null) {
            permissions = new HashSet<>();
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

    @Override
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

    public Department getDepartment() {
        return department;
    }

    public void setDepartment(Department department) {
        this.department = department;
    }

    @Override
    public String toString() {
        return this.name;
    }

}
