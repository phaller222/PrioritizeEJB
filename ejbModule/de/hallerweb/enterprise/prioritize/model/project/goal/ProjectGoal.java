package de.hallerweb.enterprise.prioritize.model.project.goal;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;

/**
 * ProjectGoalProperty - Describes possible values of a given ProjectProperty a ProjectGoalPropertyNumericRecord might have.
 * @author peter
 *
 */
@Entity
@NamedQueries({
		@NamedQuery(name = "findProjectGoalRecordsForProjectGoal", query = "select gr FROM ProjectGoalRecord gr WHERE gr.projectGoal.id = :goalId"),
		@NamedQuery(name = "findAllProjectGoals", query = "select pg FROM ProjectGoal pg"),
		@NamedQuery(name = "findProjectGoalPropertiesForProjectGoal", query = "select prop FROM ProjectGoalProperty prop WHERE prop.projectGoal.id = :goalId") })
public class ProjectGoal {

	@Id
	@GeneratedValue
	int id;

	String name;
	String description;

	@OneToOne
	ProjectGoalCategory category;

	@OneToMany(fetch = FetchType.EAGER, mappedBy = "projectGoal", cascade = CascadeType.ALL)
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

	public void addProjectGoalProperty(ProjectGoalProperty prop) {
		if (this.properties == null) {
			this.properties = new ArrayList<>();
		}
		this.properties.add(prop);
	}

	public int getId() {
		return id;
	}

	@Override
	public String toString() {
		return name;
	}
}
