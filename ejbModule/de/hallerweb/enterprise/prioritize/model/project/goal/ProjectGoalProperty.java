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

/**
 * ProjectGoalProperty - Describes possible values of a given ProjectProperty a ProjectGoalPropertyNumericRecord might have.
 *
 * @author peter
 */
@Entity
@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)
public class ProjectGoalProperty {

    @Id
    @GeneratedValue
    int id;

    String name;
    String description;

    @ManyToOne(fetch = FetchType.EAGER)
    @JsonBackReference(value = "projectGoalBackRef")
    ProjectGoal projectGoal;

    public ProjectGoal getProjectGoal() {
        return projectGoal;
    }

    public void setProjectGoal(ProjectGoal projectGoal) {
        this.projectGoal = projectGoal;
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

    public int getId() {
        return id;
    }

    @Override
    public String toString() {
        return name;
    }
}
