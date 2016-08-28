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

	public ProjectGoalProperty getProperty() {
		return property;
	}

	public void setProperty(ProjectGoalProperty property) {
		this.property = property;
	}

	public double getValue() {
		return value;
	}

	public void setValue(double value) {
		this.value = value;
	}

	public int getId() {
		return id;
	}
	
	
}
