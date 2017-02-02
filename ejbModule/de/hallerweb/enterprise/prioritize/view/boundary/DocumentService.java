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
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.NotAuthorizedException;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import de.hallerweb.enterprise.prioritize.controller.CompanyController;
import de.hallerweb.enterprise.prioritize.controller.document.DocumentController;
import de.hallerweb.enterprise.prioritize.controller.search.SearchController;
import de.hallerweb.enterprise.prioritize.controller.security.AuthorizationController;
import de.hallerweb.enterprise.prioritize.controller.security.RestAccessController;
import de.hallerweb.enterprise.prioritize.controller.security.SessionController;
import de.hallerweb.enterprise.prioritize.controller.security.UserRoleController;
import de.hallerweb.enterprise.prioritize.model.Department;
import de.hallerweb.enterprise.prioritize.model.document.Document;
import de.hallerweb.enterprise.prioritize.model.document.DocumentGroup;
import de.hallerweb.enterprise.prioritize.model.document.DocumentInfo;
import de.hallerweb.enterprise.prioritize.model.resource.Resource;
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
 * @author peter REST-Service to create, update and delete {@link Resource} objects.
 */
@RequestScoped
@Path("documents")
public class DocumentService {

	@EJB
	RestAccessController accessController;

	@EJB
	CompanyController companyController;

	@EJB
	DocumentController documentController;

	@EJB
	UserRoleController userRoleController;

	@EJB
	SearchController searchController;

	@Inject
	SessionController sessionController;

	@EJB
	AuthorizationController authController;

	/**
	 * @api {get} /documents/{departmentToken}/{group}/?apiKey={apiKey} getDocuments
	 * @apiName getDocuments
	 * @apiGroup /documents
	 * @apiDescription gets all documents (metadata only) within the given department and document group.
	 * @apiParam {String} apiKey The API-Key of the user accessing the service.
	 * @apiParam {String} departmentToken The department token of the department.
	 * @apiParam {String} group The document group name within this department to look for documents.
	 * @apiSuccess {Document} documents JSON DocumentInfo-Objects with information about found documents.
	 * @apiSuccessExample Success-Response:
	 *     HTTP/1.1 200 OK
	 *  [
	 *    {
	 *		"id" : 80,
	 *		"currentDocument" : {
	 *			"id" : 79,
	 *			"name" : "testdefault",
	 *			"version" : 1,
	 *			"mimeType" : "image/png",
	 *			"tag" : null,
	 *			"lastModified" : 1485693706000,
	 *			"encrypted" : false,
	 *			"changes" : "",
	 *			"lastModifiedBy" : {
	 *				"id" : 18,
	 *				"name" : "admin",
	 *				"username" : "admin",
	 *				"assignedTasks" : [ ]
	 *			},
	 *			"encryptedBy" : null
	 *		},
	 *		"recentDocuments" : [ ],
	 *		"locked" : false,
	 *		"lockedBy" : null,
	 * 	 }
	 * ]
	 *
	 * @apiError NotAuthorized  APIKey incorrect.
	 *
	 * @param departmentToken - The department token.
	 * @param group - The document group to look for dcuments.
	 * @return JSON object with documents in that department.
	 */
	@GET
	@Path("list/{departmentToken}/{group}")
	@Produces(MediaType.APPLICATION_JSON)
	public List<DocumentInfo> getDocuments(@PathParam(value = "departmentToken") String departmentToken,
			@PathParam(value = "group") String group, @QueryParam(value = "apiKey") String apiKey) {
		User user = accessController.checkApiKey(apiKey);
		if (user != null) {
			Department dept = companyController.getDepartmentByToken(departmentToken);
			if (dept != null) {
				List<DocumentGroup> groups = documentController.getDocumentGroupsForDepartment(dept.getId(), user);
				DocumentGroup groupToSearch = null;
				for (DocumentGroup groupInDept : groups) {
					if (groupInDept.getName().equals(group)) {
						groupToSearch = groupInDept;
					}
				}
				if (groupToSearch == null) {
					throw new NotFoundException(createNegativeResponse("Document group with name " + group + "not found!"));
				} else {
					if (authController.canRead(groupToSearch, user)) {
						List<DocumentInfo> documents = documentController.getDocumentInfosInDocumentGroup(groupToSearch.getId(), user);
						return documents;
					} else {
						throw new NotAuthorizedException(Response.serverError());
					}
				}

			} else {
				throw new NotFoundException(createNegativeResponse("Department not found or department token invalid!"));
			}
		} else {
			throw new NotAuthorizedException(Response.serverError());
		}
	}

	/**
	 * Returns all the documents in the given department matching the search phrase.
	 *
	 * @param departmentToken - The department token.
	 * @return JSON object with documents in that department.
	 */
	@GET
	@Path("search/{departmentToken}")
	@Produces(MediaType.APPLICATION_JSON)
	public Set<DocumentInfo> searchDocuments(@PathParam(value = "departmentToken") String departmentToken,
			@QueryParam(value = "apiKey") String apiKey, @QueryParam(value = "phrase") String phrase) {
		User user = accessController.checkApiKey(apiKey);
		if (user != null) {
			Department dept = companyController.getDepartmentByToken(departmentToken);
			if (dept != null) {
				Set<DocumentInfo> searchResult = new HashSet<DocumentInfo>();
				List<SearchResult> results = searchController.searchDocuments(phrase, user);
				for (SearchResult result : results) {
					DocumentInfo documentInfo = (DocumentInfo) result.getResult();
					DocumentGroup group = documentInfo.getDocumentGroup();
					if (authController.canRead(group, user)) {
						searchResult.add(documentInfo);
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
	 * Return the {@link Document} object with the given id.
	 *
	 * @param id - The id of the {@link Document}.
	 * @return {@link Document} - JSON Representation of the Document.
	 */
	@GET
	@Path("id/{id}")
	@Produces(MediaType.APPLICATION_JSON)
	public Document getDocumentByUuid(@PathParam(value = "id") String id, @QueryParam(value = "apiKey") String apiKey) {
		User user = accessController.checkApiKey(apiKey);
		if (user != null) {
			DocumentInfo docInfo = documentController.getDocumentInfo(Integer.parseInt(id), user);
			Document document = docInfo.getCurrentDocument();
			if (authController.canRead(docInfo, user)) {
				return document;
			} else {
				throw new NotAuthorizedException(Response.serverError());
			}
		} else {
			throw new NotAuthorizedException(Response.serverError());
		}
	}

	/**
	 * Return the {@link Document} object with the given id.
	 *
	 * @param id - The id of the {@link Document}.
	 * @return {@link Document} - JSON Representation of the Document.
	 */
	@GET
	@Path("id/{id}/content")
	public Response getDocumentContent(@PathParam(value = "id") String id, @QueryParam(value = "apiKey") String apiKey) {
		User user = accessController.checkApiKey(apiKey);
		if (user != null) {
			DocumentInfo docInfo = documentController.getDocumentInfo(Integer.parseInt(id), user);
			Document document = docInfo.getCurrentDocument();
			if (authController.canRead(docInfo, user)) {
				return Response.ok(document.getData(), document.getMimeType()).build();
			} else {
				throw new NotAuthorizedException(Response.serverError());
			}
		} else {
			throw new NotAuthorizedException(Response.serverError());
		}
	}

	@PUT
	@Path("id/{id}/current/")
	@Produces(MediaType.APPLICATION_JSON)
	public Response setResourceAttributesByUuid(@PathParam(value = "id") String id, @QueryParam(value = "mimeType") String mimeType,
			@QueryParam(value = "name") String name, @QueryParam(value = "tag") String tag, @QueryParam(value = "changes") String changes,
			@QueryParam(value = "apiKey") String apiKey) {
		User user = accessController.checkApiKey(apiKey);
		if (user != null) {
			DocumentInfo docInfo = documentController.getDocumentInfo(Integer.parseInt(id), user);
			if (authController.canUpdate(docInfo, user)) {
				boolean processed = false;
				Document doc = docInfo.getCurrentDocument();
				if (name != null) {
					doc.setName(name);
					processed = true;
				}
				if (mimeType != null) {
					doc.setMimeType(mimeType);
					processed = true;
				}
				if (tag != null) {
					doc.setTag(tag);
					processed = true;
				}
				if (changes != null) {
					doc.setChanges(changes);
					processed = true;
				}
				if (processed) {
					documentController.editDocument(docInfo, doc, doc.getData(), mimeType, user, doc.isEncrypted());
					return createPositiveResponse("OK");
				} else {
					return createNegativeResponse("ERROR: None of the given document property names found! Nothing changed.");
				}

			} else {
				throw new NotAuthorizedException(Response.serverError());
			}
		} else {
			throw new NotAuthorizedException(Response.serverError());
		}
	}

	@DELETE
	@Path("remove")
	@Produces(MediaType.APPLICATION_JSON)
	public Response removeDocument(@QueryParam(value = "apiKey") String apiKey,
			@QueryParam(value = "departmentToken") String departmentToken, @QueryParam(value = "id") String id) {
		User user = accessController.checkApiKey(apiKey);
		if (user != null) {
			Department dept = companyController.getDepartmentByToken(departmentToken);
			if (dept != null) {
				DocumentInfo docInfo = documentController.getDocumentInfo(Integer.parseInt(id), user);
				if (docInfo != null) {
					if (authController.canDelete(docInfo, user)) {
						documentController.deleteDocumentInfo(docInfo.getId(), user);
					} else {
						throw new NotAuthorizedException(Response.serverError());
					}
					return createPositiveResponse("Document has been removed.");
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
