/**
 * 
 */
package de.hallerweb.enterprise.prioritize.view.boundary;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.ejb.EJB;
import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.NotAuthorizedException;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import de.hallerweb.enterprise.prioritize.controller.project.ProjectController;
import de.hallerweb.enterprise.prioritize.controller.project.task.TaskController;
import de.hallerweb.enterprise.prioritize.controller.security.AuthorizationController;
import de.hallerweb.enterprise.prioritize.controller.security.RestAccessController;
import de.hallerweb.enterprise.prioritize.controller.security.SessionController;
import de.hallerweb.enterprise.prioritize.controller.security.UserRoleController;
import de.hallerweb.enterprise.prioritize.model.project.Project;
import de.hallerweb.enterprise.prioritize.model.project.task.PActor;
import de.hallerweb.enterprise.prioritize.model.project.task.Task;
import de.hallerweb.enterprise.prioritize.model.project.task.TaskStatus;
import de.hallerweb.enterprise.prioritize.model.security.Role;
import de.hallerweb.enterprise.prioritize.model.security.User;

/**
 * 
 * <p>
 * Copyright: (c) 2015
 * </p>
 * <p>
 * Peter Haller
 * </p>
 * 
 * @author peter REST-Service to create, update and delete {@link Project} and {@link Task} 
 *         objects.
 */
@RequestScoped
@Path("projects")
public class ProjectService {

	@EJB
	RestAccessController accessController;

	@EJB
	UserRoleController userRoleController;

	@EJB
	ProjectController projectController;

	@EJB
	TaskController taskController;

	@Inject
	SessionController sessionController;

	@EJB
	AuthorizationController authController;

	/**
	 * Returns all projects
	 * 
	 * @return JSON object with all projects.
	 */
	@GET
	@Path("/")
	@Produces(MediaType.APPLICATION_JSON)
	public List<Project> getProjects(@QueryParam(value = "apiKey") String apiKey) {
		User user = accessController.checkApiKey(apiKey);
		List<Project> projects = new ArrayList<Project>();
		if (user != null) {
			Set<Role> roles = user.getRoles();
			for (Role r : roles) {
				projects.addAll(projectController.findProjectsByManagerRole(r.getId()));
			}
			return projects;
		} else {
			throw new NotAuthorizedException(Response.serverError());
		}
	}

	/**
	 * Returns all tasks of a project
	 * 
	 * @return JSON object with all tasks of a projects.
	 */
	@GET
	@Path("/{projectId}/tasks")
	@Produces(MediaType.APPLICATION_JSON)
	public List<Task> getProjectTasks(@PathParam(value = "projectId") String projectId, @QueryParam(value = "apiKey") String apiKey) {

		User user = accessController.checkApiKey(apiKey);
		if (user != null) {
			Project project = projectController.findProjectById(Integer.valueOf(projectId));
			List<Task> tasks = project.getBlackboard().getTasks();
			if (tasks != null && !tasks.isEmpty()) {
				return tasks;
			} else {
				return new ArrayList<Task>();
			}
		} else {
			throw new NotAuthorizedException(Response.serverError());
		}
	}

	/**
	 * Edit a task
	 * 
	 * @return JSON object with edited task.
	 */
	@POST
	@Path("/tasks/{taskId}/edit")
	@Produces(MediaType.APPLICATION_JSON)
	public Task assignTask(@PathParam(value = "taskId") String taskId, @QueryParam(value = "apiKey") String apiKey,
			@FormParam(value = "assignee") String assigneeId, @FormParam(value = "percentage") String percentage,
			@FormParam(value = "status") String status) {
		System.out.println("-------------- ASIGNEE: --- " + assigneeId);

		User user = accessController.checkApiKey(apiKey);
		if (user != null) {
			Task task = taskController.findTaskById(Integer.parseInt(taskId));
			if (task != null) {
				return editTask(task, assigneeId, percentage, status);
			} else {
				throw new NotFoundException();
			}
		} else {
			throw new NotAuthorizedException(Response.serverError());
		}
	}

	private Task editTask(Task task, String assigneeId, String percentage, String status) {
		if (assigneeId != null) {
			User assignee = userRoleController.findUserById(Integer.valueOf(assigneeId));
			taskController.addTaskAssignee(task.getId(), assignee);
			if (percentage != null) {
				taskController.setTaskProgress(task, assignee, Integer.valueOf(percentage));
			}
		} else {
			task.setAssignees(new ArrayList<PActor>());
			setTaskStatus(task,status);
			taskController.editTask(task.getId(),task);
		}
		setTaskStatus(task, status);
		return task;
	}

	private void setTaskStatus(Task task, String status) {
		if (status != null) {
			switch (status.toUpperCase()) {
			case "ASSIGNED":
				taskController.updateTaskStatus(task.getId(),TaskStatus.ASSIGNED);
				break;
			case "CANCELLED":
				task.setTaskStatus(TaskStatus.CANCELLED);
				break;
			case "CLOSED":
				task.setTaskStatus(TaskStatus.CLOSED);
				break;
			case "CREATED":
				task.setTaskStatus(TaskStatus.CREATED);
				break;
			case "ESTIMATED":
				task.setTaskStatus(TaskStatus.ESTIMATED);
				break;
			case "FINISHED":
				task.setTaskStatus(TaskStatus.FINISHED);
				break;
			case "OPEN":
				task.setTaskStatus(TaskStatus.OPEN);
				break;
			case "STARTED":
				task.setTaskStatus(TaskStatus.STARTED);
				break;
			case "STOPPED":
				task.setTaskStatus(TaskStatus.STOPPED);
				break;
			default:
				break;
			}
		}
	}

}
