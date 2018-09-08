package de.hallerweb.enterprise.prioritize.model.project.goal;

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

/**
 * JPA entity to represent a {@link ProjectGoalCategory}. ProjectGoals can be grouped into categories.
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
		@NamedQuery(name = "findProjectGoalRootCategories", query = "select cat FROM ProjectGoalCategory cat WHERE cat.parentCategory IS NULL"),
		@NamedQuery(name = "findAllProjectGoalCategories", query = "select cat FROM ProjectGoalCategory cat"),
		@NamedQuery(name = "findProjectGoalCategoryByName", query = "select cat FROM ProjectGoalCategory cat WHERE cat.name = :categoryName"),
		@NamedQuery(name = "findProjectGoalCategoryById", query = "select cat FROM ProjectGoalCategory cat WHERE cat.id = :categoryId"),
		@NamedQuery(name = "findProjectGoalSubCategoriesForCategory", query = "select cat FROM ProjectGoalCategory cat WHERE cat.parentCategory.id = :parentCategoryId"),
		@NamedQuery(name = "findProjectGoalsForCategory", query = "select g FROM ProjectGoal g WHERE g.category.id = :catId") })
public class ProjectGoalCategory  {

	@Id
	@GeneratedValue
	int id;

	String name;
	String qualifiedName;

	@Column(length = 3000)
	String description;

	@OneToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
	Set<ProjectGoalCategory> subCategories;

	@OneToOne
	ProjectGoalCategory parentCategory;

	@OneToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
	@JsonBackReference
	Set<ProjectGoal> projectGoals;

	@Version
	private int entityVersion; // For optimistic locks

	private static final String TYPE_CATEGORY = "CATEGORY";
	
	public ProjectGoalCategory() {
		super();
		this.subCategories = new HashSet<>();
	}

	public ProjectGoalCategory getParentCategory() {
		return parentCategory;
	}

	public void setParentCategory(ProjectGoalCategory parentCategory) {
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

	public Set<ProjectGoalCategory> getSubCategories() {
		return subCategories;
	}

	public void setSubCategories(Set<ProjectGoalCategory> subCategories) {
		this.subCategories = subCategories;
	}

	public void addSubCategory(ProjectGoalCategory category) {
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
		return TYPE_CATEGORY;
	}

}
