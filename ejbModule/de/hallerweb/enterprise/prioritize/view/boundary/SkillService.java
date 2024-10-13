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

import de.hallerweb.enterprise.prioritize.controller.CompanyController;
import de.hallerweb.enterprise.prioritize.controller.search.SearchController;
import de.hallerweb.enterprise.prioritize.controller.security.AuthorizationController;
import de.hallerweb.enterprise.prioritize.controller.security.RestAccessController;
import de.hallerweb.enterprise.prioritize.controller.security.SessionController;
import de.hallerweb.enterprise.prioritize.controller.security.UserRoleController;
import de.hallerweb.enterprise.prioritize.controller.skill.SkillController;
import de.hallerweb.enterprise.prioritize.model.search.SearchResult;
import de.hallerweb.enterprise.prioritize.model.security.User;
import de.hallerweb.enterprise.prioritize.model.skill.Skill;
import de.hallerweb.enterprise.prioritize.model.skill.SkillCategory;
import jakarta.ejb.EJB;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
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
@Path("v1/skills")
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
     * Searches for a skill containing the searchphrase.
     *
     * @api {get} /search/ searchSkills
     * @apiName searchSkills
     * @apiGroup /skills
     * @apiDescription Searches for a skill containing the searchphrase.
     * @apiParam {String} apiKey The API-Key of the user accessing the service.
     * @apiParam {String} phrase The search phrase to use.
     * @apiSuccess A Set with Skill objects
     * @apiSuccessExample Success-Response:
     * HTTP/1.1 200 OK
     * @apiError NotAuthorized  APIKey incorrect.
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
                    searchResult.add(skill);
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
     * Lists all skill categories
     *
     * @api {get} /categories/ listCategories
     * @apiName listCategories
     * @apiGroup /skills
     * @apiDescription Lists all skill categories
     * @apiParam {String} apiKey The API-Key of the user accessing the service.
     * @apiSuccess A List with SkillCategory objects
     * @apiSuccessExample Success-Response:
     * HTTP/1.1 200 OK
     * @apiError NotAuthorized  APIKey incorrect.
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


    private Response createNegativeResponse(String responseText) {
        return Response.status(404).entity("{\"response\" : \"" + responseText + "\"}").build();
    }
}
