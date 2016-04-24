/**
 * 
 */
package de.hallerweb.enterprise.prioritize.view.boundary;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.ejb.EJB;
import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.NotAuthorizedException;
import javax.ws.rs.NotFoundException;
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
import de.hallerweb.enterprise.prioritize.controller.security.SessionController;
import de.hallerweb.enterprise.prioritize.controller.security.UserRoleController;
import de.hallerweb.enterprise.prioritize.model.Company;
import de.hallerweb.enterprise.prioritize.model.Department;
import de.hallerweb.enterprise.prioritize.model.resource.Resource;
import de.hallerweb.enterprise.prioritize.model.search.SearchResult;
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
 * @author peter REST-Service to retrieve {@link User} objects.
 */
@RequestScoped
@Path("users")
public class UserRoleService {

	@EJB
	RestAccessController accessController;

	@EJB
	AuthorizationController authController;

	@EJB
	CompanyController companyController;

	@EJB
	UserRoleController userRoleController;

	@EJB
	SearchController searchController;

	@Inject
	SessionController sessionController;

	/**
	 * Returns all the users in the given department
	 * 
	 * @param departmentToken - The department token.
	 * @return JSON object with users in that department.
	 */
	@GET
	@Path("list/{departmentToken}")
	@Produces(MediaType.APPLICATION_JSON)
	public List<User> getUsers(@PathParam(value = "departmentToken") String departmentToken, @QueryParam(value = "apiKey") String apiKey) {
		User user = accessController.checkApiKey(apiKey);
		if (user != null) {
			Department dept = companyController.getDepartmentByToken(departmentToken);
			if (dept != null) {
				List<User> users = userRoleController.getUsersForDepartment(dept, user);
				return users;
			}
			throw new NotFoundException(createNegativeResponse("Department not found or department token invalid!"));
		} else
			throw new NotAuthorizedException(Response.serverError());
	}

	/**
	 * Return the {@link User} object with the given id.
	 *
	 * @param id
	 *            - The id of the {@link Resource}.
	 * @return {@link Company} - JSON Representation of the company.
	 */
	@GET
	@Path("id/{id}")
	@Produces(MediaType.APPLICATION_JSON)
	public User getUserById(@PathParam(value = "id") String id, @QueryParam(value = "apiKey") String apiKey) {
		User user = accessController.checkApiKey(apiKey);
		if (user != null) {
			return userRoleController.getUserById(Integer.parseInt(id), user);
		} else
			throw new NotFoundException(createNegativeResponse("User with id " + id + " not found!"));

	}

	/**
	 * Returns all the users matching the search phrase.
	 *
	 * @param departmentToken
	 *            - The department token.
	 * @return JSON object with documents in that department.
	 */
	@GET
	@Path("search/users")
	@Produces(MediaType.APPLICATION_JSON)
	public Set<User> searchUsers(@QueryParam(value = "apiKey") String apiKey, @QueryParam(value = "phrase") String phrase) {
		User user = accessController.checkApiKey(apiKey);
		if (user != null) {
			Set<User> searchResult = new HashSet<User>();
			List<SearchResult> results = searchController.searchUsers(phrase, user);
			for (SearchResult result : results) {
				User foundUser = (User) result.getResult();
				if (authController.canRead(foundUser, user)) {
					searchResult.add(foundUser);
				} else {
					throw new NotAuthorizedException(Response.serverError());
				}
			}
			return searchResult;

		} else {
			throw new NotAuthorizedException(Response.serverError());
		}
	}

	/**
	 * Returns all the users matching the search phrase.
	 *
	 * @param departmentToken
	 *            - The department token.
	 * @return JSON object with documents in that department.
	 */
	@GET
	@Path("search/roles")
	@Produces(MediaType.APPLICATION_JSON)
	public Set<Role> searchRoles(@QueryParam(value = "apiKey") String apiKey, @QueryParam(value = "phrase") String phrase) {
		User user = accessController.checkApiKey(apiKey);
		if (user != null) {
			Set<Role> searchResult = new HashSet<Role>();
			List<SearchResult> results = searchController.searchRoles(phrase, user);
			for (SearchResult result : results) {
				Role foundRole = (Role) result.getResult();
				if (authController.canRead(foundRole, user)) {
					searchResult.add(foundRole);
				} else {
					throw new NotAuthorizedException(Response.serverError());
				}
			}
			return searchResult;

		} else {
			throw new NotAuthorizedException(Response.serverError());
		}
	}

	private Response createPositiveResponse(String responseText) {
		return Response.status(200).entity("{\"response\" : \"" + responseText + "\"}").build();
	}

	private Response createNegativeResponse(String responseText) {
		return Response.status(405).entity("{\"response\" : \"" + responseText + "\"}").build();
	}
}
