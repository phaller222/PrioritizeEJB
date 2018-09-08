package de.hallerweb.enterprise.prioritize.model.skill;

import java.util.HashSet;
import java.util.Set;

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

import com.fasterxml.jackson.annotation.JsonBackReference;

import de.hallerweb.enterprise.prioritize.model.Department;
import de.hallerweb.enterprise.prioritize.model.security.PAuthorizedObject;

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

	@Column(length = 3000)
	String description;

	@OneToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
	Set<SkillCategory> subCategories;

	@OneToOne
	SkillCategory parentCategory;

	@OneToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
	@JsonBackReference
	Set<Skill> skills;

	@Version
	private int entityVersion; // For optimistic locks

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
