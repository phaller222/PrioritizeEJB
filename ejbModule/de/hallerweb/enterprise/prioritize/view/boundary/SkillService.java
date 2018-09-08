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
import de.hallerweb.enterprise.prioritize.controller.skill.SkillController;
import de.hallerweb.enterprise.prioritize.model.calendar.TimeSpan;
import de.hallerweb.enterprise.prioritize.model.search.SearchResult;
import de.hallerweb.enterprise.prioritize.model.security.User;
import de.hallerweb.enterprise.prioritize.model.skill.Skill;
import de.hallerweb.enterprise.prioritize.model.skill.SkillCategory;

/**
 * 
 * <p>
 * Copyright: (c) 2015
 * </p>
 * <p>
 * Peter Haller
 * </p>
 * 
 * @author peter REST-Service to create, update and delete skill related objects.
 */
@RequestScoped
@Path("skills")
public class SkillService {

	@EJB
	RestAccessController accessController;

	@EJB
	CompanyController companyController;

	@EJB
	SkillController skillController;

	@EJB
	UserRoleController userRoleController;

	@EJB
	SearchController searchController;

	@Inject
	SessionController sessionController;

	@EJB
	AuthorizationController authController;

	/**
	 * Returns {@link skill} objects containing the given phrase in name or description. *
	 *
	 * @param phrase - The search phrase.
	 * @return JSON object with {@link TimeSpan} objects for that department.
	 */
	@GET
	@Path("search/")
	@Produces(MediaType.APPLICATION_JSON)
	public Set<Skill> searchSkills(@QueryParam(value = "apiKey") String apiKey, @QueryParam(value = "phrase") String phrase) {
		User user = accessController.checkApiKey(apiKey);
		if (user != null) {
			Set<Skill> searchResult = new HashSet<>();

			List<SearchResult> results = searchController.searchSkills(phrase, user);

			for (SearchResult result : results) {
				Skill skill = (Skill) result.getResult();
				if (authController.canRead(skill, user)) {
					searchResult.add((Skill) skill);
				} else {
					break;
				}
			}
			if (!searchResult.isEmpty()) {
				return searchResult;
			} else {
				throw new NotFoundException(createNegativeResponse("Query returned no result!"));
			}
		} else {
			throw new NotAuthorizedException(Response.serverError());
		}
	}

	/**
	 * Returns all defined SkillCategory objects. *
	 *
	 * @return JSON object with all defined {@link SkillCategory} objects.
	 */
	@GET
	@Path("categories/")
	@Produces(MediaType.APPLICATION_JSON)
	public List<SkillCategory> listCategories(@QueryParam(value = "apiKey") String apiKey) {
		User user = accessController.checkApiKey(apiKey);
		if (user != null) {
			List<SkillCategory> categories = skillController.getAllCategories();
			if (categories != null && !categories.isEmpty()) {
				return categories;
			} else {
				throw new NotFoundException(createNegativeResponse("No skill categories defined!"));
			}
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
