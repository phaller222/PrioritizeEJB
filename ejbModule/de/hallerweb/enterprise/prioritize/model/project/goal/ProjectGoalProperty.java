package de.hallerweb.enterprise.prioritize.model.project.goal;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

/**
 * ProjectGoalProperty - Describes possible values of a given ProjectProperty a ProjectGoalPropertyRecord might have.
 * @author peter
 *
 */
@Entity
public class ProjectGoalProperty {

	@Id
	@GeneratedValue
	int id;
	
	String name;
	String description;
	double min;
	double max;
	
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
	public double getMin() {
		return min;
	}
	public void setMin(double min) {
		this.min = min;
	}
	public double getMax() {
		return max;
	}
	public void setMax(double max) {
		this.max = max;
	}
	public int getId() {
		return id;
	}
}
