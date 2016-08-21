package de.hallerweb.enterprise.prioritize.model.project.goal;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.OneToOne;

@Entity
public class ProjectGoalPropertyRecord {
	
	@Id
	@GeneratedValue
	int id;
	
	@OneToOne
	ProjectGoalProperty property;
	
	double value;
	
	
}
