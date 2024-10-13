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

import com.fasterxml.jackson.annotation.JsonIgnore;
import de.hallerweb.enterprise.prioritize.model.security.PAuthorizedObject;
import jakarta.persistence.*;

import java.util.List;

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
@NamedQuery(name = "findAllCompanies", query = "SELECT c FROM Company c ORDER BY c.name")
@NamedQuery(name = "findCompanyByName", query = "SELECT c FROM Company c WHERE c.name = :name ORDER BY c.name")
@NamedQuery(name = "findCompanyById", query = "SELECT c FROM Company c WHERE c.id = ?1 ORDER BY c.name")
public class Company implements PAuthorizedObject {

    @Id
    @GeneratedValue
    int id;

    String name;
    String description;

    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
    Address mainAddress;

    String url;
    String vatNumber;
    String taxId;

    @JsonIgnore
    @OneToMany(fetch = FetchType.EAGER, mappedBy = "company", cascade = CascadeType.ALL)
    List<Department> departments;

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

    public String getVatNumber() {
        return vatNumber;
    }

    public void setVatNumber(String vatNumber) {
        this.vatNumber = vatNumber;
    }

    public String getTaxId() {
        return taxId;
    }

    public void setTaxId(String taxId) {
        this.taxId = taxId;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }


    @Override
    public String toString() {
        return this.getName();
    }

}
