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

	@GET
	@Path("/uuid/{uuid}")
	@Produces(MediaType.APPLICATION_JSON)
	/**
	 * @param uuid - UUID
	 * @param apiKey - API-Key
	 * @return TimeTracker
	 */
	public TimeTracker getTimeTracker(@PathParam(value = "uuid") String uuid, @QueryParam(value = "apiKey") String apiKey) {
		User sessionUser = accessController.checkApiKey(apiKey);
		if (sessionUser != null) {
			return timeTrackerController.getTimeTracker(uuid, sessionUser);
		} else {
			throw new NotAuthorizedException(Response.serverError());
		}
	}

	@POST
	@Path("/create")
	@Produces(MediaType.APPLICATION_JSON)
	/**
	 * 
	 * @param apiKey - API-Key
	 * @return TimeTracker
	 */
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

	@POST
	@Path("/toggle")
	public void toggleTimeTracker(@QueryParam(value = "apiKey") String apiKey, @FormParam(value = "uuid") String uuid) {
		User user = accessController.checkApiKey(apiKey);
		timeTrackerController.toggle(uuid, user);
	}
}
