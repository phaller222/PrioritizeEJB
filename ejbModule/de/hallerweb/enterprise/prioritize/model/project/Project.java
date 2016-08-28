package de.hallerweb.enterprise.prioritize.model.project;

import java.util.Date;
import java.util.List;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;

import de.hallerweb.enterprise.prioritize.model.document.DocumentInfo;
import de.hallerweb.enterprise.prioritize.model.project.task.Blackboard;
import de.hallerweb.enterprise.prioritize.model.resource.Resource;
import de.hallerweb.enterprise.prioritize.model.security.Role;
import de.hallerweb.enterprise.prioritize.model.security.User;
import de.hallerweb.enterprise.prioritize.model.skill.SkillGroup;
import de.hallerweb.enterprise.prioritize.model.skill.SkillRecord;

@Entity
@NamedQueries({ @NamedQuery(name = "findProjectById", query = "select p FROM Project p WHERE p.id = :projectId"),
	@NamedQuery(name = "findProjectsByManagerRole", query = "select p FROM Project p WHERE p.manager.id = :roleId"),
	 })
public class Project {

	@GeneratedValue
	@Id
	int id;

	@OneToOne
	Role manager;
	
	String name;														// Name of the project
	String description;													// Description of the project
	Date beginDate;														// Begin date of this project
	Date dueDate;														// Project due date 
	int maxManDays;														// Max. amount of man days this project should consume
	int priority;														// The prioritiy of this project
	
	@OneToMany
	List<DocumentInfo> documents;										// DocumentInfo objects assigned to this project
	
	@OneToMany
	List<Resource> resources;											// Resources assigned to this project
	@OneToMany
	List<User> users;													// Users assigned to this project
    @OneToMany	
	List<SkillGroup> requiredSkills;									// The skills required to fullfill this project
	@OneToMany
	List<SkillRecord> availableSkills;									// Skills already assigned to the project (=available)
	@OneToOne
	Blackboard blackboard;												// The blackboard with tasks for this project 
	@OneToOne
	ActionBoard actionboard;											// ActionBoard with up to date information on the project.
	@OneToOne
	ProjectProgress progress;											// Observe project goals and progress.
	
	
	public Role getManager() {
		return manager;
	}


	public void setManager(Role manager) {
		this.manager = manager;
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


	public Date getBeginDate() {
		return beginDate;
	}


	public void setBeginDate(Date beginDate) {
		this.beginDate = beginDate;
	}


	public Date getDueDate() {
		return dueDate;
	}


	public void setDueDate(Date dueDate) {
		this.dueDate = dueDate;
	}


	public int getMaxManDays() {
		return maxManDays;
	}


	public void setMaxManDays(int maxManDays) {
		this.maxManDays = maxManDays;
	}


	public int getPriority() {
		return priority;
	}


	public void setPriority(int priority) {
		this.priority = priority;
	}


	public List<DocumentInfo> getDocuments() {
		return documents;
	}


	public void setDocuments(List<DocumentInfo> documents) {
		this.documents = documents;
	}
	
	public void addDocument(DocumentInfo docInfo) {
		this.documents.add(docInfo);
	}
	
	public void removeDocument(DocumentInfo docInfo) {
		this.documents.remove(docInfo);
	}


	public List<Resource> getResources() {
		return resources;
	}


	public void setResources(List<Resource> resources) {
		this.resources = resources;
	}
	
	public void addResource(Resource res) {
		this.resources.add(res);
	}

	public void removeResource(Resource res) {
		this.resources.remove(res);
	}
	
	public List<User> getUsers() {
		return users;
	}


	public void setUsers(List<User> users) {
		this.users = users;
	}
	
	public void addUser(User user) {
		this.users.add(user);
	}

	public void removeUser(User user) {
		this.users.remove(user);
	}
	

	public List<SkillGroup> getRequiredSkills() {
		return requiredSkills;
	}


	public void setRequiredSkills(List<SkillGroup> requiredSkills) {
		this.requiredSkills = requiredSkills;
	}
	
	public void addRequiredSkill(SkillGroup group) {
		this.requiredSkills.add(group);
	}
	
	public void removeRequiredSkill(SkillGroup group) {
		this.requiredSkills.remove(group);
	}


	public List<SkillRecord> getAvailableSkills() {
		return availableSkills;
	}

	public void setAvailableSkills(List<SkillRecord> availableSkills) {
		this.availableSkills = availableSkills;
	}

	public void addAvailableSkill(SkillRecord record) {
		this.availableSkills.add(record);
	}
	
	public void removeAvailableSkill(SkillRecord skill) {
		this.availableSkills.remove(skill);	
	}

	public Blackboard getBlackboard() {
		return blackboard;
	}

	public void setBlackboard(Blackboard blackboard) {
		this.blackboard = blackboard;
	}

	public ActionBoard getActionboard() {
		return actionboard;
	}

	public void setActionboard(ActionBoard actionboard) {
		this.actionboard = actionboard;
	}

	public ProjectProgress getProgress() {
		return progress;
	}

	public void setProgress(ProjectProgress progress) {
		this.progress = progress;
	}
	
	public int getId() {
		return id;
	}
	
	
}
