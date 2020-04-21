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
package de.hallerweb.enterprise.prioritize.view.project.wizard;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.enterprise.context.SessionScoped;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.inject.Named;

import org.primefaces.event.FlowEvent;

import de.hallerweb.enterprise.prioritize.controller.document.DocumentController;
import de.hallerweb.enterprise.prioritize.controller.project.ProjectController;
import de.hallerweb.enterprise.prioritize.controller.project.task.BlackboardController;
import de.hallerweb.enterprise.prioritize.controller.project.task.TaskController;
import de.hallerweb.enterprise.prioritize.controller.resource.ResourceController;
import de.hallerweb.enterprise.prioritize.controller.security.AuthorizationController;
import de.hallerweb.enterprise.prioritize.controller.security.SessionController;
import de.hallerweb.enterprise.prioritize.controller.security.UserRoleController;
import de.hallerweb.enterprise.prioritize.model.document.DocumentInfo;
import de.hallerweb.enterprise.prioritize.model.project.Project;
import de.hallerweb.enterprise.prioritize.model.project.task.Blackboard;
import de.hallerweb.enterprise.prioritize.model.project.task.Task;
import de.hallerweb.enterprise.prioritize.model.project.task.TaskStatus;
import de.hallerweb.enterprise.prioritize.model.resource.Resource;
import de.hallerweb.enterprise.prioritize.model.security.Role;
import de.hallerweb.enterprise.prioritize.model.security.User;
import de.hallerweb.enterprise.prioritize.view.project.ListProjectsBean;

@Named
@SessionScoped
public class ClassicApproach implements Serializable {

	@EJB
	UserRoleController userRoleController;
	@EJB
	DocumentController documentController;
	@EJB
	ResourceController resourceController;
	@EJB
	ProjectController projectController;
	@EJB
	TaskController taskController;
	@EJB
	BlackboardController blackboardController;
	@Inject
	SessionController sessionController;
	
	@Inject
	ListProjectsBean listProjectsBean;

	public List<Resource> getResources() {
		return resources;
	}

	public void setResources(List<Resource> resources) {
		this.resources = resources;
	}

	@PostConstruct
	public void initialize() {
		project = new Project();
		tasks = new ArrayList<>();
		members = new ArrayList<>();
		documents = new ArrayList<>();
		resources = new ArrayList<>();
	}

	public String getProjectName() {
		return projectName;
	}

	public void setProjectName(String projectName) {
		this.projectName = projectName;
	}

	public String getProjectDescription() {
		return projectDescription;
	}

	public void setProjectDescription(String projectDescription) {
		this.projectDescription = projectDescription;
	}

	private transient Project project;					// Holds the current project instance to be created
	private transient List<Task> tasks;					// Holds current Tasks to be created
	private transient List<User> members;					// A list of all Members (Users) for the project to be created.
	private transient List<DocumentInfo> documents;		// A list of all documents relevant to the project
	private transient  List<Resource> resources;			// A list of all documents relevant to the project
	private transient  String managerUserName;				// The name of the User which will act as project lead.

	private String userToAdd;					// Current username to add to the project (selected by autocomplete)
	private String documentToAdd;				// Current DocumentInfo-Object to add to the project.
	private String resourceToAdd;				// Current Resource-Object to add to the project.
	private String taskNameToAdd;				// Name of the task to add

	private String projectName;					// Project Name
	private String projectDescription;			// Project description
	private Date projectDueDate;				// Project due date

	public Date getProjectDueDate() {
		return projectDueDate;
	}

	public void setProjectDueDate(Date projectDueDate) {
		this.projectDueDate = projectDueDate;
	}

	public String getTaskNameToAdd() {
		return taskNameToAdd;
	}

	public String getTaskDescriptionToAdd() {
		return taskDescriptionToAdd;
	}

	private String taskDescriptionToAdd;		// Descriptin off the task to add.

	public void setTaskNameToAdd(String taskNameToAdd) {
		this.taskNameToAdd = taskNameToAdd;
	}

	public void setTaskDescriptionToAdd(String taskDescriptionToAdd) {
		this.taskDescriptionToAdd = taskDescriptionToAdd;
	}

	public String getResourceToAdd() {
		return resourceToAdd;
	}

	public void setResourceToAdd(String resourceToAdd) {
		this.resourceToAdd = resourceToAdd;
	}

	public List<DocumentInfo> getDocuments() {
		return documents;
	}

	public void setDocuments(List<DocumentInfo> documents) {
		this.documents = documents;
	}

	public String getDocumentToAdd() {
		return documentToAdd;
	}

	public void setDocumentToAdd(String documentToAdd) {
		this.documentToAdd = documentToAdd;
	}

	public String getManagerUserName() {
		return managerUserName;
	}

	public void setManagerUserName(String managerName) {
		this.managerUserName = managerName;
	}

	public Project getProject() {
		return project;
	}

	public void setProject(Project project) {
		this.project = project;
	}

	public List<Task> getTasks() {
		return tasks;
	}

	public void setTasks(List<Task> tasks) {
		this.tasks = tasks;
	}

	public List<User> getMembers() {
		return members;
	}

	public void setMembers(List<User> members) {
		this.members = members;
	}

	public String getUserToAdd() {
		return userToAdd;
	}

	public void setUserToAdd(String userToAdd) {
		this.userToAdd = userToAdd;
	}

	public void save() {

		// Create project object with mandatory data.
		this.project = new Project();
		project.setName(projectName);
		project.setDescription(projectDescription);
		project.setDocuments(this.documents);
		project.setResources(this.resources);
		User projectLead = userRoleController.findUserByUsername(managerUserName, sessionController.getUser());
		project.setManager(projectLead);
		members.add(projectLead);
		project.setDueDate(this.projectDueDate);
		project.setBeginDate(new Date());
		project.setUsers(members);

		// Create blackboard for project
		Blackboard blackBoard = new Blackboard();
		blackBoard.setTitle(project.getName());
		blackBoard.setDescription(project.getDescription());
		blackBoard.setFrozen(false);
		blackBoard.setTasks(tasks);

		// Create all subdata (ProjectGoal etc..) and persist project
		projectController.createProject(project, blackBoard);

		clearInputData();

		listProjectsBean.loadProjects();
		
		ExternalContext context = FacesContext.getCurrentInstance().getExternalContext();
		try {
			context.redirect(context.getApplicationContextPath() + "/client/projects/projects.xhtml");
		} catch (IOException e) {
			Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, e.getMessage());
		}
	}

	private void clearInputData() {
		this.projectDescription = "";
		this.projectName = "";
		this.projectDueDate = new Date();
		this.managerUserName = "";
		this.userToAdd = "";
		this.documents = new ArrayList<>();
		this.resources = new ArrayList<>();
		this.members = new ArrayList<>();
		this.tasks = new ArrayList<>();
	}

	public void addUser() {
		if (userToAdd != null && userToAdd.length() > 0) {
			User user = userRoleController.findUserByUsername(userToAdd, AuthorizationController.getSystemUser());
			if ((user != null)) {
				this.members.add(user);
			}
		}
	}

	public void removeUser(User user) {
		this.members.remove(user);
	}

	public void addDocument() {
		if (documentToAdd != null) {
			DocumentInfo docInfo = documentController.getAllDocumentInfos(AuthorizationController.getSystemUser()).get(0);
			if ((docInfo != null)) {
				this.documents.add(docInfo);
			}
		}
	}

	public void removeResource(Resource res) {
		this.resources.remove(res);
	}

	public void addResource() {
		if (resourceToAdd != null) {
			// TODO: Make a real selection of resourrces!
			Resource res = resourceController.getAllResources(AuthorizationController.getSystemUser()).get(0);
			if ((res != null)) {
				this.resources.add(res);
			}
		}
	}

	public void addTask() {
		Task t = new Task();
		t.setName(taskNameToAdd);
		t.setDescription(taskDescriptionToAdd);
		t.setTaskStatus(TaskStatus.CREATED);
		t.setPriority(1);

		Task managedTask = taskController.createTask(t);
		this.tasks.add(managedTask);

	}

	public void removeDocument(DocumentInfo docInfo) {
		this.documents.remove(docInfo);
	}

	public void removeTask(Task task) {
		this.tasks.remove(task);
	}

	public String onFlowProcess(FlowEvent event) {
		return event.getNewStep();
	}

	/**
	 * AutoComplete method for Users
	 * @param query
	 * @return
	 */
	public List<String> completeUserList(String query) {
		List<String> users = userRoleController.getAllUserNames(sessionController.getUser());
		List<String> result = new ArrayList<>();
		for (String username : users) {
			if (username.startsWith(query)) {
				result.add(username);
			}
		}
		return result;
	}

	/**
	 * AutoComplete method for Role
	 * @param query
	 * @return
	 */
	public List<String> completeRolesList(String query) {
		List<Role> availableRoles = userRoleController.getAllRoles(sessionController.getUser());
		List<String> roles = new ArrayList<>();
		for (Role r : availableRoles) {
			if (r.getName().startsWith(query)) {
				roles.add(r.getName());
			}
		}
		return roles;
	}

	/**
	 * Autocomplete method for documents.
	 * @param query
	 * @return
	 */
	public List<String> completeDocumentList(String query) {
		List<DocumentInfo> docInfos = documentController.getAllDocumentInfos(sessionController.getUser());
		List<String> result = new ArrayList<>();
		for (DocumentInfo docInfo : docInfos) {
			if (docInfo.getCurrentDocument().getName().startsWith(query)) {
				result.add(docInfo.getCurrentDocument().getName());
			}
		}
		return result;
	}

	/**
	 * Autocomplete method for resources.
	 * @param query
	 * @return
	 */
	public List<String> completeResourcesList(String query) {
		List<Resource> allResources = resourceController.getAllResources(sessionController.getUser());
		List<String> result = new ArrayList<>();
		for (Resource res : allResources) {
			if (res.getName().startsWith(query)) {
				result.add(res.getName());
			}
		}
		return result;
	}

}