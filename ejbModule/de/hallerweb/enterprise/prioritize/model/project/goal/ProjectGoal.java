package de.hallerweb.enterprise.prioritize.model.project.goal;

import java.util.List;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;


/**
 * ProjectGoalProperty - Describes possible values of a given ProjectProperty a ProjectGoalPropertyRecord might have.
 * @author peter
 *
 */
@Entity
public class ProjectGoal {

	@Id
	@GeneratedValue
	int id;
	
	String name;
	String description;
	
	@OneToOne
	ProjectGoalCategory category;
	
	@OneToMany
	List<ProjectGoalProperty> properties;
	
	public ProjectGoalCategory getCategory() {
		return category;
	}

	public void setCategory(ProjectGoalCategory category) {
		this.category = category;
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

	public List<ProjectGoalProperty> getProperties() {
		return properties;
	}

	public void setProperties(List<ProjectGoalProperty> properties) {
		this.properties = properties;
	}

	public int getId() {
		return id;
	}
}
