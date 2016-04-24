/**
 * 
 */
package de.hallerweb.enterprise.prioritize.view.boundary;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

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
import de.hallerweb.enterprise.prioritize.model.Company;
import de.hallerweb.enterprise.prioritize.model.Department;
import de.hallerweb.enterprise.prioritize.model.search.SearchResult;
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
 * @author peter
 * 
 *         REST-Service to create, update and delete {@link Company} objects.
 * 
 * 
 */
@RequestScoped
@Path("companies")
public class CompanyService {

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
	 * Return the {@link Company} object with the given Id.
	 * 
	 * @param id
	 *            - The id of the {@link Company}.
	 * @return {@link Company} - JSON Representation of the company.
	 */
	@GET
	@Path("{id}")
	@Produces(MediaType.APPLICATION_JSON)
	public Company getCompany(@PathParam(value = "id") int id, @QueryParam(value = "apiKey") String apiKey) {
		if (accessController.checkApiKey(apiKey) != null) {
			return companyController.findCompanyById(id);
		} else
			throw new NotAuthorizedException(Response.serverError());
	}

	/**
	 * Returns all the departments matching the seacrh phrase.
	 *
	 * @return JSON object with departments in that company.
	 */
	@GET
	@Path("search/departments")
	@Produces(MediaType.APPLICATION_JSON)
	public Set<Department> searchDepartments(@QueryParam(value = "apiKey") String apiKey, @QueryParam(value = "phrase") String phrase) {
		User user = accessController.checkApiKey(apiKey);
		if (user != null) {
			Set<Department> searchResult = new HashSet<Department>();
			List<SearchResult> results = searchController.searchDepartments(phrase, user);
			for (SearchResult result : results) {
				Department dept = (Department) result.getResult();
				if (authController.canRead(dept, user)) {
					searchResult.add(dept);
				} else {
					throw new NotAuthorizedException(Response.serverError());
				}
			}
			return searchResult;

		} else {
			throw new NotAuthorizedException(Response.serverError());
		}
	}
}
