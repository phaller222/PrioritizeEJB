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

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import jakarta.ejb.EJB;
import jakarta.enterprise.context.RequestScoped;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.NotAuthorizedException;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

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
@Path("v1/companies")
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
	 * @api {get} /companies/{id} getCompany
	 * @apiName getCompany
	 * @apiGroup /company
	 * @apiDescription Returns the company with the given id.
	 * @apiParam {String} apiKey The API-Key of the user accessing the service.
	 * @apiSuccess {Company} company JSON Object with the company of the given id.
	 * @apiSuccessExample Success-Response:
	 *     HTTP/1.1 200 OK
	 *	{
	 *    "id" : 1,
	 *    "name" : "Default Company",
	 *    "description" : "",
	 *    "mainAddress" : {
	 *    "id" : 7,
	 *    "zipCode" : "00000",
	 *    "phone" : "00000-00000",
	 *    "fax" : "00000-00000",
	 *    "city" : "City of Admin",
	 *    "street" : "Street of Admins"
	 *    ...many more
	 *  }
	 *
	 * @apiError NotAuthorized APIKey incorrect.
	 */
	@GET
	@Path("id/{id}")
	@Produces(MediaType.APPLICATION_JSON)
	public Company getCompany(@PathParam(value = "id") int id, @QueryParam(value = "apiKey") String apiKey) {
		if (accessController.checkApiKey(apiKey) != null) {
			return companyController.findCompanyById(id);
		} else {
			throw new NotAuthorizedException(Response.serverError());
		}
	}

	@GET
	@Path("name/{name}")
	@Produces(MediaType.APPLICATION_JSON)
	public Company getCompany(@PathParam(value = "name") String name, @QueryParam(value = "apiKey") String apiKey)
			throws UnsupportedEncodingException {
		if (accessController.checkApiKey(apiKey) != null) {
			name = URLDecoder.decode(name,"UTF-8");
			return companyController.findCompanyByName(name);
		} else {
			throw new NotAuthorizedException(Response.serverError());
		}
	}

	@GET
	@Path("list/")
	@Produces(MediaType.APPLICATION_JSON)
	public List<Company> getAllCompanies(@QueryParam(value = "apiKey") String apiKey) {
		User user = accessController.checkApiKey(apiKey);
		if (user != null) {
			return companyController.getAllCompanies(user);
		} else {
			throw new NotAuthorizedException(Response.serverError());
		}
	}



	/**
	 * Returns all the departments matching the seach phrase.
	 *
	 * @api {get} /search/departments searchDepartments
	 * @apiName searchDepartments
	 * @apiGroup /company
	 * @apiDescription Searches all departments which contain the given phrase
	 * @apiParam {String} apiKey The API-Key of the user accessing the service.
	 * @apiParam {String} phrase The search phrase used in the search.
	 * @apiSuccess {Department} department JSON department Objects which contained the search phrase.
	 * @apiSuccessExample Success-Response:
	 *     HTTP/1.1 200 OK
	 * [
     *   {
     *     "id" : 6,
     *     "address" : {
     *     "id" : 5,
     *     "zipCode" : "00000",
     *     "phone" : "00000-00000",
     *     "fax" : "00000-00000",
     *     "city" : "City of Admin",
     *     "street" : "Street of Admins"
     *     ...many more
     *    }
     * ]
	 *
	 * @apiError NotAuthorized  APIKey incorrect.
	 *
	 */
	@GET
	@Path("search/departments")
	@Produces(MediaType.APPLICATION_JSON)
	public Set<Department> searchDepartments(@QueryParam(value = "apiKey") String apiKey, @QueryParam(value = "phrase") String phrase) {
		User user = accessController.checkApiKey(apiKey);
		if (user != null) {
			Set<Department> searchResult = new HashSet<>();
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
