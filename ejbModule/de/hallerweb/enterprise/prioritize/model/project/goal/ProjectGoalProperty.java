package de.hallerweb.enterprise.prioritize.model.project.goal;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;

import com.fasterxml.jackson.annotation.JsonBackReference;

import de.hallerweb.enterprise.prioritize.model.skill.Skill;

/**
 * ProjectGoalProperty - Describes possible values of a given ProjectProperty a ProjectGoalPropertyNumericRecord might have.
 * @author peter
 *
 */
@Entity
@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)
public  class ProjectGoalProperty {

	@Id
	@GeneratedValue
	int id;
	
	String name;
	String description;
	
	@ManyToOne(fetch = FetchType.EAGER)
	@JsonBackReference
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
