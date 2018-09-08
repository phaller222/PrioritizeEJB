/**
 * 
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
