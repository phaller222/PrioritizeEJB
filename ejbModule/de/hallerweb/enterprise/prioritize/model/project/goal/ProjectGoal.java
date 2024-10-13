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

package de.hallerweb.enterprise.prioritize.model.project.goal;

import jakarta.persistence.*;

import java.util.HashSet;
import java.util.Set;

/**
 * ProjectGoalProperty - Describes possible values of a given ProjectProperty a ProjectGoalPropertyNumericRecord might have.
 *
 * @author peter
 */
@Entity
@NamedQuery(name = "findProjectGoalRecordsForProjectGoal", query = "select gr FROM ProjectGoalRecord gr WHERE gr.projectGoal.id = :goalId")
@NamedQuery(name = "findAllProjectGoals", query = "select pg FROM ProjectGoal pg")
@NamedQuery(name = "findProjectGoalPropertiesForProjectGoal", query = "select prop FROM ProjectGoalProperty prop WHERE prop.projectGoal.id = :goalId")
public class ProjectGoal {

    @Id
    @GeneratedValue
    int id;

    String name;
    String description;

    @OneToOne
    ProjectGoalCategory category;

    @OneToMany(fetch = FetchType.EAGER, mappedBy = "projectGoal", cascade = CascadeType.ALL)
    Set<ProjectGoalProperty> properties;

    public ProjectGoalCategory getCategory() {
        return category;
    }

    public void setCategory(ProjectGoalCategory category) {
        this.category = category;
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

    public Set<ProjectGoalProperty> getProperties() {
        return properties;
    }

    public void setProperties(Set<ProjectGoalProperty> properties) {
        this.properties = properties;
    }

    public void addProjectGoalProperty(ProjectGoalProperty prop) {
        if (this.properties == null) {
            this.properties = new HashSet<>();
        }
        this.properties.add(prop);
    }

    public int getId() {
        return id;
    }

    @Override
    public String toString() {
        return name;
    }
}
