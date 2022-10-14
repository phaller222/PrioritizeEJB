package de.hallerweb.enterprise.prioritize.view.boundary;

import de.hallerweb.enterprise.prioritize.controller.project.ProjectController;
import de.hallerweb.enterprise.prioritize.controller.project.task.BlackboardController;
import de.hallerweb.enterprise.prioritize.controller.project.task.TaskController;
import de.hallerweb.enterprise.prioritize.controller.security.AuthorizationController;
import de.hallerweb.enterprise.prioritize.controller.security.RestAccessController;
import de.hallerweb.enterprise.prioritize.controller.security.SessionController;
import de.hallerweb.enterprise.prioritize.controller.security.UserRoleController;
import de.hallerweb.enterprise.prioritize.model.project.Project;
import de.hallerweb.enterprise.prioritize.model.project.task.Blackboard;
import de.hallerweb.enterprise.prioritize.model.project.task.Task;
import de.hallerweb.enterprise.prioritize.model.project.task.TaskStatus;
import de.hallerweb.enterprise.prioritize.model.security.User;

import javax.ejb.EJB;
import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

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
@Path("v1/projects")
public class ProjectService {

	@EJB
	RestAccessController accessController;

	@EJB
	UserRoleController userRoleController;

	@EJB
	ProjectController projectController;
	
	@EJB
	BlackboardController blackboardController;

	@EJB
	TaskController taskController;

	@Inject
	SessionController sessionController;

	@EJB
	AuthorizationController authController;

	/**
	 * Returns all projects the current user is project lead
	 *
	 * @api {get} /projects getProjects
	 * @apiName getProjects
	 * @apiGroup /projects
	 * @apiDescription Returns all projects the current user is project lead
	 * @apiParam {String} apiKey The API-Key of the user accessing the service.
	 * @apiSuccess {String} JSON Array projects found
	 * @apiSuccessExample Success-Response:
	 *     HTTP/1.1 200 OK
	 *[
	 *{
	 *"id": 51,
	 *"manager": {
	 *"id": 45,
	 *"name": "admin",
	 *"username": "admin",
	 *"address": null
	 *},
	 *"name": "aaa",
	 *"description": "aaa",
	 *"beginDate": 1588525293108,
	 *"dueDate": 1590357600000,
	 *"maxManDays": 0,
	 *"priority": 0,
	 *"users": [
	 *	{
	 *"id": 45,
	 *"name": "admin",
	 *"username": "admin",
	 *"address": null
	 *	}
	 *],
	 *"progress": {
	 *"id": 57,
	 *"targetGoals": [
	 *	{
	 *"id": 55,
	 *"task": {
	 *"id": 49,
	 *"priority": 1,
	 *"name": "aaa",
	 *"description": "aaa",
	 *"taskStatus": "CREATED",
	 *"assignee": null,
	 *"timeSpent": []
	 *},
	 *"projectGoal": {
	 *"id": 52,
	 *"name": "aaa",
	 *"description": "aaa",
	 *"category": null,
	 *"properties": [
	*{
	 *"id": 53,
	*"name": "Task completeness)",
	 *"description": "Indicates the percentage value of completeness of a task.",
	 *"min": 0.0,
	 *"max": 100.0,
	 *"tempValue": 0.0
	 *	}
	 *	]
	 *},
	 *"propertyRecord": {
	 *"id": 56,
	 *"property": {
	 *"id": 53,
	 *"name": "Task completeness)",
	 *"description": "Indicates the percentage value of completeness of a task.",
	 *"min": 0.0,
	 *"max": 100.0,
	 *"tempValue": 0.0
	 *},
	 *"value": 0.0,
	 *"documentInfo": null,
	 *"documentPropertyRecord": false,
	 *"numericPropertyRecord": true
	 *},
	 *"percentage": 0
	 *}
	 *],
	 *"progress": 0
	 *	}
	 *}
	*]
	 *
	 * @apiError NotAuthorized  APIKey incorrect.
	 *
	 *
	 */
	@GET
	@Path("/")
	@Produces(MediaType.APPLICATION_JSON)
	public List<Project> getProjects(@QueryParam(value = "apiKey") String apiKey) {
		User user = accessController.checkApiKey(apiKey);
		if (user != null) {
			return new ArrayList<>(projectController.findProjectsByManager(user.getId()));
		} else {
			throw new NotAuthorizedException(Response.serverError());
		}
	}

	/**
	 * Returns all tasks of the given project
	 *
	 * @api {get} /{projectId}/tasks getProjectTasks
	 * @apiName getProjectTasks
	 * @apiGroup /projects
	 * @apiDescription Returns all tasks for the project with the given id
	 * @apiParam {String} apiKey The API-Key of the user accessing the service.
	 * @apiParam {String} projectId ID of the project to retrieve the tasks from.
	 * @apiSuccess {String} JSON Array projects found
	 * @apiSuccessExample Success-Response:
	 *     HTTP/1.1 200 OK
	 *[
	 *{
	 *"id": 49,
	 *"priority": 1,
	 *"name": "aaa",
	 *"description": "aaa",
	 *"taskStatus": "CREATED",
	 *"assignee": null,
	 *"timeSpent": []
	 *}
	 *]
	 */
	@GET
	@Path("/{projectId}/tasks")
	@Produces(MediaType.APPLICATION_JSON)
	public List<Task> getProjectTasks(@PathParam(value = "projectId") String projectId, @QueryParam(value = "apiKey") String apiKey) {

		User user = accessController.checkApiKey(apiKey);
		if (user != null) {
			Project project;
			try {
				project = projectController.findProjectById(Integer.parseInt(projectId));
			} catch (Exception ex) {
				return new ArrayList<>();
			}
			Blackboard bb = project.getBlackboard();
			List<Task> tasks = blackboardController.getBlackboardTasks(bb);
			if (tasks != null && !tasks.isEmpty()) {
				return tasks;
			} else {
				return new ArrayList<>();
			}
		} else {
			throw new NotAuthorizedException(Response.serverError());
		}
	}
	/**
	 * Assignes a task, sets it's status and percentage complete
	 *
	 * @api {post} /tasks/{taskId}/edit assignTask
	 * @apiName assignTask
	 * @apiGroup /projects
	 * @apiDescription Assignes a task, sets it's status and percentage complete
	 * @apiParam {String} apiKey The API-Key of the user accessing the service.
	 * @apiParam {String} status The state of the task (ASSIGNED / CREATED / FINISHED...)
	 * @apiParam {Integer} assignee The id of the user the task should be assigned to.
	 * @apiParam {Integer} percentage The percentage complete value for the task.
	 * @apiSuccess {String} JSON Array with the task
	 * @apiSuccessExample Success-Response:
	 *     HTTP/1.1 200 OK
	 *[
	 *{
	 *"id": 49,
	 *"priority": 1,
	 *"name": "aaa",
	 *"description": "aaa",
	 *"taskStatus": "CREATED",
	 *"assignee": null,
	 *"timeSpent": []
	 *}
	 *]
	 */
	@POST
	@Path("/tasks/{taskId}/edit")
	@Produces(MediaType.APPLICATION_JSON)
	public Task assignTask(@PathParam(value = "taskId") String taskId, @QueryParam(value = "apiKey") String apiKey,
			@FormParam(value = "assignee") String assigneeId, @FormParam(value = "percentage") String percentage,
			@FormParam(value = "status") String status) {
		Logger.getLogger(this.getClass().getName()).log(Level.INFO, "-------------- ASIGNEE: --- " + assigneeId);
		User user = accessController.checkApiKey(apiKey);
		if (user != null) {
			Task task = taskController.findTaskById(Integer.parseInt(taskId));
			if (task != null) {
				return editTask(task, assigneeId, percentage, status, user);
			} else {
				throw new NotFoundException();
			}
		} else {
			throw new NotAuthorizedException(Response.serverError());
		}
	}

	private Task editTask(Task task, String assigneeId, String percentage, String status, User sessionUser) {
		if (assigneeId != null) {
			User assignee = userRoleController.findUserById(Integer.parseInt(assigneeId), sessionUser);
			Task managedTask = taskController.findTaskById(task.getId());
			taskController.setTaskAssignee(managedTask, assignee);
			if (percentage != null) {
				taskController.setTaskProgress(task, assignee, Integer.parseInt(percentage));
			}
		} else {
			setTaskStatus(task, status);
			taskController.editTask(task.getId(), task);
		}
		setTaskStatus(task, status);
		return task;
	}

	private void setTaskStatus(Task task, String status) {
		if (status != null) {
			switch (status.toUpperCase()) {
			case "ASSIGNED":
				taskController.updateTaskStatus(task.getId(), TaskStatus.ASSIGNED);
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
