/*
 * Copyright 2015-2024 Peter Michael Haller and contributors
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

package de.hallerweb.enterprise.prioritize.model.project.goal;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;

import java.util.HashSet;
import java.util.Set;

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
@NamedQuery(name = "findProjectGoalRootCategories", query = "select cat FROM ProjectGoalCategory cat WHERE cat.parentCategory IS NULL")
@NamedQuery(name = "findAllProjectGoalCategories", query = "select cat FROM ProjectGoalCategory cat")
@NamedQuery(name = "findProjectGoalCategoryByName", query = "select cat FROM ProjectGoalCategory cat WHERE cat.name = :categoryName")
@NamedQuery(name = "findProjectGoalCategoryById", query = "select cat FROM ProjectGoalCategory cat WHERE cat.id = :categoryId")
@NamedQuery(name = "findProjectGoalSubCategoriesForCategory", query = "select cat FROM ProjectGoalCategory cat WHERE cat.parentCategory.id = :parentCategoryId")
@NamedQuery(name = "findProjectGoalsForCategory", query = "select g FROM ProjectGoal g WHERE g.category.id = :catId")
public class ProjectGoalCategory {

    @Id
    @GeneratedValue
    int id;

    String name;
    String qualifiedName;

    String description;

    @OneToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    Set<ProjectGoalCategory> subCategories;

    @OneToOne
    ProjectGoalCategory parentCategory;

    @OneToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    @JsonBackReference(value = "projectGoalsBackRef")
    Set<ProjectGoal> projectGoals;

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
