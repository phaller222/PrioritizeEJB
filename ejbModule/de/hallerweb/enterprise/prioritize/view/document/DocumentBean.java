package de.hallerweb.enterprise.prioritize.view.document;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.enterprise.context.SessionScoped;
import javax.faces.bean.ManagedBean;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.inject.Named;
import javax.servlet.http.HttpServletRequest;

import org.primefaces.event.FileUploadEvent;
import org.primefaces.event.NodeCollapseEvent;
import org.primefaces.event.NodeExpandEvent;
import org.primefaces.model.DefaultStreamedContent;
import org.primefaces.model.DefaultTreeNode;
import org.primefaces.model.TreeNode;

import de.hallerweb.enterprise.prioritize.controller.CompanyController;
import de.hallerweb.enterprise.prioritize.controller.document.DocumentController;
import de.hallerweb.enterprise.prioritize.controller.security.AuthorizationController;
import de.hallerweb.enterprise.prioritize.controller.security.SessionController;
import de.hallerweb.enterprise.prioritize.controller.usersetting.ItemCollectionController;
import de.hallerweb.enterprise.prioritize.model.Company;
import de.hallerweb.enterprise.prioritize.model.Department;
import de.hallerweb.enterprise.prioritize.model.document.Document;
import de.hallerweb.enterprise.prioritize.model.document.DocumentGroup;
import de.hallerweb.enterprise.prioritize.model.document.DocumentInfo;
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
	AuthorizationController authController;
	@EJB
	ItemCollectionController itemCollectionController;

	String selectedItemCollectionName;

	TreeNode documentTreeRoot;
	TreeNode selectedNode;

	public TreeNode getSelectedNode() {
		return selectedNode;
	}

	public void setSelectedNode(TreeNode selectedNode) {
		this.selectedNode = selectedNode;
	}

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
		this.documentTreeRoot = createDocumentTree();
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
		} else {
			return null;
		}
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

	@Named
	public String edit(String documentInfoId) {
		return edit(controller.getDocumentInfo(Integer.parseInt(documentInfoId), sessionController.getUser()));
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
	public String history(String documentInfoId) {

		this.documentInfo = controller.getDocumentInfo(Integer.parseInt(documentInfoId), sessionController.getUser());
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
		Document docToDownload = controller.getDocument(Integer.valueOf(id), sessionController.getUser());

		ByteArrayInputStream in = new ByteArrayInputStream(docToDownload.getData(), 0, docToDownload.getData().length);

		setDownload(new DefaultStreamedContent(in, docToDownload.getMimeType(), docToDownload.getName()));
	}

	public void handleFileUpload(FileUploadEvent event) {
		clientFilename = event.getFile().getFileName();
		try {

			// write the inputStream to data attribute
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			InputStream in = event.getFile().getInputstream();

			tmpMimeType = event.getFile().getContentType();// URLConnection.guessContentTypeFromName(clientFilename);
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
	public boolean canRead(String documentInfoId) {
		try {
			return canRead(controller.getDocumentInfo(Integer.parseInt(documentInfoId), sessionController.getUser()));
		} catch (Exception ex) {
			return false;
		}
	}

	@Named
	public boolean canUpdate(DocumentInfo info) {
		if (info == null) {
			return false;
		}
		return authController.canUpdate(info, sessionController.getUser());
	}

	@Named
	public boolean canUpdate(String documentInfoId) {
		try {
			return canUpdate(controller.getDocumentInfo(Integer.parseInt(documentInfoId), sessionController.getUser()));
		} catch (Exception ex) {
			return false;
		}

	}

	@Named
	public boolean canDelete(DocumentInfo info) {
		if (info == null) {
			return false;
		}
		return authController.canDelete(info, sessionController.getUser());
	}

	@Named
	public boolean canDelete(String documentInfoId) {
		try {
			return canDelete(controller.getDocumentInfo(Integer.parseInt(documentInfoId), sessionController.getUser()));
		} catch (Exception ex) {
			return false;
		}
	}

	@Named
	public boolean canCreate() {
		try {
			return authController.canCreate(Integer.parseInt(this.selectedDepartmentId), new DocumentInfo(), sessionController.getUser());
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

	// --------------------------------- Client view ---------------------------------

	public TreeNode getDocumentTree() {
		return this.documentTreeRoot;
	}

	// Create Tree for documents view
	public TreeNode createDocumentTree() {
		TreeNode root = new DefaultTreeNode("My Documents", null);

		List<Company> companies = companyController.getAllCompanies();
		for (Company c : companies) {
			TreeNode company = new DefaultTreeNode(new DocumentTreeInfo(c.getName(), false, false, null, null), root);
			List<Department> departments = c.getDepartments();
			for (Department d : departments) {
				TreeNode department = new DefaultTreeNode(new DocumentTreeInfo(d.getName(), false, false, null, null), company);
				List<DocumentGroup> groups = d.getDocumentGroups();
				for (DocumentGroup g : groups) {
					if (authController.canRead(g, sessionController.getUser())) {
						TreeNode group = null;
						if (authController.canCreate(g, sessionController.getUser())) {
							group = new DefaultTreeNode(new DocumentTreeInfo(g.getName(), false, true, String.valueOf(g.getId()), null),
									department);
						} else {
							group = new DefaultTreeNode(new DocumentTreeInfo(g.getName(), false, false, null, null), department);
						}
						Set<DocumentInfo> documents = g.getDocuments();
						List<DocumentInfo> docList = new ArrayList<DocumentInfo>();
						docList.addAll(documents);
						Collections.sort(docList, new Comparator<DocumentInfo>() {
							@Override
							public int compare(DocumentInfo o1, DocumentInfo o2) {
								Integer id1 = o1.getId();
								Integer id2 = o2.getId();
								if (id1 == null && id2 == null) {
									return 0;
								} else if (id1 != null && id2 == null) {
									return -1;
								} else if (id1 == null && id2 != null) {
									return 1;
								} else {
									return o2.getCurrentDocument().getLastModified().compareTo(o1.getCurrentDocument().getLastModified());
								}
							}
						});
						for (DocumentInfo docInfo : docList) {
							if (authController.canRead(docInfo, sessionController.getUser())) {
								TreeNode documentInfoNode = new DefaultTreeNode(
										new DocumentTreeInfo(docInfo.getCurrentDocument().getName(), true, false, null, docInfo), group);
							}
						}

					}

				}

			}
		}
		return root;
	}

	public void updateDocumentTree() {
		if (isNewRequest()) {
			this.documentTreeRoot = createDocumentTree();
		}
	}

	public void nodeExpand(NodeExpandEvent event) {
		event.getTreeNode().setExpanded(true);
	}

	public void nodeCollapse(NodeCollapseEvent event) {
		event.getTreeNode().setExpanded(false);
	}

	public boolean isNewRequest() {
		final FacesContext fc = FacesContext.getCurrentInstance();
		final boolean getMethod = ((HttpServletRequest) fc.getExternalContext().getRequest()).getMethod().equals("GET");
		final boolean ajaxRequest = fc.getPartialViewContext().isAjaxRequest();
		final boolean validationFailed = fc.isValidationFailed();
		return getMethod && !ajaxRequest && !validationFailed;
	}

	public String createNewDocument() {
		if (selectedNode != null) {
			if (selectedNode.isLeaf()) {
				return "documents";
			} else
				return "history";
		} else
			return null;
	}

	public void updateDocumentGroupId(String groupId) {
		this.selectedDocumentGroup = groupId;
	}

	public String goBack() {
		return "documents";
	}

}
