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
import javax.ws.rs.GET;
import javax.ws.rs.NotAuthorizedException;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import de.hallerweb.enterprise.prioritize.controller.CompanyController;
import de.hallerweb.enterprise.prioritize.controller.search.SearchController;
import de.hallerweb.enterprise.prioritize.controller.security.AuthorizationController;
import de.hallerweb.enterprise.prioritize.controller.security.RestAccessController;
import de.hallerweb.enterprise.prioritize.controller.security.UserRoleController;
import de.hallerweb.enterprise.prioritize.model.Department;

/**
 * 
 * <p>
 * Copyright: (c) 2015
 * </p>
 * <p>
 * Peter Haller
 * </p>
 * 
 * @author peter
 * 
 *         REST-Service to create, update and delete {@link Department} objects.
 * 
 * 
 */
@RequestScoped
@Path("departments")
public class DepartmentService {

	@EJB
	CompanyController companyController;
	@EJB
	UserRoleController userRoleController;
	@EJB
	SearchController searchController;
	@EJB
	RestAccessController accessController;
	@EJB
	AuthorizationController authController;

	/**
	 * @api {get} /departments/{id} getDepartment
	 * @apiName getDepartment
	 * @apiGroup /department
	 * @apiDescription Returns the department with the given id.
	 * @apiParam {String} apiKey The API-Key of the user accessing the service.
	 * @apiSuccess {Department} company JSON Object with the department of the given id.
	 * @apiSuccessExample Success-Response:
	 *     HTTP/1.1 200 OK
	 *
	 * @apiError NotAuthorized APIKey incorrect.
	 *
	 * @param id - The id of the {@link Department}.
	 * @return {@link Department} - JSON Representation of the department.
	 */
	@GET
	@Path("{id}")
	@Produces(MediaType.APPLICATION_JSON)
	public Department getDepartment(@PathParam(value = "id") int id, @QueryParam(value = "apiKey") String apiKey) {
		if (accessController.checkApiKey(apiKey) != null) {
			return companyController.findDepartmentById(id);
		} else {
			throw new NotAuthorizedException(Response.serverError());
		}
	}
}
