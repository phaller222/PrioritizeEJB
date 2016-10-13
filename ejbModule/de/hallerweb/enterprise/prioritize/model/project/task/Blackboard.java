package de.hallerweb.enterprise.prioritize.model.project.task;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;

import de.hallerweb.enterprise.prioritize.model.project.Project;

@Entity
 @NamedQueries({ @NamedQuery(name = "findBlackboardById", query = "select bb FROM Blackboard bb WHERE bb.id = :blackboardId")
 })
public class Blackboard {
	@Id
	@GeneratedValue
	int id;

	private String title;
	private String description;
	boolean frozen;

	@OneToMany(fetch = FetchType.EAGER)
	List<Task> tasks;

	@OneToOne
	Project project;

	public Project getProject() {
		return project;
	}

	public void setProject(Project project) {
		this.project = project;
	}

	public boolean isFrozen() {
		return frozen;
	}

	public void setFrozen(boolean frozen) {
		this.frozen = frozen;
	}

	public List<Task> getTasks() {
		return tasks;
	}

	public void setTasks(List<Task> tasks) {
		this.tasks = tasks;
	}

	public void addTask(Task t) {
		if (tasks == null) {
			tasks = new ArrayList<Task>();
		}
		this.tasks.add(t);
	}

	public void removeTask(Task t) {
		this.tasks.remove(t);
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
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
}
