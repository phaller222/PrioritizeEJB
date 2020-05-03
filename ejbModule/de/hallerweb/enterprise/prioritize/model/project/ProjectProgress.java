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
package de.hallerweb.enterprise.prioritize.model.project;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;

import de.hallerweb.enterprise.prioritize.model.project.goal.ProjectGoalRecord;

/**
 * 
 * @author peter
 * ProjectProgress - Indicates the progress of the project. It holds information about the
 * ProjectGoals and calculates the project progress by observing how project goals aree comingg forward. 
 */
@Entity
@NamedQueries({
	@NamedQuery(name = "findProjectProgressById", query = "select pgr FROM ProjectProgress pgr WHERE pgr.id = :projectProgressId")})
public class ProjectProgress {

	@Id
	@GeneratedValue
	int id;

	@OneToMany(fetch = FetchType.EAGER)
	Set<ProjectGoalRecord> targetGoals;		// Goals for this project. If concrete tasks are build for this goal,
												// a copy of this goal is created and assigned to a task.

	int progress;											// Project progress in percent (0-100)

	public Set<ProjectGoalRecord> getTargetGoals() {
		return targetGoals;
	}

	public void setTargetGoals(Set<ProjectGoalRecord> targetGoals) {
		this.targetGoals = targetGoals;
	}

	public void addTargetGoal(ProjectGoalRecord targetGoal) {
		if (this.targetGoals == null) {
			this.targetGoals = new HashSet<>();
		}
		this.targetGoals.add(targetGoal);
	}

	public int getProgress() {
		return progress;
	}

	public void setProgress(int progress) {
		this.progress = progress;
	}

	public int getId() {
		return id;
	}
}
