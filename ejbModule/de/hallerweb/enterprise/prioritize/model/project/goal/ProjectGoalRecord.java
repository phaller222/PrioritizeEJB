package de.hallerweb.enterprise.prioritize.model.project.goal;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.OneToOne;

import de.hallerweb.enterprise.prioritize.model.project.task.Task;

/**
 * ProjectGoalRecord - A concrete ProjectGoal. Depending if this ProjectGoalRecord indicates a target goal (and value)
 * or a currently achived goal by a task the Task field is the link to the current task assigned to this goal or null 
 * if this ProjectGoalRecord is just the description of what should be achieved (target goal).
 * @author peter
 *
 */
@Entity
public class ProjectGoalRecord {

	@Id
	@GeneratedValue
	int id;
		
	@OneToOne
	Task task;											// null if describing target goal, Link to task if concrete progress.

	@OneToOne
	ProjectGoalPropertyRecord property;					// Value to achieve if target goal, otherwise current value of underlying task.
	
	public int getId() {
		return id;
	}
}
