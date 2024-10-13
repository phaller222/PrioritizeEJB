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
import de.hallerweb.enterprise.prioritize.controller.security.UserRoleController;
import de.hallerweb.enterprise.prioritize.model.Department;
import de.hallerweb.enterprise.prioritize.model.security.User;
import jakarta.ejb.EJB;
import jakarta.enterprise.context.RequestScoped;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.List;

/**
 *
 * <p>
 * Copyright: (c) 2015
 * </p>
 *
 * <p>
 * Peter Haller
 * </p>
 *
 * @author peter
 *
 *
 * <p>
 * REST-Service to create, update and delete {@link Department} objects.
 */
@RequestScoped
@Path("v1/departments")
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
     * HTTP/1.1 200 OK
     * @apiError NotAuthorized APIKey incorrect.
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

    @GET
    @Path("list/")
    @Produces(MediaType.APPLICATION_JSON)
    public List<Department> getAllDepartmentsForCompany(@QueryParam(value = "companyName") String companyName,
                                                        @QueryParam(value = "apiKey") String apiKey) {
        User user = accessController.checkApiKey(apiKey);
        if (user != null) {
            return companyController.findDepartmentsByCompany(
                companyController.findCompanyByName(companyName).getId(), user);
        } else {
            throw new NotAuthorizedException(Response.serverError());
        }
    }

}
