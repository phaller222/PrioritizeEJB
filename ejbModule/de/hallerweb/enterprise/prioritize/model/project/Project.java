/*
 * Copyright 2015-2020 Peter Michael Haller and contributors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.hallerweb.enterprise.prioritize.model.project;

import java.util.Date;
import java.util.List;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.NamedQueries;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;

import com.fasterxml.jackson.annotation.JsonIgnore;

import de.hallerweb.enterprise.prioritize.model.document.DocumentInfo;
import de.hallerweb.enterprise.prioritize.model.project.task.Blackboard;
import de.hallerweb.enterprise.prioritize.model.project.task.PActor;
import de.hallerweb.enterprise.prioritize.model.resource.Resource;
import de.hallerweb.enterprise.prioritize.model.security.User;
import de.hallerweb.enterprise.prioritize.model.skill.SkillGroup;

/* Entity for projects */
@Entity
@NamedQueries({ @NamedQuery(name = "findProjectById", query = "select p FROM Project p WHERE p.id = :projectId"),
		@NamedQuery(name = "findProjectsByManager", query = "select p FROM Project p WHERE p.manager = :managerId"),
		@NamedQuery(name = "findProjectsByMember", query = "select p FROM Project p WHERE :user MEMBER OF p.users ORDER BY p.name") })
public class Project {

	@GeneratedValue
	@Id
	int id;

	@OneToOne
	PActor manager;

	String name;										// Name of the project
	String description;									// Description of the project
	Date beginDate;										// Begin date of this project
	Date dueDate;										// Project due date
	int maxManDays;										// Max. amount of man days this project should consume
	int priority;										// The prioritiy of this project

	@JsonIgnore
	@ManyToMany
	List<DocumentInfo> documents;						// DocumentInfo objects assigned to this project

	@JsonIgnore
	@ManyToMany
	List<Resource> resources;							// Resources assigned to this project

	@ManyToMany(fetch = FetchType.EAGER)
	List<User> users;									// Users assigned to this project

	@JsonIgnore
	@OneToMany
	List<SkillGroup> requiredSkills;					// The skills required to fullfill this project

	//@JsonIgnore
	//@OneToMany
	//List<SkillRecord> availableSkills;					// Skills already assigned to the project (=available)

	@JsonIgnore
	@OneToOne
	Blackboard blackboard;								// The blackboard with tasks for this project

	@JsonIgnore
	@OneToOne
	ActionBoard actionboard;							// ActionBoard with up to date information on the project.

	@OneToOne
	ProjectProgress progress;							// Observe project goals and progress.

	public void setId(int id) {
		this.id = id;
	}


	public PActor getManager() {
		return manager;
	}

	public void setManager(PActor manager) {
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

	/*public List<SkillRecord> getAvailableSkills() {
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
	}*/

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
