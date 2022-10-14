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
package de.hallerweb.enterprise.prioritize.view.project;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.enterprise.context.SessionScoped;
import javax.inject.Inject;
import javax.inject.Named;

import org.primefaces.PrimeFaces;

import org.primefaces.event.SelectEvent;
import org.primefaces.model.SelectableDataModel;

import de.hallerweb.enterprise.prioritize.controller.project.ProjectController;
import de.hallerweb.enterprise.prioritize.controller.project.task.BlackboardController;
import de.hallerweb.enterprise.prioritize.controller.project.task.TaskController;
import de.hallerweb.enterprise.prioritize.controller.security.SessionController;
import de.hallerweb.enterprise.prioritize.controller.security.UserRoleController;
import de.hallerweb.enterprise.prioritize.model.project.Project;
import de.hallerweb.enterprise.prioritize.model.project.task.Blackboard;
import de.hallerweb.enterprise.prioritize.model.project.task.Task;
import de.hallerweb.enterprise.prioritize.model.project.task.TaskStatus;
import de.hallerweb.enterprise.prioritize.model.security.User;

@Named
@SessionScoped
public class ListProjectsBean implements Serializable, SelectableDataModel {

	@EJB
	ProjectController projectController;
	@EJB
	TaskController taskController;
	@EJB
	BlackboardController blackboardController;
	@EJB
	UserRoleController userRoleController;
	@Inject
	SessionController sessionController;

	private transient List<Project> projects;
	private transient List<Task> notMyTasks;
	private transient List<Task> myTasks;
	private transient Project currentProject;		// Currently selected Project
	private transient Task selectedTask;
	private transient User currentUser;

	private static final String NAVIGATION_EDITPROJECT = "editproject";
	private static final String NAVIGATION_BLACKBOARD = "blackboard";
	private static final String NAVIGATION_TASKLIST = "tasklist";

	public Project getCurrentProject() {
		return currentProject;
	}

	public void setCurrentProject(Project currentProject) {
		this.currentProject = currentProject;
	}

	@PostConstruct
	public void init() {
		this.currentUser = sessionController.getUser();
		loadProjects();
		loadForeignTasks();
		loadMyTasks();
		if (selectedTask == null && !myTasks.isEmpty()) {
			this.selectedTask = myTasks.get(0);
		}
	}

	public void loadProjects() {
		if (currentUser != null) {
			// TODO: Project admin must also be project member!
			this.projects = projectController.findProjectsByUser(currentUser);
		} else {
			this.projects = new ArrayList<>();
		}
	}

	public void loadForeignTasks() {
		notMyTasks = taskController.findTasksNotAssignedToUser(currentUser, currentProject);
		if (notMyTasks == null || notMyTasks.isEmpty()) {
			notMyTasks = new ArrayList<>();
		}
	}

	public void loadMyTasks() {
		myTasks = taskController.findTasksAssignedToUser(currentUser, currentProject);
		if (myTasks == null || myTasks.isEmpty()) {
			myTasks = new ArrayList<>();
		}
	}

	public List<Project> getProjects() {
		loadProjects();
		return this.projects;
	}

	public void setProjects(List<Project> projects) {
		this.projects = projects;
	}

	public String editProject() {
		return NAVIGATION_EDITPROJECT;
	}

	public String showTasks(Project project) {
		setCurrentProject(project);
		return "tasks";
	}

	public String showBlackboard(Project project) {
		setCurrentProject(project);
		return NAVIGATION_BLACKBOARD;
	}

	public List<Task> getForeignTasks() {
		loadForeignTasks();
		return notMyTasks;
	}

	public List<Task> getMyTasksForCurrentProject() {
		loadMyTasks();
		if (selectedTask == null && !myTasks.isEmpty()) {
			this.selectedTask = myTasks.get(0);
		}
		return myTasks;
	}

	public List<Task> getMyTasks() {
		return taskController.findTasksByAssignee(sessionController.getUser());
	}

	public Set<Task> getProjectTasks(Project pr) {
		Project project = projectController.findProjectById(pr.getId());
		Blackboard board = project.getBlackboard();

		TreeSet<Task> determinedTasks = new TreeSet<>(blackboardController.getBlackboardTasks(board));
		if (!determinedTasks.isEmpty()) {
			return determinedTasks;
		} else {
			return new TreeSet<>();
		}
	}

	public List<Task> getBlackboardTasks(Project pr) {
		Project project = projectController.findProjectById(pr.getId());
		Blackboard board = project.getBlackboard();
		return board.getTasks();
	}

	public String assignTaskToUser(Task task) {

		Task managedTask = taskController.findTaskById(task.getId());

		taskController.updateTaskStatus(managedTask.getId(), TaskStatus.ASSIGNED);
		taskController.setTaskAssignee(managedTask, currentUser);
		userRoleController.assignTask(currentUser, managedTask);

		loadForeignTasks();

		return NAVIGATION_BLACKBOARD;
	}

	public String unassignTask(Task task) {
		Task managedTask = taskController.findTaskById(task.getId());
		User user = sessionController.getUser();
		taskController.updateTaskStatus(managedTask.getId(), TaskStatus.OPEN);
		taskController.removeTaskAssignee(managedTask.getId());
		userRoleController.removeAssignedTask(user, managedTask, user);
		loadForeignTasks();
		return NAVIGATION_BLACKBOARD;
	}

	public String unassignTaskFromTasklist(Task t) {
		unassignTask(t);
		return NAVIGATION_TASKLIST;
	}

	public String resolveTask(Task task) {
		taskController.resolveTask(task, sessionController.getUser());
		return NAVIGATION_BLACKBOARD;
	}

	public String resolveTaskFromTasklist(Task task) {
		taskController.resolveTask(task, sessionController.getUser());
		return NAVIGATION_TASKLIST;
	}

	public String setTaskProgress(Task task, int percentage) {
		taskController.setTaskProgress(task, sessionController.getUser(), percentage);
		return NAVIGATION_BLACKBOARD;
	}

	public Task getSelectedTask() {
		return selectedTask;
	}

	public void setSelectedTask(Task task) {
		this.selectedTask = task;
	}

	public void onTaskSelected(SelectEvent event) {
		setSelectedTask((Task) event.getObject());
	}

	@Override
	public Object getRowData(String arg0) {
		return taskController.findTaskById(Integer.parseInt(arg0));
	}

	@Override
	public String getRowKey(Object arg0) {
		return String.valueOf(((Task) arg0).getId());
	}

	public void showTimeTracker() {
		PrimeFaces.current().dialog().openDynamic("admin/timetracker");
		//Was deprecated:RequestContext.getCurrentInstance().openDialog("admin/timetracker");
	}

}
