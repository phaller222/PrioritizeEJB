/**
 * 
 */
package de.hallerweb.enterprise.prioritize.view.boundary;

import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.ejb.EJB;
import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.NotAuthorizedException;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import de.hallerweb.enterprise.prioritize.controller.CompanyController;
import de.hallerweb.enterprise.prioritize.controller.resource.ResourceController;
import de.hallerweb.enterprise.prioritize.controller.search.SearchController;
import de.hallerweb.enterprise.prioritize.controller.security.AuthorizationController;
import de.hallerweb.enterprise.prioritize.controller.security.RestAccessController;
import de.hallerweb.enterprise.prioritize.controller.security.SessionController;
import de.hallerweb.enterprise.prioritize.controller.security.UserRoleController;
import de.hallerweb.enterprise.prioritize.model.Company;
import de.hallerweb.enterprise.prioritize.model.Department;
import de.hallerweb.enterprise.prioritize.model.resource.Resource;
import de.hallerweb.enterprise.prioritize.model.resource.ResourceGroup;
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
 * @author peter REST-Service to create, update and delete {@link Resource}
 *         objects.
 */
@RequestScoped
@Path("resources")
public class ResourceService {

	@EJB
	RestAccessController accessController;

	@EJB
	CompanyController companyController;

	@EJB
	ResourceController resourceController;

	@EJB
	UserRoleController userRoleController;

	@EJB
	SearchController searchController;

	@Inject
	SessionController sessionController;

	@EJB
	AuthorizationController authController;

	/**
	 * Returns all the resources in the given department
	 * 
	 * @param departmentToken - The department token.
	 * @return JSON object with resources in that department.
	 */
	@GET
	@Path("list/{departmentToken}/{group}")
	@Produces(MediaType.APPLICATION_JSON)
	public Set<Resource> getResources(@PathParam(value = "departmentToken") String departmentToken,
			@PathParam(value = "group") String group, @QueryParam(value = "apiKey") String apiKey) {
		User user = accessController.checkApiKey(apiKey);
		if (user != null) {
			Department dept = companyController.getDepartmentByToken(departmentToken);
			if (dept != null) {
				ResourceGroup resourceGroup = resourceController.getResourceGroupInDepartment(dept.getId(), group);
				if (resourceGroup != null) {
					if (authController.canRead(resourceGroup, user)) {
						Set<Resource> resources = resourceGroup.getResources();
						return resources;
					} else
						throw new NotAuthorizedException(Response.serverError());
				}
				throw new NotFoundException(createNegativeResponse("Resource group with name " + group + "not found!"));
			}
			throw new NotFoundException(createNegativeResponse("Department not found or department token invalid!"));
		} else
			throw new NotAuthorizedException(Response.serverError());
	}

	/**
	 * Returns all the resources in the given department
	 * 
	 * @param departmentToken - The department token.
	 * @return JSON object with resources in that department.
	 */
	@GET
	@Path("search/{departmentToken}")
	@Produces(MediaType.APPLICATION_JSON)
	public Set<Resource> searchResources(@PathParam(value = "departmentToken") String departmentToken,
			@QueryParam(value = "apiKey") String apiKey, @QueryParam(value = "phrase") String phrase) {
		User user = accessController.checkApiKey(apiKey);
		if (user != null) {
			Department dept = companyController.getDepartmentByToken(departmentToken);
			if (dept != null) {
				Set<Resource> searchResult = new HashSet<Resource>();
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
			} else {
				throw new NotAuthorizedException(Response.serverError());
			}
		} else {
			throw new NotAuthorizedException(Response.serverError());
		}
	}

	/**
	 * Return the {@link Resource} object with the given uuid.
	 * 
	 * @param uuid - The uuid of the {@link Resource}.
	 * @return {@link Company} - JSON Representation of the company.
	 */
	@GET
	@Path("uuid/{uuid}")
	@Produces(MediaType.APPLICATION_JSON)
	public Resource getResourceByUuid(@PathParam(value = "uuid") String uuid, @QueryParam(value = "apiKey") String apiKey) {
		User user = accessController.checkApiKey(apiKey);
		if (user != null) {
			Resource resource = resourceController.getResource(uuid, user);
			if (authController.canRead(resource, user)) {
				return resource;
			} else {
				throw new NotAuthorizedException(Response.serverError());
			}
		} else
			throw new NotAuthorizedException(Response.serverError());
	}

	/**
	 * Returns if the {@link Resource} object with the given uuid is online.
	 * 
	 * @param uuid - The uuid of the {@link Resource}.
	 * @return {@link Company} - JSON Representation of the company.
	 */
	@GET
	@Path("uuid/{uuid}/mqttOnline")
	@Produces(MediaType.APPLICATION_JSON)
	public boolean isResourceOnline(@PathParam(value = "uuid") String uuid, @QueryParam(value = "apiKey") String apiKey) {
		User user = accessController.checkApiKey(apiKey);
		if (user != null) {
			Resource resource = resourceController.getResource(uuid, user);
			if (authController.canRead(resource, user)) {
				return resource.isMqttOnline();
			} else {
				throw new NotAuthorizedException(Response.serverError());
			}
		} else
			throw new NotAuthorizedException(Response.serverError());
	}

	/**
	 * Updates a resource and sets different attributtes if present
	 * 
	 * @param uuid - The uuid of the {@link Resource}.
	 *
	 */
	@PUT
	@Path("uuid/{uuid}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response setResourceAttributesByUuid(@PathParam(value = "uuid") String uuid, @QueryParam(value = "mqttOnline") String mqttOnline,
			@QueryParam(value = "name") String name, @QueryParam(value = "description") String description,
			@QueryParam(value = "commands") String commands, @QueryParam(value = "geo") String geo, @QueryParam(value = "set") String set,
			@QueryParam(value = "apiKey") String apiKey) {
		User user = userRoleController.findUserByApiKey(apiKey);
		Resource resource = resourceController.getResource(uuid, user);
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
	 * @return {@link Company} - JSON Representation of the company.
	 */
	@GET
	@Path("id/{id}")
	@Produces(MediaType.APPLICATION_JSON)
	public Resource getResourceById(@PathParam(value = "id") String id, @QueryParam(value = "apiKey") String apiKey) {
		User user = accessController.checkApiKey(apiKey);
		if (user != null) {
			Resource resource = resourceController.getResource(Integer.parseInt(id), user);
			if (authController.canRead(resource, user)) {
				return resource;
			} else {
				throw new NotAuthorizedException(Response.serverError());
			}
		} else
			throw new NotAuthorizedException(Response.serverError());
	}

	/**
	 * Returns if the {@link Resource} object with the given id is online.
	 * 
	 * @param id - The id of the {@link Resource}.
	 * @return boolean true or false
	 */
	@GET
	@Path("id/{id}/mqttOnline")
	@Produces(MediaType.APPLICATION_JSON)
	public boolean isResourceOnlineById(@PathParam(value = "id") String id, @QueryParam(value = "apiKey") String apiKey) {
		User user = accessController.checkApiKey(apiKey);
		if (user != null) {
			Resource resource = resourceController.getResource(Integer.parseInt(id), user);
			if (authController.canRead(resource, user)) {
				return resource.isMqttOnline();
			} else {
				throw new NotAuthorizedException(Response.serverError());
			}
		} else
			throw new NotAuthorizedException(Response.serverError());
	}

	/**
	 * Updates a resource and sets it's online state.
	 * @param uuid - The uuid of the {@link Resource}.
	 */
	@PUT
	@Path("id/{id}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response setResourceAttributesById(@PathParam(value = "id") String id, @QueryParam(value = "mqttOnline") String mqttOnline,
			@QueryParam(value = "name") String name, @QueryParam(value = "description") String description,
			@QueryParam(value = "commands") String commands, @QueryParam(value = "geo") String geo, @QueryParam(value = "set") String set,
			@QueryParam(value = "apiKey") String apiKey) {
		User user = userRoleController.findUserByApiKey(apiKey);
		Resource resource = resourceController.getResource(Integer.parseInt(id), user);
		if (authController.canUpdate(resource, user)) {
			return setResourceAttributes(resource, mqttOnline, name, description, commands, geo, apiKey, set);
		} else {
			throw new NotAuthorizedException(Response.serverError());
		}

	}

	private Response setResourceAttributes(Resource resource, String mqttOnline, String name, String description, String commands,
			String geo, String set, String apiKey) {
		User user = accessController.checkApiKey(apiKey);
		if (user != null) {
			boolean processed = false;
			if (mqttOnline != null) {
				processed = true;
				boolean online = Boolean.parseBoolean(mqttOnline);
				if (online) {
					resourceController.setMqttResourceOnline(resource);
				} else {
					resourceController.setMqttResourceOffline(resource);
				}
			}

			if (name != null) {
				processed = true;
				resourceController.setResourceName(resource, name, sessionController.getUser());
			}
			if (description != null) {
				processed = true;
				resourceController.setResourceDescription(resource, description, sessionController.getUser());
			}
			if (commands != null) {
				processed = true;
				String[] commandString = commands.split(":");
				HashSet<String> commandsForResource = new HashSet<String>();
				for (String cmd : commandString) {
					commandsForResource.add(cmd);
				}
				resourceController.setCommands(resource, commandsForResource);
			}
			if (geo != null) {
				processed = true;
				String[] geoString = geo.split(":");
				resourceController.setCoordinates(resource, geoString[0], geoString[1]);
			}

			if (set != null) {
				processed = true;
				String[] nameValuePair = set.split(":");
				resourceController.addMqttValueForResource(resource, nameValuePair[0], nameValuePair[1]);
			}

			if (!processed) {
				return createNegativeResponse("ERROR: None of the given resource property names found! Nothing changed.");
			} else {
				return createPositiveResponse("OK");
			}

		} else
			throw new NotAuthorizedException(Response.serverError());
	}

	@POST
	@Path("create")
	@Produces(MediaType.APPLICATION_JSON)
	public Response createResource(@QueryParam(value = "apiKey") String apiKey, @QueryParam(value = "uuid") String uuid,
			@QueryParam(value = "departmentToken") String departmentToken, @QueryParam(value = "group") String group,
			@QueryParam(value = "name") String name, @QueryParam(value = "description") String description,
			@QueryParam(value = "slots") String slots, @QueryParam(value = "ip") String ip, @QueryParam(value = "commands") String commands,
			@QueryParam(value = "isAgent") boolean isAgent) {
		User user = accessController.checkApiKey(apiKey);
		if (user != null) {
			Department dept = companyController.getDepartmentByToken(departmentToken);
			if (authController.canCreate(dept.getId(), Resource.class, user)) {
				if (ip == null) {
					ip = "";
				}
				Resource resource = resourceController.createMqttResource(name, departmentToken, group, sessionController.getUser(),
						description, ip, Integer.parseInt(slots), false, true, isAgent, uuid, uuid + "/write", uuid + "/read");

				if (commands != null) {
					HashSet<String> cmds = new HashSet<String>();
					String[] commandsArray = commands.split(":");
					for (String cmd : commandsArray) {
						cmds.add(cmd);
					}
					resourceController.setCommands(resource, cmds);
				}

				if (resource != null) {
					return createPositiveResponse("Resource has been discovered and created.");
				} else {
					return createNegativeResponse("Resource could not be created!");
				}
			} else
				throw new NotAuthorizedException(Response.serverError());

		} else
			throw new NotAuthorizedException(Response.serverError());

	}

	@POST
	@Path("command")
	@Produces(MediaType.APPLICATION_JSON)
	public Response sendCommand(@QueryParam(value = "apiKey") String apiKey, @QueryParam(value = "uuid") String uuid,
			@QueryParam(value = "departmentToken") String departmentToken, @QueryParam(value = "group") String group,
			@QueryParam(value = "command") String command, @QueryParam(value = "value") String value) {
		User user = accessController.checkApiKey(apiKey);
		if (user != null) {
			Resource managedResource = resourceController.getResource(uuid, user);
			if (authController.canUpdate(managedResource, user)) {
				if (resourceController.createResourceReservation(managedResource, new Date(), new Date(System.currentTimeMillis() + 2000),
						user) != null) {
					resourceController.sendCommand(managedResource, command, value);
					return createPositiveResponse("Command has been send to resource: " + managedResource.getMqttUUID());
				} else {
					return createNegativeResponse("Not enough free slots available for resource: " + managedResource.getMqttUUID());
				}
			}
			throw new NotAuthorizedException(Response.serverError());
		}
		throw new NotAuthorizedException(Response.serverError());
	}

	@DELETE
	@Path("remove")
	@Produces(MediaType.APPLICATION_JSON)
	public Response removeResource(@QueryParam(value = "apiKey") String apiKey,
			@QueryParam(value = "departmentToken") String departmentToken, @QueryParam(value = "uuid") String uuid) {
		User user = accessController.checkApiKey(apiKey);
		if (user != null) {
			Department dept = companyController.getDepartmentByToken(departmentToken);
			if (dept != null) {
				Resource resource = resourceController.getResource(uuid, user);
				if (resource != null) {
					if (authController.canDelete(resource, user)) {
						resourceController.deleteResource(resource.getId());
						return createPositiveResponse("Resource has been removed.");
					} else {
						throw new NotAuthorizedException(Response.serverError());
					}
				} else
					throw new NotFoundException(Response.serverError().build());
			} else {
				throw new NotAuthorizedException(Response.serverError());
			}
		} else
			throw new NotAuthorizedException(Response.serverError());
	}

	private Response createPositiveResponse(String responseText) {
		return Response.status(200).entity("{\"response\" : \"" + responseText + "\"}").build();
	}

	private Response createNegativeResponse(String responseText) {
		return Response.status(405).entity("{\"response\" : \"" + responseText + "\"}").build();
	}
}
