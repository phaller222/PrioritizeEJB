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
package de.hallerweb.enterprise.prioritize.controller.document;

import de.hallerweb.enterprise.prioritize.controller.InitializationController;
import de.hallerweb.enterprise.prioritize.controller.LoggingController;
import de.hallerweb.enterprise.prioritize.controller.LoggingController.Action;
import de.hallerweb.enterprise.prioritize.controller.security.AuthorizationController;
import de.hallerweb.enterprise.prioritize.controller.security.SessionController;
import de.hallerweb.enterprise.prioritize.controller.security.UserRoleController;
import de.hallerweb.enterprise.prioritize.model.Department;
import de.hallerweb.enterprise.prioritize.model.document.Document;
import de.hallerweb.enterprise.prioritize.model.document.DocumentGroup;
import de.hallerweb.enterprise.prioritize.model.document.DocumentInfo;
import de.hallerweb.enterprise.prioritize.model.security.User;
import jakarta.ejb.EJB;
import jakarta.ejb.Stateless;
import jakarta.enterprise.context.ContextNotActiveException;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;

import java.util.*;
import java.util.logging.Level;
import java.util.logging.LogManager;

/**
 * DocumentController.java - Handles the creation, modification and deletion of {@link DocumentInfo}, {@link DocumentGroup} and
 * {@link Document} objects.
 */
@Stateless
public class DocumentController {

    @PersistenceContext
    EntityManager em;
    @EJB
    UserRoleController userRoleController;
    @EJB
    AuthorizationController authController;
    @EJB
    InitializationController initController;
    @EJB
    LoggingController logger;
    @Inject
    SessionController sessionController;

    static final String DOCUMENT_LITERAL = "Document";

    public DocumentInfo createDocumentInfo(String name, int groupId, User user, String mimeType, boolean encrypt, byte[] data, String changes) {
        int maxsize = Integer.parseInt(InitializationController.config.get(InitializationController.MAXIMUM_FILE_UPLOAD_SIZE));
        if (data.length > maxsize) {
            return null;
        }

        // check if document with that name already exists
        // within the provided DocumentGroup. If yes, don't create document and
        // return null.
        DocumentGroup managedDocumentGroup = em.find(DocumentGroup.class, groupId);
        if (managedDocumentGroup == null) {
            return null;
        }
        if (findDocumentInfoByGroupAndName(managedDocumentGroup.getId(), name, user) == null) {
            // Then create the Document
            Document document = new Document();
            document.setName(name);
            document.setMimeType(mimeType);
            document.setEncrypted(encrypt);
            document.setChanges(changes);
            if (encrypt) {
                document.setEncryptedBy(user);
            }
            document.setLastModified(new Date());
            document.setLastModifiedBy(userRoleController.findUserByUsername(user.getUsername(), authController.getSystemUser()));

            // New document, so version is always 1
            document.setVersion(1);
            document.setData(data);

            // Then create the DocumentInfo
            DocumentInfo documentInfo = new DocumentInfo();
            documentInfo.setCurrentDocument(document);
            documentInfo.setLocked(false);
            documentInfo.setDocumentGroup(managedDocumentGroup);

            // ------------------ AUTH check ---------------------
            if (authController.canCreate(documentInfo, user)) {
                managedDocumentGroup.addDocument(documentInfo);
                em.persist(document);
                em.persist(documentInfo);
                try {
                    logger.log(user.getUsername(), DOCUMENT_LITERAL, Action.CREATE, documentInfo.getId(),
                            " " + DOCUMENT_LITERAL + " \"" + documentInfo.getCurrentDocument().getName() + "\" created.");
                } catch (ContextNotActiveException ex) {
                    LogManager.getLogManager().getLogger(getClass().getName()).log(Level.WARNING, ex.getMessage());
                }
                return documentInfo;
            } else {
                return null;
            }
        } else {
            return null;
        }
    }

    public DocumentGroup createDocumentGroup(int departmentId, String name, User user) {
        // first get department and check if group (directory) already exists
        Department managedDepartment = em.find(Department.class, departmentId);

        if (findDocumentGroupByNameAndDepartment(managedDepartment.getId(), name, user) == null) {

            // Create DocumentGroup and return
            DocumentGroup documentGroup = new DocumentGroup();
            documentGroup.setName(name);
            documentGroup.setDepartment(managedDepartment);

            // ------------------ AUTH check --------------
            if (authController.canCreate(departmentId, documentGroup, user)) {

                em.persist(documentGroup);
                managedDepartment.addDocumentGroup(documentGroup);
                try {
                    logger.log(sessionController.getUser().getUsername(), "DocumentGroup", Action.CREATE, documentGroup.getId(),
                            " DocumentGroup \"" + documentGroup.getName() + "\" created.");
                } catch (Exception ex) {
                    LogManager.getLogManager().getLogger(getClass().getName()).log(Level.WARNING, ex.getMessage());
                }

                return documentGroup;
            } else {
                return null;
            }
        } else {
            return null;
        }
    }

    public List<DocumentInfo> getDocumentInfosInDocumentGroup(int documentGroupId, User user) {
        Query query = em.createNamedQuery("findDocumentInfosByDocumentGroup");
        query.setParameter("dgid", documentGroupId);

        List<DocumentInfo> result = query.getResultList();
        if (result.isEmpty()) {
            return new ArrayList<>();
        } else {
            // ------------- AUTH check ----------------
            if (authController.canRead(result.get(0), user)) {
                return result;
            } else {
                return new ArrayList<>();
            }
        }
    }

    public List<Document> getCurrentDocumentsInDocumentGroup(int documentGroupId, User user) {
        Query query = em.createNamedQuery("findDocumentInfosByDocumentGroup");
        query.setParameter("dgid", documentGroupId);
        List<Document> currentDocuments = new ArrayList<>();

        List<DocumentInfo> result = query.getResultList();
        if (!result.isEmpty()) {
            // ------------- AUTH check ----------------
            if (authController.canRead(result.get(0), user)) {
                for (DocumentInfo docInfo : result) {
                    currentDocuments.add(docInfo.getCurrentDocument());
                }
                return currentDocuments;
            } else {
                return new ArrayList<>();
            }
        } else {
            return new ArrayList<>();
        }
    }

    public DocumentInfo getDocumentInfo(int id, User user) {
        Query query = em.createNamedQuery("findDocumentInfoById");
        query.setParameter("docInfoId", id);
        DocumentInfo info = (DocumentInfo) query.getSingleResult();

        // ------------------ AUTH check ---------------
        if (authController.canRead(info, user)) {
            return info;
        } else {
            return null;
        }
    }

    public DocumentInfo getDocumentInfoByDocumentId(int documentId, User user) {
        Query query = em.createNamedQuery("findDocumentInfoByDocumentId");
        query.setParameter("documentId", documentId);
        DocumentInfo docinfo = (DocumentInfo) query.getSingleResult();

        // ------------------ AUTH check ---------------
        if (authController.canRead(docinfo, user)) {
            return docinfo;
        } else {
            return null;
        }
    }

    public List<DocumentInfo> getAllDocumentInfos(User user) {
        if (authController.canRead(new DocumentInfo(), user)) {
            Query query = em.createNamedQuery("findAllDocumentInfos");
            return query.getResultList();
        } else {
            return new ArrayList<>();
        }
    }

    public Document getDocument(int id) {
        Query query = em.createNamedQuery("findDocumentById");
        query.setParameter("docId", id);
        return (Document) query.getSingleResult();
    }

    public Document getDocumentByTag(String tag) {
        Query query = em.createNamedQuery("findDocumentByTag");
        query.setParameter("docTag", tag);
        return (Document) query.getSingleResult();
    }

    public DocumentGroup getDocumentGroup(int id, User user) {
        Query query = em.createNamedQuery("findDocumentGroupById");
        query.setParameter("groupId", id);
        DocumentGroup group = (DocumentGroup) query.getSingleResult();
        if (authController.canRead(group, user)) {
            return (DocumentGroup) query.getSingleResult();
        } else {
            return null;
        }
    }

    public List<DocumentGroup> getDocumentGroupsForDepartment(int departmentId, User user) {
        Query query = em.createNamedQuery("findDocumentGroupsForDepartment");
        query.setParameter("deptId", departmentId);
        List<DocumentGroup> groups = query.getResultList();
        DocumentGroup gr = groups.get(0);
        if (authController.canRead(gr, user)) {
            return groups;
        } else {
            return new ArrayList<>();
        }
    }

    public void deleteDocumentInfo(int documentInfoId, User user) {
        DocumentInfo info = em.find(DocumentInfo.class, documentInfoId);

        // ------------------ AUTH check --------------
        if (authController.canDelete(info, user)) {
            info.getDocumentGroup().removeDocument(info);

            // Delete DocumentInfo
            em.remove(info);
            em.flush();

            logger.log(user.getUsername(), DOCUMENT_LITERAL, Action.DELETE, info.getId(),
                    " " + DOCUMENT_LITERAL + "\"" + info.getCurrentDocument().getName() + "\" deleted.");
        }
    }

    public void deleteDocumentGroup(int documentGroupId, User user) {
        DocumentGroup group = em.find(DocumentGroup.class, documentGroupId);
        // ------------------ AUTH check ---------------
        if (authController.canDelete(group, user) && group.getDepartment().getDocumentGroups().size() > 1) {
            Set<DocumentInfo> documents = group.getDocuments();
            if (documents != null) {
                for (DocumentInfo info : documents) {
                    deleteDocumentInfo(info.getId(), user);
                }
            }

            group.getDepartment().getDocumentGroups().remove(group);
            group.setDepartment(null);
            em.remove(group);
            em.flush();

        }
    }

    public DocumentInfo editDocumentInfo(DocumentInfo info, Document newDocumentData, byte[] data, String mimeType, User user,
                                         boolean encrypt) {
        int maxsize = Integer.parseInt(InitializationController.config.get(InitializationController.MAXIMUM_FILE_UPLOAD_SIZE));
        if (data.length > maxsize) {
            return null;
        }

        // Edit the document
        DocumentInfo managedInfo = em.find(DocumentInfo.class, info.getId());

        // ------------------ AUTH check ---------------
        if (authController.canUpdate(managedInfo, user)) {
            Document document = new Document();
            document.setName(newDocumentData.getName());
            document.setMimeType(mimeType);
            document.setEncrypted(newDocumentData.isEncrypted());
            document.setChanges(newDocumentData.getChanges());
            if (encrypt) {
                document.setEncryptedBy(user);
            }
            document.setLastModified(new Date());
            document.setLastModifiedBy(user);

            // the document is beeing changed, so increase version number
            document.setVersion(info.getCurrentDocument().getVersion() + 1);
            document.setData(data);

            // Then edit the DocumentInfo information
            if (managedInfo.getRecentDocuments() == null) {
                managedInfo.setRecentDocuments(new TreeSet<>());
            }
            managedInfo.getRecentDocuments().add(managedInfo.getCurrentDocument());
            managedInfo.setCurrentDocument(document);
            managedInfo.setLocked(false);

            em.persist(document);
            em.flush();
            try {
                logger.log(user.getUsername(), DOCUMENT_LITERAL, Action.UPDATE, managedInfo.getId(),
                        " " + DOCUMENT_LITERAL + " \"" + managedInfo.getCurrentDocument().getName() + "\" changed.");
            } catch (ContextNotActiveException ex) {
                // Omit logging here.
            }

            return managedInfo;
        } else {
            return null;
        }
    }


    public Document setDocumentTag(Document document, String tag) {
        Document managedDocument = em.find(Document.class, document.getId());
        managedDocument.setTag(tag);
        if (tag == null || tag.isEmpty()) {
            logger.log(sessionController.getUser().getUsername(), DOCUMENT_LITERAL, Action.UPDATE, document.getId(),
                    "Tag " + tag + " for " + DOCUMENT_LITERAL + " \"" + document.getName() + "\" has been removed.");
        } else {
            logger.log(sessionController.getUser().getUsername(), DOCUMENT_LITERAL, Action.UPDATE, document.getId(),
                    " Document \"" + document.getName() + "\" has been tagged: " + tag + ".");
        }
        return managedDocument;
    }

    /**
     * Sets a Document object as most recent version of a DocumentInfo. Also removes the Document from the recent documents list.
     *
     * @param document
     * @param documentInfo
     * @param user
     * @return
     */
    public Document setDocumentAsCurrentVersion(Document document, DocumentInfo documentInfo, User user) {
        Document managedDocument = em.find(Document.class, document.getId());
        DocumentInfo managedDocumentInfo = em.find(DocumentInfo.class, documentInfo.getId());

        if (authController.canUpdate(managedDocumentInfo, user)) {

            Document newDoc = createDocumentCopy(managedDocument, (managedDocumentInfo.getCurrentDocument().getVersion()) + 1);
            Document managedNewDoc = em.find(Document.class, newDoc.getId());
            Set<Document> recentDocuments = managedDocumentInfo.getRecentDocuments();
            recentDocuments.add(managedDocumentInfo.getCurrentDocument());
            managedDocumentInfo.setCurrentDocument(managedNewDoc);
            managedDocumentInfo.setRecentDocuments(recentDocuments);

            return managedNewDoc;
        } else {
            return null;
        }
    }

    public DocumentInfo deleteDocumentFromDocumentInfo(Document doc, DocumentInfo docInfo, User user) {
        Document managedDocument = em.find(Document.class, doc.getId());
        DocumentInfo managedDocumentInfo = em.find(DocumentInfo.class, docInfo.getId());
        if (authController.canDelete(managedDocumentInfo, user)) {
            managedDocumentInfo.getRecentDocuments().remove(managedDocument);
            em.remove(managedDocument);
            return managedDocumentInfo;
        } else {
            return null;
        }
    }

    private Document createDocumentCopy(Document doc, int newVersion) {
        Document document = new Document();
        document.setName(doc.getName());
        document.setMimeType(doc.getMimeType());
        document.setEncrypted(doc.isEncrypted());
        document.setChanges(doc.getChanges() + "[COPY OF VERSION " + doc.getVersion() + "]");
        document.setEncryptedBy(doc.getEncryptedBy());
        document.setLastModified(new Date());
        document.setLastModifiedBy(doc.getLastModifiedBy());

        document.setVersion(newVersion);
        document.setData(doc.getData());

        em.persist(document);
        return document;
    }

    public DocumentInfo findDocumentInfoByGroupAndName(int groupId, String documentName, User user) {
        Query query = em.createNamedQuery("findDocumentInfoByDocumentGroupAndName");
        query.setParameter("groupId", groupId);
        query.setParameter("name", documentName);
        try {
            DocumentInfo info = (DocumentInfo) query.getSingleResult();
            if (authController.canRead(info, user)) {
                return info;
            } else {
                return null;
            }
        } catch (NoResultException ex) {
            return null;
        }
    }

    public DocumentGroup findDocumentGroupByNameAndDepartment(int deptId, String groupName, User user) {
        Query query = em.createNamedQuery("findDocumentGroupByNameAndDepartment");
        query.setParameter("deptId", deptId);
        query.setParameter("name", groupName);
        try {
            DocumentGroup group = (DocumentGroup) query.getSingleResult();
            if (authController.canRead(group, user)) {
                return group;
            } else {
                return null;
            }
        } catch (NoResultException ex) {
            return null;
        }
    }
}
