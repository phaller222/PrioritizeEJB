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
package de.hallerweb.enterprise.prioritize.view.boundary;

import javax.ejb.EJB;
import javax.enterprise.context.RequestScoped;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.NotAuthorizedException;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import de.hallerweb.enterprise.prioritize.controller.project.task.TaskController;
import de.hallerweb.enterprise.prioritize.controller.project.task.TimeTrackerController;
import de.hallerweb.enterprise.prioritize.controller.security.RestAccessController;
import de.hallerweb.enterprise.prioritize.controller.security.SessionController;
import de.hallerweb.enterprise.prioritize.model.project.task.Task;
import de.hallerweb.enterprise.prioritize.model.project.task.TimeTracker;
import de.hallerweb.enterprise.prioritize.model.security.User;

@RequestScoped
@Path("v1/timetrackers")
public class TimeTrackerService {

	@EJB
	RestAccessController accessController;
	@EJB
	SessionController sessionController;
	@EJB
	TimeTrackerController timeTrackerController;
	@EJB
	TaskController taskController;


	/**
	 * Gets the timetracker with the given UUID
	 * @api {get} /uuid/{uuid} getTimeTracker
	 * @apiName getTimeTracker
	 * @apiGroup /timetrackers
	 * @apiDescription  Gets the timetracker with the given UUID
	 * @apiParam {String} apiKey The API-Key of the user accessing the service.
	 * @apiParam {String} uuid The uuid of the timetracker.
	 * @apiSuccess {TimeTracker} TimeTracker object
	 * @apiSuccessExample Success-Response:
	 *     HTTP/1.1 200 OK
	 *
	 * @apiError NotAuthorized  APIKey incorrect.
	 */
	@GET
	@Path("/uuid/{uuid}")
	@Produces(MediaType.APPLICATION_JSON)
	public TimeTracker getTimeTracker(@PathParam(value = "uuid") String uuid, @QueryParam(value = "apiKey") String apiKey) {
		User sessionUser = accessController.checkApiKey(apiKey);
		if (sessionUser != null) {
			return timeTrackerController.getTimeTracker(uuid, sessionUser);
		} else {
			throw new NotAuthorizedException(Response.serverError());
		}
	}


	/**
	 * Creates a timetracker
	 * @api {post} /create createTimeTracker
	 * @apiName createTimeTracker
	 * @apiGroup /timetrackers
	 * @apiDescription  Creates a timetracker
	 * @apiParam {String} apiKey The API-Key of the user accessing the service.
	 * @apiParam {String} uuid The uuid to assign to the timetracker.
	 * @apiParam {Long} The id of the task the timetracker shall track.
	 * @apiSuccess {TimeTracker}  TimeTracker object
	 * @apiSuccessExample Success-Response:
	 *     HTTP/1.1 200 OK
	 *
	 * @apiError NotAuthorized  APIKey incorrect.
	 */
	@POST
	@Path("/create")
	@Produces(MediaType.APPLICATION_JSON)
	public TimeTracker createTimeTracker(@QueryParam(value = "apiKey") String apiKey, @FormParam(value = "uuid") String uuid,
			@FormParam(value = "taskId") String taskId) {
		User user = accessController.checkApiKey(apiKey);
		if (user != null) {
			Task task = taskController.findTaskById(Integer.parseInt(taskId));
			return timeTrackerController.createTimeTracker(uuid, task, user);
		} else {
			throw new NotAuthorizedException(Response.serverError());
		}
	}

	/**
	 * Toggles a timetracker (run - pause/stop)
	 * @api {post} /create toggleTimeTracker
	 * @apiName toggleTimeTracker
	 * @apiGroup /timetrackers
	 * @apiDescription  Toggles a timetracker (run - pause/stop)
	 * @apiParam {String} apiKey The API-Key of the user accessing the service.
	 * @apiParam {String} uuid The uuid of the timetracker to start or stop (toggle).
	 * @apiSuccess void
	 * @apiSuccessExample Success-Response:
	 *     HTTP/1.1 200 OK
	 *
	 * @apiError NotAuthorized  APIKey incorrect.
	 */
	@POST
	@Path("/toggle")
	public void toggleTimeTracker(@QueryParam(value = "apiKey") String apiKey, @FormParam(value = "uuid") String uuid) {
		User user = accessController.checkApiKey(apiKey);
		timeTrackerController.toggle(uuid, user);
	}
}
