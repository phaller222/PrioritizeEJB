package de.hallerweb.enterprise.prioritize.model.project;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.Entity;
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

	@OneToMany
	List<ProjectGoalRecord> targetGoals;					// Goals for this project. If concrete tasks are build for this goal,
															// a copy of this goal is created and assigned to a task.

	int progress;											// Project progress in percent (0-100)

	public List<ProjectGoalRecord> getTargetGoals() {
		return targetGoals;
	}

	public void setTargetGoals(List<ProjectGoalRecord> targetGoals) {
		this.targetGoals = targetGoals;
	}

	public void addTargetGoal(ProjectGoalRecord targetGoal) {
		if (this.targetGoals == null) {
			this.targetGoals = new ArrayList<ProjectGoalRecord>();
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

	/**
	 * Calculates the project progress in percent (0-100).
	 * @return
	 */
	public int calcProgress() {
		return 0;
	}

}
