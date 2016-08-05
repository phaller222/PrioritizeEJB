package de.hallerweb.enterprise.prioritize.model.project;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.OneToOne;

import de.hallerweb.enterprise.prioritize.model.project.task.Blackboard;

@Entity
public class Project {

	@GeneratedValue
	@Id
	int id;

	@OneToOne
	Blackboard blackboard;
	
	public int getId() {
		return id;
	}
	
	
}
