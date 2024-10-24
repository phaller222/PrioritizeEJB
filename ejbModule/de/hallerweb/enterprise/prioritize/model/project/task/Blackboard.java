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

package de.hallerweb.enterprise.prioritize.model.project.task;

import com.fasterxml.jackson.annotation.JsonBackReference;
import de.hallerweb.enterprise.prioritize.model.project.Project;
import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("ALL")
@Entity
@NamedQuery(name = "findBlackboardById", query = "select bb FROM Blackboard bb WHERE bb.id = :blackboardId")
@NamedQuery(name = "findBlackboardTasks", query = "select t FROM Task t, Blackboard b WHERE t MEMBER OF b.tasks AND b.id = :blackboardId")
public class Blackboard {
    @Id
    @GeneratedValue
    int id;

    private String title;
    private String description;
    boolean frozen;

    @OneToMany(fetch = FetchType.LAZY)
    List<Task> tasks;

    @JsonBackReference(value = "projectBackRef")
    @OneToOne
    Project project;

    public Project getProject() {
        return project;
    }

    public void setProject(Project project) {
        this.project = project;
    }

    public boolean isFrozen() {
        return frozen;
    }

    public void setFrozen(boolean frozen) {
        this.frozen = frozen;
    }

    public List<Task> getTasks() {
        return tasks;
    }

    public void setTasks(List<Task> tasks) {
        if (this.tasks == null) {
            this.tasks = new ArrayList<>();
        } else {
            this.tasks.clear();
        }
        this.tasks.addAll(tasks);
    }

    public void addTask(Task task) {
        if (tasks == null) {
            tasks = new ArrayList<>();
        }
        this.tasks.add(task);
    }

    public void removeTask(Task task) {
        this.tasks.remove(task);
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
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
}
