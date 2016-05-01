package de.hallerweb.enterprise.prioritize.view.document;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.enterprise.context.SessionScoped;
import javax.faces.bean.ManagedBean;
import javax.inject.Inject;
import javax.inject.Named;

import org.primefaces.event.FileUploadEvent;
import org.primefaces.model.DefaultStreamedContent;

import de.hallerweb.enterprise.prioritize.controller.CompanyController;
import de.hallerweb.enterprise.prioritize.controller.document.DocumentController;
import de.hallerweb.enterprise.prioritize.controller.resource.ResourceController;
import de.hallerweb.enterprise.prioritize.controller.security.AuthorizationController;
import de.hallerweb.enterprise.prioritize.controller.security.SessionController;
import de.hallerweb.enterprise.prioritize.controller.security.UserRoleController;
import de.hallerweb.enterprise.prioritize.controller.usersetting.ItemCollectionController;
import de.hallerweb.enterprise.prioritize.model.Department;
import de.hallerweb.enterprise.prioritize.model.document.Document;
import de.hallerweb.enterprise.prioritize.model.document.DocumentGroup;
import de.hallerweb.enterprise.prioritize.model.document.DocumentInfo;
import de.hallerweb.enterprise.prioritize.model.security.User;
import de.hallerweb.enterprise.prioritize.model.usersetting.ItemCollection;
import de.hallerweb.enterprise.prioritize.view.ViewUtilities;

/**
 * DocumentBean - JSF Backing-Bean to store information about documents.
 * 
 * <p>
 * Copyright: (c) 2014
 * </p>
 * <p>
 * Peter Haller
 * </p>
 * 
 * @author peter
 */
@Named
@SessionScoped
@ManagedBean
public class DocumentBean implements Serializable {

	private static final long serialVersionUID = -9021544577054017322L;

	@Inject
	SessionController sessionController;

	@EJB
	DocumentController controller;
	@EJB
	CompanyController companyController;
	@EJB
	UserRoleController userController;
	@EJB
	AuthorizationController authController;
	@EJB
	ResourceController resourceController;
	@EJB
	ItemCollectionController itemCollectionController;

	String selectedItemCollectionName;

	public String getSelectedItemCollectionName() {
		return selectedItemCollectionName;
	}

	public void setSelectedItemCollectionName(String selectedItemCollectionName) {
		this.selectedItemCollectionName = selectedItemCollectionName;
	}

	List<DocumentInfo> documentInfos; // Current List with documentInfo objects
										 // in the view.
	List<DocumentGroup> documentGroups; // Current list of document groups in
										 // the view
	List<Department> departments; // List of departments in the view
	String selectedDepartmentId; // Currently selected Department
	String selectedDocumentGroup; // Currently selected DocumentGroup
	String documentGroupName; // DocumentGroup to create
	Document document; // Current Document to create
	DocumentInfo documentInfo; // Current edited DocumentInfo
	String clientFilename; // Name of file to upload on the client machine
	String tmpMimeType = "text/plain"; // Temporary mimeType of document to
										 // upload (default: text/plain)
	byte[] tmpBytes = new byte[] {}; // Temporary byte array for new documents
									 // (upload)
	byte[] tmpBytesDownload = new byte[] {}; // Temporary byte array of document
											 // to download (download)
	private DefaultStreamedContent download; // download streamed content

	Department selectedDepartment;

	@PostConstruct
	public void init() {
		document = new Document();
		selectedDocumentGroup = new String();
	}

	@Named
	public void setDocumentInfo(DocumentInfo documentInfo) {
		this.documentInfo = documentInfo;
	}

	public DocumentInfo getDocumentInfo() {
		return documentInfo;
	}

	public List<DocumentInfo> getDocumentInfos() {
		if (!(this.selectedDocumentGroup == null) && (!this.selectedDocumentGroup.isEmpty())) {
			return controller.getDocumentInfosInDocumentGroup(Integer.parseInt(this.selectedDocumentGroup), sessionController.getUser());
		} else
			return null;
	}

	@Named
	public void setDocument(Document document) {
		this.document = document;
	}

	@Named
	public String setDocumentTag(Document doc, String tag) {
		Document managedDocument = controller.getDocument(doc.getId(), sessionController.getUser());
		controller.setDocumentTag(managedDocument, tag, sessionController.getUser());
		if (tag == null || tag.isEmpty()) {
			doc.setTag(null);
		}
		return "history";
	}

	@Named
	public Document getDocument() {
		return document;
	}

	@Named
	public String createDocument() {
		if (controller.createDocument(document.getName(), Integer.parseInt(selectedDocumentGroup), sessionController.getUser(), tmpMimeType,
				false, tmpBytes, "") != null) {
			return "documents";
		} else {
			ViewUtilities.addErrorMessage("name",
					"A document with the name " + document.getName() + " already exists in this document group!");
			return "documents";
		}
	}

	@Named
	public void setDocumentGroupName(String documentGroup) {
		this.documentGroupName = documentGroup;
	}

	public String getDocumentGroupName() {
		return documentGroupName;
	}

	@Named
	public List<Department> getDepartments() {
		return companyController.getAllDepartments();
	}

	@Named
	public String getSelectedDepartmentId() {
		return selectedDepartmentId;
	}

	public void setSelectedDepartmentId(String departmentId) {
		this.selectedDepartmentId = departmentId;
		if (this.documentGroups != null) {
			this.documentGroups.clear();
		}
		if ((departmentId != null) && (departmentId.length() > 0)) {
			this.documentGroups = controller.getDocumentGroupsForDepartment(Integer.parseInt(departmentId), sessionController.getUser());
			try {
				setSelectedDocumentGroup(String.valueOf(this.documentGroups.get(0).getId()));
			} catch (IndexOutOfBoundsException ex) {
				// can happen if no known document group is selected.
			}
			this.selectedDepartment = companyController.findDepartmentById(Integer.parseInt(departmentId));
		}

	}

	public Department getSelectedDepartment() {
		return selectedDepartment;
	}

	public void setSelectedDepartment(Department selectedDepartment) {
		this.selectedDepartment = selectedDepartment;
	}

	public String getSelectedDocumentGroup() {
		return selectedDocumentGroup;
	}

	@Named
	public void setSelectedDocumentGroup(String documentGroupId) {
		this.selectedDocumentGroup = documentGroupId;
		if (this.documentInfos != null) {
			this.documentInfos.clear();
		}
		this.documentInfos = controller.getDocumentInfosInDocumentGroup(Integer.parseInt(this.selectedDocumentGroup),
				sessionController.getUser());
	}

	public List<DocumentGroup> getDocumentGroups() {
		if ((selectedDepartmentId != null) && (selectedDepartmentId.length() > 0)) {
			List<DocumentGroup> groups = controller.getDocumentGroupsForDepartment(Integer.parseInt(selectedDepartmentId),
					sessionController.getUser());
			return groups;
		} else
			return new ArrayList<DocumentGroup>();

	}

	/**
	 * Creates a new {@link DocumentGroup}.
	 * 
	 * @return "documents".
	 */
	@Named
	public String createDocumentGroup() {
		if (controller.createDocumentGroup(Integer.parseInt(selectedDepartmentId), documentGroupName,
				sessionController.getUser()) != null) {
			init();
			return "documents";
		} else {
			ViewUtilities.addErrorMessage(null,
					"A document group with the name " + documentGroupName + " already exists in this department!");
			return "documents";
		}
	}

	public String deleteDocumentGroup() {
		controller.deleteDocumentGroup(
				controller.getDocumentGroup(Integer.parseInt(this.selectedDocumentGroup), sessionController.getUser()).getId(),
				sessionController.getUser());
		return "documents";
	}

	public String delete(DocumentInfo info) {
		controller.deleteDocumentInfo(info.getId(), sessionController.getUser());
		return "documents";
	}

	public String deleteDocumentVersion(Document doc) {
		this.documentInfo = controller.deleteDocumentFromDocumentInfo(doc, documentInfo, sessionController.getUser());
		if (documentInfo.getRecentDocuments().isEmpty()) {
			return "documents";
		} else
			return "history";
	}

	/**
	 * Calls "editdocument" for the given {@link DocumentInfo} object.
	 * 
	 * @param info
	 *            {@link DocumentInfo} object to be edited.
	 * @return
	 */
	@Named
	public String edit(DocumentInfo info) {
		this.clientFilename = null;
		this.documentInfo = info;
		this.document = documentInfo.getCurrentDocument();
		return "editdocument";
	}

	/**
	 * Calls the History of the given {@link DocumentInfo} object.
	 * 
	 * @param info
	 *            The {@link DocumentInfo} object to ge the history for.
	 * @return
	 */
	@Named
	public String history(DocumentInfo info) {
		this.documentInfo = info;
		return "history";
	}

	@Named
	public String setDocumentAsCurrent(Document doc) {
		controller.setDocumentAsCurrentVersion(doc, documentInfo, sessionController.getUser());
		return "documents";
	}

	/**
	 * Commit the edits of a document to the underlying database by using the
	 * {@link DocumentController}.
	 * 
	 * @return "documents"
	 */
	@Named
	public String commitEdits() {
		controller.editDocument(documentInfo, document, tmpBytes, tmpMimeType, sessionController.getUser(), false);
		return "documents";
	}

	// ---------------- File download and upload management

	public void setDownload(DefaultStreamedContent download) {
		this.download = download;
	}

	public DefaultStreamedContent getDownload() throws Exception {
		return download;
	}

	/**
	 * Prepare a download of current document (DocumentInfo)
	 * 
	 * @param id
	 * @throws Exception
	 */
	public void prepDownload(int id) throws Exception {
		DocumentInfo docToDownload = controller.getDocumentInfo(Integer.valueOf(id), sessionController.getUser());
		Document currentDocument = docToDownload.getCurrentDocument();

		ByteArrayInputStream in = new ByteArrayInputStream(currentDocument.getData(), 0, currentDocument.getData().length);

		setDownload(new DefaultStreamedContent(in, currentDocument.getMimeType(), currentDocument.getName()));
	}

	/**
	 * Prepare a download from the history (Document)
	 * 
	 * @param id
	 * @throws Exception
	 */
	public void prepDownloadHistory(int id) throws Exception {
		System.out.println("ID: " + id);
		Document docToDownload = controller.getDocument(Integer.valueOf(id), sessionController.getUser());

		ByteArrayInputStream in = new ByteArrayInputStream(docToDownload.getData(), 0, docToDownload.getData().length);

		setDownload(new DefaultStreamedContent(in, docToDownload.getMimeType(), docToDownload.getName()));
		System.out.println("PREP = " + docToDownload.getName());
	}

	public void handleFileUpload(FileUploadEvent event) {
		System.out.println("UPLOAd: " + event.getFile().getFileName());
		clientFilename = event.getFile().getFileName();
		try {

			// write the inputStream to data attribute
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			InputStream in = event.getFile().getInputstream();

			tmpMimeType = URLConnection.guessContentTypeFromName(clientFilename);
			if (tmpMimeType == null) {
				tmpMimeType = "application/unknown";
			}

			int read = 0;
			byte[] bytes = new byte[1024];

			while ((read = in.read(bytes)) != -1) {
				out.write(bytes, 0, read);
			}

			in.close();
			out.flush();
			out.close();

			tmpBytes = out.toByteArray();
			System.out.println("TempBytes; " + tmpBytes.length);

		} catch (IOException e) {
			System.out.println(e.getMessage());
		}
	}

	public String getClientFilename() {
		return clientFilename;
	}

	// ----------------------- Auth check methods ----------------------------------------------
	@Named
	public boolean canRead(DocumentInfo info) {
		if (info == null) {
			return false;
		}
		return authController.canRead(info, sessionController.getUser());
	}

	@Named
	public boolean canUpdate(DocumentInfo info) {
		if (info == null) {
			return false;
		}
		return authController.canUpdate(info, sessionController.getUser());
	}

	@Named
	public boolean canDelete(DocumentInfo info) {
		if (info == null) {
			return false;
		}
		return authController.canDelete(info, sessionController.getUser());
	}

	@Named
	public boolean canCreate() {
		try {
			return authController.canCreate(Integer.parseInt(this.selectedDepartmentId), DocumentInfo.class, sessionController.getUser());
		} catch (NumberFormatException ex) {
			return false;
		}
	}

	@Named
	public void addDocumentToItemCollection(DocumentInfo docInfo) {
		ItemCollection managedCollection = itemCollectionController.getItemCollection(sessionController.getUser(),
				selectedItemCollectionName);
		if (managedCollection != null) {
			DocumentInfo managedDocInfo = controller.getDocumentInfo(docInfo.getId(), sessionController.getUser());
			itemCollectionController.addDocumentInfo(managedCollection, managedDocInfo);
		}
	}

}
