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
import de.hallerweb.enterprise.prioritize.model.search.SearchResult;
import de.hallerweb.enterprise.prioritize.model.security.Role;
import de.hallerweb.enterprise.prioritize.model.security.User;
import jakarta.ejb.EJB;
import jakarta.enterprise.context.RequestScoped;
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
 * @author peter REST-Service to retrieve {@link User} objects.
 */
@RequestScoped
@Path("v1/users")
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


    /**
     * Returns all the users in the given department
     *
     * @api {get} /users/department/{departmentToken} getUsers
     * @apiName getUsers
     * @apiGroup /users
     * @apiDescription Returns all users within the department with token {departmentToken}
     * @apiParam {String} apiKey The API-Key of the user accessing the service.
     * @apiParam {String} departmentToken The department token of the department to search for users.
     * @apiSuccess {List}  JSON-Array with all users in this department.
     * @apiSuccessExample Success-Response:
     * HTTP/1.1 200 OK
     * [
     * {
     * "id": 48,
     * "name": "peter",
     * "username": "peter"
     * },
     * {
     * "id": 54,
     * "name": "torsten",
     * "username": "torsten"
     * }
     * ]
     * @apiError NotAuthorized APIKey incorrect.
     */
    @GET
    @Path("department/{departmentToken}")
    @Produces(MediaType.APPLICATION_JSON)
    public List<User> getUsers(@PathParam(value = "departmentToken") String departmentToken, @QueryParam(value = "apiKey") String apiKey) {
        User user = accessController.checkApiKey(apiKey);
        if (user != null) {
            Department dept = companyController.getDepartmentByToken(departmentToken, user);
            if (dept != null) {
                return userRoleController.getUsersForDepartment(dept, user);
            }
            throw new NotFoundException(createNegativeResponse("Department not found or department token invalid!"));
        } else {
            throw new NotAuthorizedException(Response.serverError());
        }
    }

    /**
     * Return the {@link User} object with the given id.
     *
     * @api {get} /users/{id} getUserById
     * @apiName getUserById
     * @apiGroup /users
     * @apiDescription Returns user with the given {id}
     * @apiParam {String} apiKey The API-Key of the user accessing the service.
     * @apiParam {Integer} id - The user id of the user to retrieve.
     * @apiSuccess {User}  JSON-Object with the user with id {id}.
     * @apiSuccessExample Success-Response:
     * HTTP/1.1 200 OK
     * {
     * "id": 48,
     * "name": "peter",
     * "username": "peter"
     * }
     * @apiError NotAuthorized APIKey incorrect.
     */
    @GET
    @Path("{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public User getUserById(@PathParam(value = "id") String id, @QueryParam(value = "apiKey") String apiKey) {
        User user = accessController.checkApiKey(apiKey);
        if (user != null) {
            return userRoleController.getUserById(Integer.parseInt(id), user);
        } else {
            throw new NotFoundException(createNegativeResponse("User with id " + id + " not found!"));
        }

    }


    /**
     * Return the {@link User} object with the given username
     *
     * @api {get} /users/username/{username} getUserByUsername
     * @apiName getUserByUsername
     * @apiGroup /users
     * @apiDescription Returns user with the given {username}
     * @apiParam {String} apiKey The API-Key of the user accessing the service.
     * @apiParam {String} username - The username of the user to retrieve.
     * @apiSuccess {User}  JSON-Object with the user with id {id}.
     * @apiSuccessExample Success-Response:
     * HTTP/1.1 200 OK
     * {
     * "id": 48,
     * "name": "peter",
     * "username": "peter"
     * }
     * @apiError NotAuthorized APIKey incorrect.
     */
    @GET
    @Path("username/{username}")
    @Produces(MediaType.APPLICATION_JSON)
    public User getUserByUsername(@PathParam(value = "username") String username, @QueryParam(value = "apiKey") String apiKey) {
        User user = accessController.checkApiKey(apiKey);
        if (user != null) {
            return userRoleController.findUserByUsername(username, user);
        } else {
            throw new NotFoundException(createNegativeResponse("User with username " + username + " not found!"));
        }

    }


    /**
     * Searches for users by the given phrase.
     *
     * @api {get} /search/users searchUsers
     * @apiName searchUsers
     * @apiGroup /users
     * @apiDescription searches for users
     * @apiParam {String} apiKey The API-Key of the user accessing the service.
     * @apiParam {String} phrase - The searchstring to search for.
     * @apiSuccess {User}  JSON-Object with the users found.
     * @apiSuccessExample Success-Response:
     * HTTP/1.1 200 OK
     * {
     * "id": 48,
     * "name": "peter",
     * "username": "peter"
     * }
     * @apiError NotAuthorized APIKey incorrect.
     */
    @GET
    @Path("search/users")
    @Produces(MediaType.APPLICATION_JSON)
    public Set<User> searchUsers(@QueryParam(value = "apiKey") String apiKey, @QueryParam(value = "phrase") String phrase) {
        User user = accessController.checkApiKey(apiKey);
        if (user != null) {
            Set<User> searchResult = new HashSet<>();
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
     * Searches for roles by the given phrase.
     *
     * @api {get} /search/roles searchRoles
     * @apiName searchRoles
     * @apiGroup /users
     * @apiDescription searches for roles
     * @apiParam {String} apiKey The API-Key of the user accessing the service.
     * @apiParam {String} phrase - The searchstring to search for.
     * @apiSuccess {User}  JSON-Object with the roles found.
     * @apiSuccessExample Success-Response:
     * HTTP/1.1 200 OK
     * {
     * "id": 21,
     * "name": "Default Company-default-Admin",
     * "description": "Default Company - default - Admin",
     * "permissions": [
     * {
     * "id": 24,
     * "createPermission": true,
     * "readPermission": true,
     * "updatePermission": true,
     * "deletePermission": true,
     * "absoluteObjectType": "de.hallerweb.enterprise.prioritize.model.resource.ResourceGroup",
     * "objectName": "ResourceGroup",
     * "objectId": 0,
     * "department": {
     * "id": 19,
     * "address": {
     * "id": 18,
     * "zipCode": "00000",
     * "phone": "00000-00000",
     * "fax": "00000-00000",
     * "city": "City of Admin",
     * "street": "Street of Admins",
     * "email": null
     * },
     * "documentGroups": [
     * {
     * "id": 16,
     * "name": "default"
     * }
     * ],
     * "resourceGroups": [
     * {
     * "id": 17,
     * "name": "default"
     * }
     * ],
     * "name": "default",
     * "description": "Auto generated default department",
     * "searchProperties": [
     * {
     * "name": "DocumentInfo",
     * "type": "NAME"
     * }
     * ]
     * }
     * }
     * @apiError NotAuthorized APIKey incorrect.
     */
    @GET
    @Path("search/roles")
    @Produces(MediaType.APPLICATION_JSON)
    public Set<Role> searchRoles(@QueryParam(value = "apiKey") String apiKey, @QueryParam(value = "phrase") String phrase) {
        User user = accessController.checkApiKey(apiKey);
        if (user != null) {
            Set<Role> searchResult = new HashSet<>();
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

    private Response createNegativeResponse(String responseText) {
        return Response.status(405).entity("{\"response\" : \"" + responseText + "\"}").build();
    }
}
