package de.hallerweb.enterprise.prioritize.model.project.task;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ManyToMany;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;

import com.fasterxml.jackson.annotation.JsonIgnore;

import de.hallerweb.enterprise.prioritize.model.PObject;
import de.hallerweb.enterprise.prioritize.model.calendar.TimeSpan;
import de.hallerweb.enterprise.prioritize.model.document.Document;
import de.hallerweb.enterprise.prioritize.model.project.goal.ProjectGoalRecord;
import de.hallerweb.enterprise.prioritize.model.resource.Resource;
import de.hallerweb.enterprise.prioritize.model.skill.SkillRecord;

@Entity
@NamedQueries({ 
	@NamedQuery(name = "findTaskById", query = "select t FROM Task t WHERE t.id = :taskId"),
	@NamedQuery(name = "findTasksByAssignee", query = "select t FROM Task t WHERE :assignee = t.assignee"),
		//@NamedQuery(name = "findTasksInProjectNotAssignedToUser", query = "select t FROM Task t WHERE t.projectGoalRecord.project = :project AND NOT :assignee MEMBER OF  t.assignees"),
	@NamedQuery(name = "findTasksInProjectNotAssignedToUser", query="select DISTINCT t FROM Task t " + 
																	"LEFT JOIN ProjectGoalRecord PGR2 ON PGR2.project = :project WHERE "+ 
																	"NOT :assignee = t.assignee OR t.assignee IS NULL AND PGR2.project = :project"),
	@NamedQuery(name = "findTasksInProjectAssignedToUser", query =  "select t FROM Task t " +  
																	"LEFT JOIN ProjectGoalRecord PGR2 ON PGR2.project = :project WHERE "+ 
																	":assignee = t.assignee AND PGR2.project = :project") })
public class Task extends PObject implements Comparable {

	private int priority;
	private String name;
	private String description;
	private TaskStatus taskStatus;
	@OneToOne(fetch = FetchType.EAGER)
	private Task parent;

	@OneToMany(fetch = FetchType.LAZY)
	private List<Task> subTasks;

	@JsonIgnore
	@OneToMany
	private List<Resource> resources;

	@JsonIgnore
	@OneToMany
	private List<Document> documents;

	@JsonIgnore
	@OneToMany
	private List<SkillRecord> requiredSkills;

	@OneToOne
	private PActor assignee;

	@JsonIgnore
	@OneToOne
	ProjectGoalRecord projectGoalRecord;

	@OneToMany(fetch = FetchType.LAZY)
	List<TimeSpan> timeSpent;

	public List<TimeSpan> getTimeSpent() {
		return timeSpent;
	}

	public void setTimeSpent(List<TimeSpan> timeSpent) {
		this.timeSpent = timeSpent;
	}

	public void addTimeSpent(TimeSpan timeSpent) {
		this.timeSpent.add(timeSpent);
	}

	public ProjectGoalRecord getProjectGoalRecord() {
		return projectGoalRecord;
	}

	public void setProjectGoalRecord(ProjectGoalRecord projectGoalRecord) {
		this.projectGoalRecord = projectGoalRecord;
	}

	public int getPriority() {
		return priority;
	}

	public void setPriority(int priority) {
		this.priority = priority;
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

	public TaskStatus getTaskStatus() {
		return taskStatus;
	}

	public void setTaskStatus(TaskStatus taskStatus) {
		this.taskStatus = taskStatus;
	}

	public List<Resource> getResources() {
		return resources;
	}

	public void setResources(List<Resource> resources) {
		this.resources = resources;
	}

	public List<Document> getDocuments() {
		return documents;
	}

	public void setDocuments(List<Document> documents) {
		this.documents = documents;
	}

	public List<SkillRecord> getRequiredSkills() {
		return requiredSkills;
	}

	public void setRequiredSkills(List<SkillRecord> requiredSkills) {
		this.requiredSkills = requiredSkills;
	}

	public PActor getAssignee() {
		return assignee;
	}

	public void setAssignee(PActor assignee) {
		this.assignee = assignee;
	}

	public void setParent(Task parent) {
		this.parent = parent;
	}

	public void setSubTasks(List<Task> subTasks) {
		this.subTasks = subTasks;
	}

	public Task getParent() {
		return parent;
	}

	public List<Task> getSubTasks() {
		return subTasks;
	}

	public boolean isSubTask() {
		return parent != null;
	}

	public void removeAssignee() {
		this.assignee = null;
	}

	

	@Override
	public int getId() {
		return id;
	}
	
	@Override
	public int compareTo(Object obj) {
		if (obj == null) {
			return 1;
		} else if (!(obj instanceof Task)) {
			return 1;
		} else if (((Task) obj).getId() == id) {
			return 0;
		} else {
			return -1;
		}

	}
}
