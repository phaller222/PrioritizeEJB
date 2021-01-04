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
package de.hallerweb.enterprise.prioritize.model.skill;

import com.fasterxml.jackson.annotation.JsonBackReference;
import de.hallerweb.enterprise.prioritize.model.Department;
import de.hallerweb.enterprise.prioritize.model.security.PAuthorizedObject;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Set;

/**
 * JPA entity to represent a {@link SkillCategory}. Skills can be grouped into categories.
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
		@NamedQuery(name = "findRootCategories", query = "select cat FROM SkillCategory cat WHERE cat.parentCategory IS NULL"),
		@NamedQuery(name = "findAllCategories", query = "select cat FROM SkillCategory cat"),
		@NamedQuery(name = "findCategoryByName", query = "select cat FROM SkillCategory cat WHERE cat.name = :categoryName"),
		@NamedQuery(name = "findCategoryById", query = "select cat FROM SkillCategory cat WHERE cat.id = :categoryId"),
		@NamedQuery(name = "findSubCategoriesForCategory", query = "select cat FROM SkillCategory cat WHERE cat.parentCategory.id = :parentCategoryId"),
		@NamedQuery(name = "findSkillsForCategory", query = "select s FROM Skill s WHERE s.category.id = :catId") })
public class SkillCategory implements PAuthorizedObject, SkillType {

	@Id
	@GeneratedValue
	int id;

	String name;
	String qualifiedName;


	String description;

	@OneToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
	Set<SkillCategory> subCategories;

	@OneToOne
	SkillCategory parentCategory;

	@OneToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
	@JsonBackReference
	Set<Skill> skills;

	public SkillCategory() {
		super();
		this.subCategories = new HashSet<>();
	}

	public SkillCategory getParentCategory() {
		return parentCategory;
	}

	public void setParentCategory(SkillCategory parentCategory) {
		this.parentCategory = parentCategory;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Set<SkillCategory> getSubCategories() {
		return subCategories;
	}

	public void setSubCategories(Set<SkillCategory> subCategories) {
		this.subCategories = subCategories;
	}

	public void addSubCategory(SkillCategory category) {
		this.subCategories.add(category);

	}

	public String getQualifiedName() {
		qualifiedName = "";
		if (this.parentCategory == null) {
			qualifiedName = name;
		} else {
			this.qualifiedName = this.parentCategory.getQualifiedName() + "-" + name;
		}
		return qualifiedName;
	}

	public void setQualifiedName(String qualifiedName) {
		this.qualifiedName = qualifiedName;
	}

	@Override
	public String toString() {
		return this.getName();
	}

	public String getType() {
		return "CATEGORY";
	}

	@Override
	public Department getDepartment() {
		return null;
	}

}
