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

import javax.ejb.EJB;
import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
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
 * @author peter REST-Service to create, update and delete {@link Resource} objects.
 */
@RequestScoped
@Path("v1/documents")
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
     * HTTP/1.1 200 OK
     * @apiError NotAuthorized  APIKey incorrect.
     */
    @GET
    @Path("list/{departmentToken}/{group}")
    @Produces(MediaType.APPLICATION_JSON)
    public List<DocumentInfo> getDocuments(@PathParam(value = "departmentToken") String departmentToken,
                                           @PathParam(value = "group") String group, @QueryParam(value = "apiKey") String apiKey) {
        User user = accessController.checkApiKey(apiKey);
        if (user == null) {
            throw new NotAuthorizedException(Response.serverError());
        } else {
            Department dept = companyController.getDepartmentByToken(departmentToken, user);
            if (dept != null) {
                DocumentGroup documentGroup = documentController.findDocumentGroupByNameAndDepartment(dept.getId(), group, user);
                if (documentGroup == null) {
                    throw new NotFoundException(createNegativeResponse("Document group with name " + group + "not found!"));
                } else {
                    if (authController.canRead(documentGroup, user)) {
                        return documentController.getDocumentInfosInDocumentGroup(documentGroup.getId(), user);
                    } else {
                        throw new NotAuthorizedException(Response.serverError());
                    }
                }

            } else {
                throw new NotFoundException(createNegativeResponse("Department not found or department token invalid!"));
            }
        }
    }

    /**
     * @api {get} /documents/{departmentToken}/groups?apiKey={apiKey} getDocumentGroups
     * @apiName getDocumentGroups
     * @apiGroup /documents
     * @apiDescription gets all document groups (metadata only) within the given department
     * @apiParam {String} apiKey The API-Key of the user accessing the service.
     * @apiParam {String} departmentToken The department token of the department.
     * @apiSuccess {Document} documents JSON DocumentInfo-Objects with information about found documents.
     * @apiSuccessExample Success-Response:
     * HTTP/1.1 200 OK
     * @apiError NotAuthorized  APIKey incorrect.
     */
    @GET
    @Path("list/{departmentToken}/groups")
    @Produces(MediaType.APPLICATION_JSON)
    public List<DocumentGroup> getDocumentGroups(@PathParam(value = "departmentToken") String departmentToken,
                                                 @QueryParam(value = "apiKey") String apiKey) {
        User user = accessController.checkApiKey(apiKey);
        if (user != null) {
            Department dept = companyController.getDepartmentByToken(departmentToken, user);
            if (dept == null) {
                throw new NotFoundException(createNegativeResponse("Department not found or department token invalid!"));
            } else {
                List<DocumentGroup> groups = documentController.getDocumentGroupsForDepartment(dept.getId(), user);
                return groups != null ? groups : new ArrayList<>();
            }
        } else {
            throw new NotAuthorizedException(Response.serverError());
        }
    }

    /**
     * Returns all the documents in the given department and document group matching the search phrase.
     *
     * @api {get} /search/{departmentToken}/documentGroup?apiKey={apiKey}&phrase={phrase} searchDocuments
     * @apiName searchDocuments
     * @apiGroup /documents
     * @apiDescription Returns all the documents in the given department and document group matching the search phrase.
     * @apiParam {String} apiKey The API-Key of the user accessing the service.
     * @apiParam {String} departmentToken The department token of the department.
     * @apiParam {String} phrase The searchstring to searh for.
     * @apiSuccess {DocumentInfo} documents JSON DocumentInfo-Objects with information about found documents.
     * @apiSuccessExample Success-Response:
     * HTTP/1.1 200 OK
     * @apiError NotAuthorized  APIKey incorrect.
     */
    @GET
    @Path("search/{departmentToken}/{documentGroup}")
    @Produces(MediaType.APPLICATION_JSON)
    public Set<DocumentInfo> searchDocuments(@PathParam(value = "departmentToken") String departmentToken,
                                             @PathParam(value = "documentGroup") String documentGroup, @QueryParam(value = "apiKey") String apiKey,
                                             @QueryParam(value = "phrase") String phrase) {
        User user = accessController.checkApiKey(apiKey);
        if (user != null) {
            Department dept = companyController.getDepartmentByToken(departmentToken, user);
            if (dept == null) {
                throw new NotAuthorizedException(Response.serverError());
            } else {
                Set<DocumentInfo> searchResult = new HashSet<>();
                List<SearchResult> results = searchController.searchDocuments(phrase, user);
                for (SearchResult result : results) {
                    DocumentInfo documentInfo = (DocumentInfo) result.getResult();
                    DocumentGroup group = documentInfo.getDocumentGroup();
                    if (group.getName().equals(documentGroup) && authController.canRead(group, user)) {
                        searchResult.add(documentInfo);
                    } else {
                        throw new NotAuthorizedException(Response.serverError());
                    }
                }
                return searchResult;
            }
        } else {
            throw new NotAuthorizedException(Response.serverError());
        }
    }

    /**
     * Return the {@link Document} object with the given id.
     *
     * @api {get} /id/{id}?apiKey={apiKey} getDocumentById
     * @apiName getDocumentById
     * @apiGroup /documents
     * @apiDescription returns the document with the given id
     * @apiParam {String} apiKey The API-Key of the user accessing the service.
     * @apiSuccess {DocumentInfo} document  JSON DocumentInfo-Object.
     * @apiSuccessExample Success-Response:
     * HTTP/1.1 200 OK
     * @apiError NotAuthorized  APIKey incorrect.
     */
    @GET
    @Path("id/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getDocumentById(@PathParam(value = "id") String id, @QueryParam(value = "apiKey") String apiKey) {
        User user = accessController.checkApiKey(apiKey);
        DocumentInfo docInfo;
        if (user == null) {
            throw new NotAuthorizedException(Response.serverError());
        } else {
            try {
                docInfo = documentController.getDocumentInfo(Integer.parseInt(id), user);
            } catch (Exception ex) {
                return createNegativeResponse("Document not found!");
            }
            if (authController.canRead(docInfo, user)) {
                return Response.ok(docInfo, MediaType.APPLICATION_JSON).build();
            } else {
                throw new NotAuthorizedException(Response.serverError());
            }
        }
    }

    /**
     * Return the {@link Document} object with the given id.
     *
     * @api {get} /id/{id}/content?apiKey={apiKey} getDocumentContent
     * @apiName getDocumentContent
     * @apiGroup /documents
     * @apiDescription returns the content of the document with the given id
     * @apiParam {String} apiKey The API-Key of the user accessing the service.
     * @apiSuccess {byte[]} Document content
     * @apiSuccessExample Success-Response:
     * HTTP/1.1 200 OK
     * @apiError NotAuthorized  APIKey incorrect.
     */
    @GET
    @Path("id/{id}/content")
    public Response getDocumentContent(@PathParam(value = "id") String id, @QueryParam(value = "apiKey") String apiKey) {
        User user = accessController.checkApiKey(apiKey);
        DocumentInfo docInfo;
        if (user == null) {
            throw new NotAuthorizedException(Response.serverError());
        } else {
            try {
                docInfo = documentController.getDocumentInfo(Integer.parseInt(id), user);
            } catch (Exception ex) {
                return createNegativeResponse("Document not found!");
            }
            Document document = docInfo.getCurrentDocument();
            if (authController.canRead(docInfo, user)) {
                return Response.ok(document.getData(), document.getMimeType()).build();
            } else {
                throw new NotAuthorizedException(Response.serverError());
            }
        }
    }

    /**
     * @api {put} /id/{id}?apiKey={apiKey}&name={name}&mimeType={mimeType}&tag={tag}&changes={changes} setDocumentAttributes
     * @apiName setDocumentAttributes
     * @apiGroup /documents
     * @apiDescription Changes different attributes of a document
     * @apiParam {String} apiKey The API-Key of the user accessing the service.
     * @apiParam {String} name The new name of the document.
     * @apiParam {String} mimeType The new MIME-Type of the document.
     * @apiParam {String} tag The new document tag.
     * @apiParam {String} changes description of changes made.
     * @apiSuccess OK
     * @apiSuccessExample Success-Response:
     * HTTP/1.1 200 OK
     * @apiError NotAuthorized  APIKey incorrect.
     */
    @PUT
    @Path("id/{id}/")
    @Produces(MediaType.APPLICATION_JSON)
    public Response setDocumentAttributesById(@PathParam(value = "id") String id, @QueryParam(value = "mimeType") String mimeType,
                                              @QueryParam(value = "name") String name, @QueryParam(value = "tag") String tag, @QueryParam(value = "changes") String changes,
                                              @QueryParam(value = "apiKey") String apiKey) {
        User user = accessController.checkApiKey(apiKey);
        if (user == null) {
            throw new NotAuthorizedException(Response.serverError());
        } else {
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
                    documentController.editDocumentInfo(docInfo, doc, doc.getData(), mimeType, user, doc.isEncrypted());
                    return createPositiveResponse("OK");
                } else {
                    return createNegativeResponse("ERROR: None of the given document property names found! Nothing changed.");
                }

            } else {
                throw new NotAuthorizedException(Response.serverError());
            }
        }
    }

    /**
     * @api {delete} /remove?apiKey={apiKey}&departmentToken={departmenttoken}&id={id} deleteDocument
     * @apiName deleteDocument
     * @apiGroup /documents
     * @apiDescription Deletes a document
     * @apiParam {String} apiKey The API-Key of the user accessing the service.
     * @apiParam {String} id The id of the document to remove.
     * @apiParam {String} departmentToken department token of the department the document belongs to.
     * @apiSuccess OK
     * @apiSuccessExample Success-Response:
     * HTTP/1.1 200 OK
     * @apiError NotAuthorized  APIKey incorrect.
     */
    @DELETE
    @Path("remove/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response removeDocument(@QueryParam(value = "apiKey") String apiKey,
                                   @QueryParam(value = "departmentToken") String departmentToken, @PathParam(value = "id") String id) {
        User user = accessController.checkApiKey(apiKey);
        if (user != null) {
            Department dept = companyController.getDepartmentByToken(departmentToken, user);
            if (dept == null) {
                throw new NotAuthorizedException(Response.serverError());
            } else {
                DocumentInfo docInfo = null;
                try {
                    docInfo = documentController.getDocumentInfo(Integer.parseInt(id), user);
                } catch (Exception ex) {
                    return createNegativeResponse("Document not found!");
                }
                if (docInfo != null) {
                    if (authController.canDelete(docInfo, user)) {
                        documentController.deleteDocumentInfo(docInfo.getId(), user);
                    } else {
                        throw new NotAuthorizedException(Response.serverError());
                    }
                    return createPositiveResponse("Document has been removed.");
                } else
                    throw new NotFoundException(Response.serverError().build());
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
