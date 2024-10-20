/*
 * Copyright 2015-2024 Peter Michael Haller and contributors
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
import de.hallerweb.enterprise.prioritize.controller.DepartmentController;
import de.hallerweb.enterprise.prioritize.controller.InitializationController;
import de.hallerweb.enterprise.prioritize.controller.resource.MQTTResourceController;
import de.hallerweb.enterprise.prioritize.controller.resource.ResourceController;
import de.hallerweb.enterprise.prioritize.controller.resource.ResourceReservationController;
import de.hallerweb.enterprise.prioritize.controller.search.SearchController;
import de.hallerweb.enterprise.prioritize.controller.security.AuthorizationController;
import de.hallerweb.enterprise.prioritize.controller.security.RestAccessController;
import de.hallerweb.enterprise.prioritize.controller.security.SessionController;
import de.hallerweb.enterprise.prioritize.controller.security.UserRoleController;
import de.hallerweb.enterprise.prioritize.model.Department;
import de.hallerweb.enterprise.prioritize.model.resource.NameValueEntry;
import de.hallerweb.enterprise.prioritize.model.resource.Resource;
import de.hallerweb.enterprise.prioritize.model.resource.ResourceGroup;
import de.hallerweb.enterprise.prioritize.model.search.SearchResult;
import de.hallerweb.enterprise.prioritize.model.security.User;
import jakarta.ejb.EJB;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.*;

/**
 * <p>
 * Copyright: (c) 2015
 * </p>
 * <p>
 * Peter Haller
 * </p>
 *
 * @author peter REST-Service to create, update and delete {@link Resource}
 * objects.
 */
@RequestScoped
@Path("v1/resources")
public class ResourceService {

    @EJB
    RestAccessController accessController;
    @EJB
    CompanyController companyController;
    @EJB
    DepartmentController departmentController;
    @EJB
    ResourceController resourceController;
    @EJB
    MQTTResourceController mqttResourceController;
    @EJB
    ResourceReservationController resourceReservationController;
    @EJB
    UserRoleController userRoleController;
    @EJB
    SearchController searchController;
    @Inject
    SessionController sessionController;
    @EJB
    AuthorizationController authController;
    @EJB
    InitializationController initController;

    /**
     * Returns all the resource groups in the given department
     *
     * @param departmentToken - The department token.
     * @return JSON object with resources in that department.
     * @api {get} /list/{departmentToken}/groups?apiKey={apiKey} getResourceGroups
     * @apiName getResourceGroups
     * @apiGroup /resources
     * @apiDescription gets all resource groups within the given department.
     * @apiParam {String} apiKey The API-Key of the user accessing the service.
     * @apiParam {String} departmentToken The department token of the department.
     * @apiSuccess List of {ResourceGroup} objects with information about found resource groups.
     * @apiSuccessExample Success-Response:
     * HTTP/1.1 200 OK
     * @apiError NotAuthorized  APIKey incorrect.
     */
    @GET
    @Path("list/{departmentToken}/groups")
    @Produces(MediaType.APPLICATION_JSON)
    public List<ResourceGroup> getResourceGroups(@PathParam(value = "departmentToken") String departmentToken,
                                                 @QueryParam(value = "apiKey") String apiKey) {
        User user = accessController.checkApiKey(apiKey);
        if (user == null) {
            throw new NotAuthorizedException(Response.serverError());
        } else {
            Department dept = departmentController.getDepartmentByToken(departmentToken, user);
            if (dept != null) {
                List<ResourceGroup> groups = resourceController.getResourceGroupsForDepartment(dept.getId(), user);
                if (groups != null) {
                    if (authController.canRead(groups.get(0), user)) {
                        return groups;
                    } else {
                        throw new NotAuthorizedException(Response.serverError());
                    }
                }
                throw new NotFoundException(createNegativeResponse("no resource groups found!"));
            }
            throw new NotFoundException(createNegativeResponse("Department not found or department token invalid!"));
        }
    }

    /**
     * Returns all the resource groups in the given department and group
     *
     * @api {get} /list/{departmentToken}/{group}?apiKey={apiKey} getResources
     * @apiName getResources
     * @apiGroup /resources
     * @apiDescription gets all resources within the given department and group.
     * @apiParam {String} apiKey The API-Key of the user accessing the service.
     * @apiParam {String} departmentToken The department token of the department.
     * @apiParam {String} group The resource group of the resources.
     * @apiSuccess List of {Resource} objects.
     * @apiSuccessExample Success-Response:
     * HTTP/1.1 200 OK
     * @apiError NotAuthorized  APIKey incorrect.
     */
    @GET
    @Path("list/{departmentToken}/{group}")
    @Produces(MediaType.APPLICATION_JSON)
    public Set<Resource> getResources(@PathParam(value = "departmentToken") String departmentToken,
                                      @QueryParam(value = "apiKey") String apiKey, @PathParam(value = "group") String group) {
        User user = accessController.checkApiKey(apiKey);
        if (user != null) {
            Department dept = departmentController.getDepartmentByToken(departmentToken, user);
            if (dept != null) {
                ResourceGroup grp = resourceController.findResourceGroupByNameAndDepartment(group, dept.getId(), user);
                if (authController.canRead(grp, user)) {
                    return grp.getResources();
                } else {
                    throw new NotAuthorizedException(Response.serverError());
                }
            }
            throw new NotFoundException(createNegativeResponse("no resource groups found!"));
        }
        throw new NotFoundException(createNegativeResponse("Department not found or department token invalid!"));
    }

    /**
     * Searches the given department for resources with phrase.
     *
     * @api {get} search/{departmentToken}
     * @apiName searchResources
     * @apiGroup /resources
     * @apiDescription Searches the given department for resources with phrase.
     * @apiParam {String} apiKey The API-Key of the user accessing the service.
     * @apiParam {String} departmentToken The department token of the department.
     * @apiSuccess Set of {Resource} objects.
     * @apiSuccessExample Success-Response:
     * HTTP/1.1 200 OK
     * @apiError NotAuthorized  APIKey incorrect.
     */
    @GET
    @Path("search/{departmentToken}")
    @Produces(MediaType.APPLICATION_JSON)
    public Set<Resource> searchResources(@PathParam(value = "departmentToken") String departmentToken,
                                         @QueryParam(value = "apiKey") String apiKey, @QueryParam(value = "phrase") String phrase) {
        User user = accessController.checkApiKey(apiKey);
        if (user == null) {
            throw new NotAuthorizedException(Response.serverError());
        } else {
            Department dept = departmentController.getDepartmentByToken(departmentToken, user);
            if (dept == null) {
                throw new NotAuthorizedException(Response.serverError());
            } else {
                Set<Resource> searchResult = new HashSet<>();
                List<SearchResult> results = searchController.searchResources(phrase, user);
                for (SearchResult result : results) {
                    Resource resource = (Resource) result.getResult();
                    ResourceGroup group = resource.getResourceGroup();
                    if (authController.canRead(group, user)) {
                        searchResult.add(resource);
                    } else {
                        throw new NotAuthorizedException(Response.serverError());
                    }
                }
                return searchResult;
            }
        }
    }

    /**
     * Searches for a resource with the given uuid.
     *
     * @api {get} uuid/{uuid}
     * @apiName getResourceByUuid
     * @apiGroup /resources
     * @apiDescription Searches for a resource with the given uuid.
     * @apiParam {String} apiKey The API-Key of the user accessing the service.
     * @apiSuccess Resource object found.
     * @apiSuccessExample Success-Response:
     * HTTP/1.1 200 OK
     * @apiError NotAuthorized  APIKey incorrect.
     */
    @GET
    @Path("uuid/{uuid}")
    @Produces(MediaType.APPLICATION_JSON)
    public Object getResourceByUuid(@PathParam(value = "uuid") String uuid, @QueryParam(value = "apiKey") String apiKey) {
        User user = accessController.checkApiKey(apiKey);
        if (user == null) {
            throw new NotAuthorizedException(Response.serverError());
        } else {
            Resource resource = mqttResourceController.getResource(uuid, user);
            if (resource == null) {
                return createNegativeResponse("Resource not found!");
            }
            if (authController.canRead(resource, user)) {
                return resource;
            } else {
                throw new NotAuthorizedException(Response.serverError());
            }
        }
    }

    /**
     * returns if the resource with the given uuid is online.
     *
     * @api {get} uuid/{uuid}/mqttOnline
     * @apiName isResourceOnline
     * @apiGroup /resources
     * @apiDescription returns if the resource with the given uuid is online.
     * @apiParam {String} apiKey The API-Key of the user accessing the service.
     * @apiSuccess true or false
     * @apiSuccessExample Success-Response:
     * HTTP/1.1 200 OK
     * @apiError NotAuthorized  APIKey incorrect.
     */
    @GET
    @Path("uuid/{uuid}/mqttOnline")
    @Produces(MediaType.APPLICATION_JSON)
    public boolean isResourceOnline(@PathParam(value = "uuid") String uuid, @QueryParam(value = "apiKey") String apiKey) {
        User user = accessController.checkApiKey(apiKey);
        if (user == null) {
            throw new NotAuthorizedException(Response.serverError());
        } else {
            Resource resource = mqttResourceController.getResource(uuid, user);
            if (authController.canRead(resource, user)) {
                return resource.isMqttOnline();
            } else {
                throw new NotAuthorizedException(Response.serverError());
            }
        }
    }

    /**
     * sets the attributes of a resource identified by it's uuid
     *
     * @api {put} uuid/{uuid}
     * @apiName setResourceAttributesByUuid
     * @apiGroup /resources
     * @apiDescription Changes different attributes of a resource
     * @apiParam {String} apiKey The API-Key of the user accessing the service.
     * @apiParam {String} name The new name of the resource. omit if no changes.
     * @apiParam {String} description The new description of the resource. omit if no changes.
     * @apiParam {String} mqttOnline (true/false) - Set the resources online state. omit if no changes.
     * @apiParam {String} commands update command set the resource understands. separate by colon (e.G ON:OFF:RESET)
     * @apiParam {String} geo new coordinates of the resource (LAT:LONG)- leave blank if no changes
     * @apiParam {String} set set a specific resource attribute to a specific value (e.G. NAME:WERT)
     * @apiSuccess true or false
     * @apiSuccessExample Success-Response:
     * HTTP/1.1 200 OK
     * @apiError NotAuthorized  APIKey incorrect.
     */
    @PUT
    @Path("uuid/{uuid}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response setResourceAttributesByUuid(@PathParam(value = "uuid") String uuid, @QueryParam(value = "mqttOnline") String mqttOnline,
                                                @QueryParam(value = "name") String name, @QueryParam(value = "description") String description,
                                                @QueryParam(value = "commands") String commands, @QueryParam(value = "geo") String geo,
                                                @QueryParam(value = "set") String set,
                                                @QueryParam(value = "apiKey") String apiKey) {
        User user = userRoleController.findUserByApiKey(apiKey);
        Resource resource = mqttResourceController.getResource(uuid, user);
        if (authController.canUpdate(resource, user)) {
            return setResourceAttributes(resource, mqttOnline, name, description, commands, geo, set, apiKey);
        } else {
            throw new NotAuthorizedException(Response.serverError());
        }

    }

    /**
     * Return the {@link Resource} object with the given id.
     *
     * @param id - The id of the {@link Resource}.
     * @return {@link Resource} - JSON Representation of the Resource.
     * @api {get} /id/{id}?apiKey={apiKey} getResourceById
     * @apiName getResourceById
     * @apiGroup /resources
     * @apiDescription returns the resource/device with the given id
     * @apiParam {String} apiKey The API-Key of the user accessing the service.
     * @apiSuccess {Resource} resource/device Object.
     * @apiSuccessExample Success-Response:
     * HTTP/1.1 200 OK
     * @apiError NotAuthorized  APIKey incorrect.
     */
    @GET
    @Path("id/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Resource getResourceById(@PathParam(value = "id") String id, @QueryParam(value = "apiKey") String apiKey) {
        User user = accessController.checkApiKey(apiKey);
        if (user == null) {
            throw new NotAuthorizedException(Response.serverError());
        } else {
            Resource resource = resourceController.getResource(Integer.parseInt(id), user);
            if (authController.canRead(resource, user)) {
                return resource;
            } else {
                throw new NotAuthorizedException(Response.serverError());
            }
        }
    }

    /**
     * returns if the resource with the given id is online.
     *
     * @api {get} id/{id}/mqttOnline
     * @apiName isResourceOnlineById
     * @apiGroup /resources
     * @apiDescription returns if the resource with the given id is online.
     * @apiParam {String} apiKey The API-Key of the user accessing the service.
     * @apiSuccess true or false
     * @apiSuccessExample Success-Response:
     * HTTP/1.1 200 OK
     * @apiError NotAuthorized  APIKey incorrect.
     */
    @GET
    @Path("id/{id}/mqttOnline")
    @Produces(MediaType.APPLICATION_JSON)
    public boolean isResourceOnlineById(@PathParam(value = "id") String id, @QueryParam(value = "apiKey") String apiKey) {
        User user = accessController.checkApiKey(apiKey);
        if (user == null) {
            throw new NotAuthorizedException(Response.serverError());
        } else {
            Resource resource = resourceController.getResource(Integer.parseInt(id), user);
            if (authController.canRead(resource, user)) {
                return resource.isMqttOnline();
            } else {
                throw new NotAuthorizedException(Response.serverError());
            }
        }
    }

    /**
     * Updates a resource and sets it's attributes.
     *
     * @api {put} /id/{id}?apiKey={apiKey}&name={name}&description={description}&mqttOnline={mqttOnline}&commands={commands}&geo=[geo}&set={set} updateResource
     * @apiName updateResource
     * @apiGroup /resources
     * @apiDescription Changes different attributes of a resource
     * @apiParam {String} apiKey The API-Key of the user accessing the service.
     * @apiParam {String} name The new name of the resource. omit if no changes.
     * @apiParam {String} description The new description of the resource. omit if no changes.
     * @apiParam {String} mqttOnline (true/false) - Set the resources online state. omit if no changes.
     * @apiParam {String} commands update command set the resource understands. separate by colon (e.G ON:OFF:RESET)
     * @apiParam {String} geo new coordinates of the resource (LAT:LONG)- leave blank if no changes
     * @apiParam {String} set set a specific resource attribute to a specific value (e.G. NAME:WERT)
     * @apiSuccess OK
     * @apiSuccessExample Success-Response:
     * HTTP/1.1 200 OK
     * @apiError NotAuthorized  APIKey incorrect.
     */
    @PUT
    @Path("id/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response setResourceAttributesById(@PathParam(value = "id") String id, @QueryParam(value = "mqttOnline") String mqttOnline,
                                              @QueryParam(value = "name") String name, @QueryParam(value = "description") String description,
                                              @QueryParam(value = "commands") String commands, @QueryParam(value = "geo") String geo,
                                              @QueryParam(value = "set") String set,
                                              @QueryParam(value = "apiKey") String apiKey) {
        User user = userRoleController.findUserByApiKey(apiKey);
        Resource resource = resourceController.getResource(Integer.parseInt(id), user);
        if (authController.canUpdate(resource, user)) {
            return setResourceAttributes(resource, mqttOnline, name, description, commands, geo, set, apiKey);
        } else {
            throw new NotAuthorizedException(Response.serverError());
        }

    }


    /**
     * @return Response
     * @api {post} /create/{departmentToken}/{group}?apiKey={apiKey} createResource
     * @apiName createResource
     * @apiGroup /resources
     * @apiDescription creates a new resource
     * @apiParam {String} apiKey The API-Key of the user accessing the service.
     * @apiParam {String} departmentToken The department token of the department.
     * @apiParam {String} group The resource group to put new resource in.
     * @apiSuccess 200 OK.
     * @apiSuccessExample Success-Response:
     * HTTP/1.1 200 OK
     * @apiError NotAuthorized  APIKey incorrect.
     * @apiParam uuid - uuid of new resource
     * @apiParam name - name of new resource
     * @apiParam description - description
     * @apiParam slots - max.number of slots for new device
     * @apiParam ip - ip address of new device/resource (if applicable)
     * @apiParam commands - list of commands the device understands
     * @apiParam isAgent - is device an agent?
     */
    @POST
    @Path("create/{departmentToken}/{group}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response createResource(@QueryParam(value = "apiKey") String apiKey, @QueryParam(value = "uuid") String uuid,
                                   @PathParam(value = "departmentToken") String departmentToken, @PathParam(value = "group") String group,
                                   @QueryParam(value = "name") String name, @QueryParam(value = "description") String description,
                                   @QueryParam(value = "slots") String slots, @QueryParam(value = "ip") String ip,
                                   @QueryParam(value = "commands") String commands,
                                   @QueryParam(value = "isAgent") boolean isAgent) {
        User user = accessController.checkApiKey(apiKey);
        if (user == null) {
            throw new NotAuthorizedException(Response.serverError());
        } else {
            Department dept = departmentController.getDepartmentByToken(departmentToken, user);
            if (authController.canCreate(dept.getId(), new Resource(), user)) {
                Resource tempResource = new Resource();
                tempResource.setName(name);
                tempResource.setDescription(description);
                tempResource.setMaxSlots(Integer.parseInt(slots));
                tempResource.setStationary(false);
                tempResource.setRemote(true);
                tempResource.setAgent(false);
                tempResource.setMqttUUID(uuid);
                tempResource.setDataReceiveTopic(uuid + "/write");
                tempResource.setDataSendTopic(uuid + "/read");
                tempResource.setIp(ip != null ? ip : "");

                Resource resource = mqttResourceController.createMqttResource(tempResource, departmentToken, group, user);

                if (commands != null) {
                    HashSet<String> cmds = new HashSet<>();
                    String[] commandsArray = commands.split(":");
                    Collections.addAll(cmds, commandsArray);
                    mqttResourceController.setCommands(resource, cmds);
                }

                if (resource == null) {
                    return createNegativeResponse("Resource could not be created!");
                } else {
                    return createPositiveResponse("Resource has been discovered and created.");
                }
            } else {
                throw new NotAuthorizedException(Response.serverError());
            }
        }
    }

    /**
     * @api {post} /command?apiKey={apiKey} sendCommand
     * @apiName sendCommand
     * @apiGroup /resources
     * @apiDescription sends a command to a resource
     * @apiParam {String} apiKey The API-Key of the user accessing the service.
     * @apiParam {String} departmentToken The department token of the department.
     * @apiParam {String} group The resource group of the device.
     * @apiParam {String} uuid The uuid of the resource.
     * @apiParam {String} command - the command to send
     * @apiParam {String} value - the value sent with the command
     * @apiSuccess 200 OK.
     * @apiSuccessExample Success-Response:
     * HTTP/1.1 200 OK
     * @apiError NotAuthorized  APIKey incorrect.
     */
    @POST
    @Path("command")
    @Produces(MediaType.APPLICATION_JSON)
    public Response sendCommand(@QueryParam(value = "apiKey") String apiKey, @QueryParam(value = "uuid") String uuid,
                                @QueryParam(value = "departmentToken") String departmentToken, @QueryParam(value = "group") String group,
                                @QueryParam(value = "command") String command, @QueryParam(value = "value") String value) {
        User user = accessController.checkApiKey(apiKey);
        if (user != null) {
            Resource managedResource = mqttResourceController.getResource(uuid, user);
            if (authController.canUpdate(managedResource, user)) {
                if (resourceReservationController.createResourceReservation(managedResource, new Date(),
                    new Date(System.currentTimeMillis() + 2000), user) != null) {
                    mqttResourceController.sendCommand(managedResource, command, value);
                    return createPositiveResponse("Command has been send to resource: " + managedResource.getMqttUUID());
                } else {
                    return createNegativeResponse("Not enough free slots available for resource: " + managedResource.getMqttUUID());
                }
            }
            throw new NotAuthorizedException(Response.serverError());
        }
        throw new NotAuthorizedException(Response.serverError());
    }

    /**
     * @api {delete} /remove/uuid/{uuid}?apiKey={apiKey}&departmentToken={departmenttoken} deleteResourceByUuid
     * @apiName deleteResourceByUuid
     * @apiGroup /resources
     * @apiDescription Deletes a resource by uuid
     * @apiParam {String} apiKey The API-Key of the user accessing the service.
     * @apiParam {String} uuid The uuid of the resource to remove.
     * @apiParam {String} departmentToken department token of the department the resource belongs to.
     * @apiSuccess OK
     * @apiSuccessExample Success-Response:
     * HTTP/1.1 200 OK
     * @apiError NotAuthorized  APIKey incorrect.
     */
    @DELETE
    @Path("remove/uuid/{uuid}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response removeResource(@QueryParam(value = "apiKey") String apiKey,
                                   @QueryParam(value = "departmentToken") String departmentToken, @PathParam("uuid") String uuid) {
        User user = accessController.checkApiKey(apiKey);
        if (user != null) {
            Department dept = departmentController.getDepartmentByToken(departmentToken, user);
            if (dept != null) {
                Resource resource = mqttResourceController.getResource(uuid, user);
                if (resource != null) {
                    if (authController.canDelete(resource, user)) {
                        resourceController.deleteResource(resource.getId(), user);
                        return createPositiveResponse("Resource has been removed.");
                    } else {
                        throw new NotAuthorizedException(Response.serverError());
                    }
                } else {
                    throw new NotFoundException(Response.serverError().build());
                }
            } else {
                throw new NotAuthorizedException(Response.serverError());
            }
        } else {
            throw new NotAuthorizedException(Response.serverError());
        }
    }

    /**
     * @api {delete} /remove/id/{id}?apiKey={apiKey}&departmentToken={departmenttoken} deleteResourceById
     * @apiName deleteResourceById
     * @apiGroup /resources
     * @apiDescription Deletes a resource by id
     * @apiParam {String} apiKey The API-Key of the user accessing the service.
     * @apiParam {String} id The id of the resource to remove.
     * @apiParam {String} departmentToken department token of the department the resource belongs to.
     * @apiSuccess OK
     * @apiSuccessExample Success-Response:
     * HTTP/1.1 200 OK
     * @apiError NotAuthorized  APIKey incorrect.
     */
    @DELETE
    @Path("remove/id/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response removeResource(@QueryParam(value = "apiKey") String apiKey,
                                   @QueryParam(value = "departmentToken") String departmentToken, @PathParam("id") int id) {
        User user = accessController.checkApiKey(apiKey);
        if (user != null) {
            Department dept = departmentController.getDepartmentByToken(departmentToken, user);
            if (dept != null) {
                Resource resource = resourceController.getResource(id, user);
                if (resource != null) {
                    if (authController.canDelete(resource, user)) {
                        resourceController.deleteResource(resource.getId(), user);
                        return createPositiveResponse("Resource has been removed.");
                    } else {
                        throw new NotAuthorizedException(Response.serverError());
                    }
                } else {
                    throw new NotFoundException(Response.serverError().build());
                }
            } else {
                throw new NotAuthorizedException(Response.serverError());
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

    private Response setResourceAttributes(Resource resource, String mqttOnline, String name, String description, String commands,
                                           String geo, String set, String apiKey) {
        User user = accessController.checkApiKey(apiKey);
        if (user != null) {
            boolean processed = false;
            processed = handleSetResourceAttributes(resource, mqttOnline, name, description, commands, geo, set, processed);

            if (!processed) {
                return createNegativeResponse("ERROR: None of the given resource property names found! Nothing changed.");
            } else {
                return createPositiveResponse("OK");
            }

        } else {
            throw new NotAuthorizedException(Response.serverError());
        }
    }

    private boolean handleSetResourceAttributes(Resource resource, String mqttOnline, String name, String description, String commands,
                                                String geo, String set, boolean processed) {
        boolean processedCopy = processed;
        if (mqttOnline != null) {
            processedCopy = true;
            boolean online = Boolean.parseBoolean(mqttOnline);
            mqttResourceController.setMqttResourceStatus(resource, online);
        }

        if (name != null) {
            processedCopy = true;
            resourceController.setResourceName(resource, name, sessionController.getUser());
        }
        if (description != null) {
            processedCopy = true;
            resourceController.setResourceDescription(resource, description, sessionController.getUser());
        }
        if (commands != null) {
            processedCopy = handleSetCommands(resource, commands);
        }
        if (geo != null) {
            processedCopy = true;
            String[] geoString = geo.split(":");
            mqttResourceController.setCoordinates(resource, geoString[0], geoString[1]);
        }

        if (set != null) {
            processedCopy = handleSetNameValuePairs(resource, set);
        }
        return processedCopy;
    }

    private boolean handleSetCommands(Resource resource, String commands) {
        String[] commandString = commands.split(":");
        HashSet<String> commandsForResource = new HashSet<>();
        Collections.addAll(commandsForResource, commandString);
        mqttResourceController.setCommands(resource, commandsForResource);
        return true;
    }

    private boolean handleSetNameValuePairs(Resource resource, String set) {
        String[] nameValuePair = set.split(":");

        // ----------------- Raise event for value change
        Set<NameValueEntry> valuesOld = mqttResourceController.getNameValueEntries(resource);
        String oldValue;
        for (NameValueEntry entry : valuesOld) {
            if (entry.getName().equals(nameValuePair[0])) {
                oldValue = entry.getValues();
                // TODO: compare values and raise event if needed
            }
        }
        // ------------------------------------------------------

        mqttResourceController.addMqttValueForResource(resource, nameValuePair[0], nameValuePair[1]);
        return true;
    }


}
