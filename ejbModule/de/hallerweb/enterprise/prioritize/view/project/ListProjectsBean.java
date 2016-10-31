package de.hallerweb.enterprise.prioritize.view.project;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.enterprise.context.SessionScoped;
import javax.inject.Inject;
import javax.inject.Named;

import org.primefaces.model.SelectableDataModel;

import de.hallerweb.enterprise.prioritize.controller.project.ProjectController;
import de.hallerweb.enterprise.prioritize.controller.project.task.TaskController;
import de.hallerweb.enterprise.prioritize.controller.security.SessionController;
import de.hallerweb.enterprise.prioritize.controller.security.UserRoleController;
import de.hallerweb.enterprise.prioritize.model.project.Project;
import de.hallerweb.enterprise.prioritize.model.project.goal.ProjectGoalRecord;
import de.hallerweb.enterprise.prioritize.model.project.task.Task;
import de.hallerweb.enterprise.prioritize.model.project.task.TaskStatus;
import de.hallerweb.enterprise.prioritize.model.security.Role;
import de.hallerweb.enterprise.prioritize.model.security.User;

@Named
@SessionScoped
public class ListProjectsBean implements Serializable, SelectableDataModel {

	@EJB
	ProjectController projectController;
	@EJB
	TaskController taskController;
	@EJB
	UserRoleController userRoleController;

	@Inject
	SessionController sessionController;

	private List<Project> projects;

	private Project currentProject;		// Currently selected Project
	
	private Task selectedTask;

	public Project getCurrentProject() {
		return currentProject;
	}

	public void setCurrentProject(Project currentProject) {
		this.currentProject = currentProject;
	}

	@PostConstruct
	public void init() {
		
	}

	public List<Project> getProjects() {
		this.projects = new ArrayList<Project>();
		User sessionUser = sessionController.getUser();
		if (sessionUser != null) {
			this.projects.addAll(getProjectsForUser(sessionUser.getId()));
			for (Role r : sessionUser.getRoles()) {
				List<Project> managerProjects = getProjectsByManagerRole(r.getId());
				for (Project p : managerProjects) {
					if (!p.getUsers().contains(sessionUser)) {
						this.projects.add(p);
					}
				}
			}
		}
		Collections.sort(projects, (projectA, projectB) -> projectA.getName().compareTo(projectB.getName()));
		return projects;
	}

	public void setProjects(List<Project> projects) {
		this.projects = projects;
	}

	private List<Project> getProjectsByManagerRole(int roleId) {
		List<Project> projects = projectController.findProjectsByManagerRole(roleId);
		if (projects != null && !projects.isEmpty()) {
			return projects;
		} else {
			return new ArrayList<Project>();
		}
	}

	private List<Project> getProjectsForUser(int userId) {
		List<Project> projects = projectController.findProjectsByUser(userId);
		if (projects != null && !projects.isEmpty()) {
			return projects;
		} else {
			return new ArrayList<Project>();
		}
	}

	public String editProject() {
		return "editproject";
	}

	public String showTasks(Project project) {
		setCurrentProject(project);
		return "tasks";
	}

	public String showBlackboard(Project project) {
		setCurrentProject(project);
		return "blackboard";
	}

	public List<Task> getForeignTasks() {
		List<Task> foreignTasks = new ArrayList<Task>();
		List<Task> notMyTasks = taskController.findTasksNotAssignedToUser(sessionController.getUser());// taskController.findTasksByAssignee(sessionController.getUser());

		for (Task t : currentProject.getBlackboard().getTasks()) {
			for (Task myTask : notMyTasks) {
				if (t.getId() == myTask.getId()) {
					foreignTasks.add(t);
				}
			}
		}
		if (foreignTasks.isEmpty()) {
			foreignTasks = currentProject.getBlackboard().getTasks();
		}
		return notMyTasks;
	}

	public List<Task> getForeignTasks2() {
		User user = sessionController.getUser();
		List<Task> foreignTasks = new ArrayList<Task>();
		List<Task> notMyTasks = taskController.findTasksNotAssignedToUser(sessionController.getUser());// taskController.findTasksByAssignee(sessionController.getUser());

		for (Task t : currentProject.getBlackboard().getTasks()) {
			if (notMyTasks.contains(t)) {
				foreignTasks.add(t);
			}
		}
		if (foreignTasks.isEmpty()) {
			foreignTasks = currentProject.getBlackboard().getTasks();
		}
		return notMyTasks;
	}

	public List<Task> getMyTasks() {
		return taskController.findTasksByAssignee(sessionController.getUser());
	}

	public String assignTaskToUser(Task task) {

		Task managedTask = taskController.findTaskById(task.getId());
		User user = sessionController.getUser();

		taskController.updateTaskStatus(managedTask.getId(), TaskStatus.ASSIGNED);
		taskController.addTaskAssignee(managedTask.getId(), user);

		userRoleController.assignTask(user, managedTask);

		return "blackboard";
	}

	public String unassignTask(Task task) {
		Task managedTask = taskController.findTaskById(task.getId());
		User user = sessionController.getUser();
		taskController.updateTaskStatus(managedTask.getId(), TaskStatus.OPEN);
		taskController.removeTaskAssignee(managedTask.getId(), user);
		userRoleController.removeAssignedTask(user, managedTask);
		return "blackboard";
	}
	
	public String resolveTask(Task task) {
//		Task managedTask = taskController.findTaskById(task.getId());
//		ProjectGoalRecord rec = projectController.findProjectGoalRecordById(managedTask.getProjectGoalRecord().getId());
//		rec.setPercentage(100);
//		
//		//projectController.updateProjectProgress(rec.getProject().getId());
//		
//		User user = sessionController.getUser();
//		taskController.removeTaskAssignee(managedTask.getId(), user);
//		userRoleController.removeAssignedTask(user, managedTask);
//		taskController.updateTaskStatus(managedTask.getId(), TaskStatus.FINISHED);
		taskController.resolveTask(task, sessionController.getUser());
		return "blackboard";
	}
	
	public Task getSelectedTask() {
		return selectedTask;
	}
	
	public void setSelectedTask(Task t) {
		this.selectedTask = t;
	}

	@Override
	public Object getRowData(String arg0) {
		return taskController.findTaskById(Integer.valueOf(arg0));
	}

	@Override
	public Object getRowKey(Object arg0) {
		return String.valueOf(((Task)arg0).getId());
	}

}
