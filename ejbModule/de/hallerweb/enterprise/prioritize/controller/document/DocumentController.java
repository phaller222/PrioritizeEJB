package de.hallerweb.enterprise.prioritize.controller.document;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

import de.hallerweb.enterprise.prioritize.controller.InitializationController;
import de.hallerweb.enterprise.prioritize.controller.LoggingController;
import de.hallerweb.enterprise.prioritize.controller.LoggingController.Action;
import de.hallerweb.enterprise.prioritize.controller.event.EventRegistry;
import de.hallerweb.enterprise.prioritize.controller.security.AuthorizationController;
import de.hallerweb.enterprise.prioritize.controller.security.SessionController;
import de.hallerweb.enterprise.prioritize.controller.security.UserRoleController;
import de.hallerweb.enterprise.prioritize.model.Department;
import de.hallerweb.enterprise.prioritize.model.PObject;
import de.hallerweb.enterprise.prioritize.model.document.Document;
import de.hallerweb.enterprise.prioritize.model.document.DocumentGroup;
import de.hallerweb.enterprise.prioritize.model.document.DocumentInfo;
import de.hallerweb.enterprise.prioritize.model.event.Event;
import de.hallerweb.enterprise.prioritize.model.event.PEventConsumerProducer;
import de.hallerweb.enterprise.prioritize.model.event.PObjectType;
import de.hallerweb.enterprise.prioritize.model.security.User;

/**
 * DocumentController.java - Controls the creation, modification and deletion of {@link DocumentInfo}, {@link DocumentGroup} and
 * {@link Document} objects.
 * 
 */
@Stateless
public class DocumentController extends PEventConsumerProducer {

	@PersistenceContext(unitName = "MySqlDS")
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
	@Inject
	EventRegistry eventRegistry;

	public DocumentInfo createDocument(String name, int groupId, User user, String mimeType, boolean encrypt, byte[] data, String changes) {
		int maxsize = Integer.parseInt(InitializationController.config.get(InitializationController.MAXIMUM_FILE_UPLOAD_SIZE));
		if (data.length > maxsize) {
			return null;
		}

		// check if document with that name already exists
		// within the provided DocumentGroup. If yes, don't create document and
		// return null.
		DocumentGroup managedDocumentGroup = em.find(DocumentGroup.class, groupId);
		if (findDocumentInfoByGroupAndName(managedDocumentGroup.getId(), name, user) != null) {
			return null;
		} else {

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
			document.setLastModifiedBy(user);

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

				logger.log(sessionController.getUser().getUsername(), "Document", Action.CREATE, documentInfo.getId(),
						" Document \"" + documentInfo.getCurrentDocument().getName() + "\" created.");

				return documentInfo;
			} else
				return null;
		}
	}

	public DocumentGroup createDocumentGroup(int departmentId, String name, User user) {
		// first get department and check if group (directory) already exists
		Department managedDepartment = em.find(Department.class, departmentId);

		if (findDocumentGroupByNameAndDepartment(managedDepartment.getId(), name, user) != null) {
			return null;
		} else {

			// Create DocumentGroup and return
			DocumentGroup documentGroup = new DocumentGroup();
			documentGroup.setName(name);
			documentGroup.setDepartment(managedDepartment);

			// ------------------ AUTH check --------------
			if (authController.canCreate(departmentId, DocumentInfo.class, user)) {

				em.persist(documentGroup);
				managedDepartment.addDocumentGroup(documentGroup);

				logger.log(sessionController.getUser().getUsername(), "DocumentGroup", Action.CREATE, documentGroup.getId(),
						" DocumentGroup \"" + documentGroup.getName() + "\" created.");

				return documentGroup;
			} else
				return null;
		}
	}

	public List<DocumentInfo> getDocumentInfosInDocumentGroup(int documentGroupId, User user) {
		Query query = em.createNamedQuery("findDocumentInfosByDocumentGroup");
		query.setParameter("dgid", documentGroupId);

		List<DocumentInfo> result = (List<DocumentInfo>) query.getResultList();
		if (!result.isEmpty()) {
			// ------------- AUTH check ----------------
			if (authController.canRead(result.get(0), user)) {
				return result;
			} else
				return null;
		} else
			return null;
	}

	public DocumentInfo getDocumentInfo(int id, User user) {
		Query query = em.createNamedQuery("findDocumentInfoById");
		query.setParameter("docInfoId", id);
		DocumentInfo info = (DocumentInfo) query.getSingleResult();

		// ------------------ AUTH check ---------------
		if (authController.canRead(info, user)) {
			return info;
		} else
			return null;
	}

	public List<DocumentInfo> getAllDocumentInfos(User user) {
		Query query = em.createNamedQuery("findAllDocumentInfos");
		return query.getResultList();
	}

	public Document getDocument(int id, User user) {
		Query query = em.createNamedQuery("findDocumentById");
		query.setParameter("docId", id);
		return (Document) query.getSingleResult();
	}

	public Document getDocumentByTag(String tag, User user) {
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
		DocumentGroup gr = (DocumentGroup) groups.get(0);
		if (authController.canRead(gr, user)) {
			return groups;
		} else {
			return new ArrayList<DocumentGroup>();
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

			logger.log(sessionController.getUser().getUsername(), "Document", Action.DELETE, info.getId(),
					" Document \"" + info.getCurrentDocument().getName() + "\" deleted.");
		}
	}

	public void deleteDocumentGroup(int documentGroupId, User user) {
		DocumentGroup group = em.find(DocumentGroup.class, documentGroupId);
		// ------------------ AUTH check ---------------
		if (authController.canDelete(group, user)) {
			if (group.getDepartment().getDocumentGroups().size() > 1) {
				Set<DocumentInfo> documents = group.getDocuments();
				if (documents != null) {
					for (DocumentInfo info : documents) {
						deleteDocumentInfo(info.getId(), user);
					}
				}

				group.getDepartment().getDocumentGroups().remove(group);
				group.setDepartment(null);
				em.remove(group);
				group = null;
				em.flush();
			}
		}
	}

	public DocumentInfo editDocument(DocumentInfo info, Document newDocumentData, byte[] data, String mimeType, User user,
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

			// Fire events for changed properties if configured.
			if (InitializationController.getAsBoolean(InitializationController.FIRE_DOCUMENT_EVENTS)) {
				Document current = managedInfo.getCurrentDocument();
				if (!current.getName().equals(newDocumentData.getName())) {
					this.raiseEvent(PObjectType.DOCUMENTINFO, managedInfo.getId(), Document.PROPERTY_NAME, current.getName(), newDocumentData.getName(),
							InitializationController.getAsInt(InitializationController.EVENT_DEFAULT_TIMEOUT));
				}
				if (!current.getMimeType().equals(newDocumentData.getMimeType())) {
					this.raiseEvent(PObjectType.DOCUMENTINFO, managedInfo.getId(), Document.PROPERTY_MIMETYPE, current.getMimeType(),
							newDocumentData.getMimeType(),
							InitializationController.getAsInt(InitializationController.EVENT_DEFAULT_TIMEOUT));
				}
				if (!current.getChanges().equals(newDocumentData.getChanges())) {
					this.raiseEvent(PObjectType.DOCUMENTINFO, managedInfo.getId(), Document.PROPERTY_CHANGES, current.getChanges(),
							newDocumentData.getChanges(),
							InitializationController.getAsInt(InitializationController.EVENT_DEFAULT_TIMEOUT));
				}
				if (!current.isEncrypted() == newDocumentData.isEncrypted()) {
					this.raiseEvent(PObjectType.DOCUMENTINFO, managedInfo.getId(), Document.PROPERTY_ENCRYPTED,String.valueOf(current.isEncrypted()),
							String.valueOf(newDocumentData.isEncrypted()),
							InitializationController.getAsInt(InitializationController.EVENT_DEFAULT_TIMEOUT));
				}
			}

			// Then edit the DocumentInfo information
			if (managedInfo.getRecentDocuments() == null) {
				managedInfo.setRecentDocuments(new TreeSet<Document>());
			}
			managedInfo.getRecentDocuments().add(managedInfo.getCurrentDocument());
			managedInfo.setCurrentDocument(document);
			managedInfo.setLocked(false);

			em.persist(document);
			em.flush();

			logger.log(sessionController.getUser().getUsername(), "Document", Action.UPDATE, managedInfo.getId(),
					" Document \"" + managedInfo.getCurrentDocument().getName() + "\" changed.");

			return managedInfo;
		} else
			return null;
	}

	public Document setDocumentTag(Document document, String tag, User user) {
		Document managedDocument = em.find(Document.class, document.getId());
		managedDocument.setTag(tag);
		if (tag == null || tag.isEmpty()) {
			logger.log(sessionController.getUser().getUsername(), "Document", Action.UPDATE, document.getId(),
					"Tag " + tag + " for Document \"" + document.getName() + "\" has been removed.");
		} else {
			logger.log(sessionController.getUser().getUsername(), "Document", Action.UPDATE, document.getId(),
					" Document \"" + document.getName() + "\" has been tagged: " + tag + ".");
		}

		this.raiseEvent(PObjectType.DOCUMENTINFO, managedDocument.getId(), Document.PROPERTY_TAG, "", managedDocument.getTag(),
				InitializationController.getAsInt(InitializationController.EVENT_DEFAULT_TIMEOUT));

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
			SortedSet<Document> recentDocuments = (SortedSet<Document>) managedDocumentInfo.getRecentDocuments();
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

	public void raiseEvent(PObjectType type, int id, String name, String oldValue, String newValue, long lifetime) {
		if (InitializationController.getAsBoolean(InitializationController.FIRE_DOCUMENT_EVENTS)) {
			Event evt = eventRegistry.getEventBuilder().newEvent().setSourceType(type).setSourceId(id).setOldValue(oldValue)
					.setNewValue(newValue).setPropertyName(name).setLifetime(lifetime).getEvent();
			eventRegistry.addEvent(evt);
		}
	}

	@Override
	public void consumeEvent(PObject destination, Event evt) {
		System.out.println("Object " + evt.getSourceType() + " with ID " + evt.getSourceId() + " raised event: " + evt.getPropertyName()
				+ " with new Value: " + evt.getNewValue() + "--- Document listening: " + destination.getClass());

	}

}
