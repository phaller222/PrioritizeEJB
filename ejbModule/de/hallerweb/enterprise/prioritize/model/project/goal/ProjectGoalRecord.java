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

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.NamedQueries;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.OneToOne;

import com.fasterxml.jackson.annotation.JsonBackReference;

import de.hallerweb.enterprise.prioritize.model.project.Project;
import de.hallerweb.enterprise.prioritize.model.project.task.Task;

/**
 * ProjectGoalRecord - A concrete ProjectGoal. Depending if this ProjectGoalRecord indicates a target goal (and value)
 * or a currently achived goal by a task the Task field is the link to the current task assigned to this goal or null 
 * if this ProjectGoalRecord is just the description of what should be achieved (target goal).
 * @author peter
 *
 */
@Entity
@NamedQueries({
		@NamedQuery(name = "findProjectGoalRecordById", query = "select pgr FROM ProjectGoalRecord pgr WHERE pgr.id = :projectGoalRecordId"),
		@NamedQuery(name = "findProjectGoalRecordsByProject", query = "select pgr FROM ProjectGoalRecord pgr WHERE pgr.project.id = :projectId"),
		@NamedQuery(name = "findActiveProjectGoalRecordsByProject", query = "select pgr FROM ProjectGoalRecord pgr WHERE pgr.project.id = :projectId AND pgr.task IS NOT NULL") })
public class ProjectGoalRecord {

	@Id
	@GeneratedValue
	int id;

	@OneToOne
	Task task;	// null if describing target goal, Link to task if concrete progress.

	@JsonBackReference(value="projectBackRef")
	@OneToOne
	Project project; // Project this ProjectGoalRecord belongs to.

	@OneToOne
	ProjectGoal projectGoal; // The base ProjectGoal

	@OneToOne
	ProjectGoalPropertyRecord propertyRecord; // Property record if NumericProperty is used.

	int percentage; // percentage of completion of this ProjectGoalRecord


	public ProjectGoalRecord(ProjectGoalRecord origin, ProjectGoalPropertyRecord rec, Task task) {
		this.project = origin.getProject();
		this.task = task;
		this.projectGoal = origin.getProjectGoal();
		this.propertyRecord = rec;
	}

	public ProjectGoalRecord() {
		super();
	}

	public int getPercentage() {
		return percentage;
	}

	public void setPercentage(int percentage) {
		this.percentage = percentage;
	}

	public ProjectGoal getProjectGoal() {
		return projectGoal;
	}

	public void setProjectGoal(ProjectGoal projectGoal) {
		this.projectGoal = projectGoal;
	}

	public Project getProject() {
		return project;
	}

	public void setProject(Project project) {
		this.project = project;
	}

	public Task getTask() {
		return task;
	}

	public void setTask(Task task) {
		this.task = task;
	}

	public ProjectGoalPropertyRecord getPropertyRecord() {
		return propertyRecord;
	}

	public void setPropertyRecord(ProjectGoalPropertyRecord property) {
		this.propertyRecord = property;
	}

	public int getId() {
		return id;
	}
}
