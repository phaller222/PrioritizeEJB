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

package de.hallerweb.enterprise.prioritize.view.document;

import de.hallerweb.enterprise.prioritize.controller.CompanyController;
import de.hallerweb.enterprise.prioritize.controller.document.DocumentController;
import de.hallerweb.enterprise.prioritize.controller.security.AuthorizationController;
import de.hallerweb.enterprise.prioritize.controller.security.SessionController;
import de.hallerweb.enterprise.prioritize.controller.usersetting.ItemCollectionController;
import de.hallerweb.enterprise.prioritize.model.Department;
import de.hallerweb.enterprise.prioritize.model.document.Document;
import de.hallerweb.enterprise.prioritize.model.document.DocumentGroup;
import de.hallerweb.enterprise.prioritize.model.document.DocumentInfo;
import de.hallerweb.enterprise.prioritize.model.usersetting.ItemCollection;
import de.hallerweb.enterprise.prioritize.view.BasicTimelineController;
import de.hallerweb.enterprise.prioritize.view.ViewUtilities;
import jakarta.annotation.PostConstruct;
import jakarta.ejb.EJB;
import jakarta.enterprise.context.SessionScoped;
import jakarta.faces.context.FacesContext;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.servlet.http.HttpServletRequest;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

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
public class DocumentBean implements Serializable {

    private static final long serialVersionUID = -9021544577054017322L;
    private static final String NAVIGATION_HISTORY = "history";
    private static final String NAVIGATION_DOCUMENTS = "documents";


    @Inject
    SessionController sessionController;

    @EJB
    transient DocumentController controller;
    @EJB
    transient CompanyController companyController;

    @EJB
    transient AuthorizationController authController;
    @EJB
    transient ItemCollectionController itemCollectionController;

    @Inject
    BasicTimelineController timelineBean;

    String selectedItemCollectionName;


    transient List<DocumentInfo> documentInfos; // Current List with documentInfo objects
    // in the view.
    transient List<DocumentGroup> documentGroups; // Current list of document groups in
    // the view
    transient List<Department> departments; // List of departments in the view
    String selectedDepartmentId; // Currently selected Department
    String selectedDocumentGroup; // Currently selected DocumentGroup
    String documentGroupName; // DocumentGroup to create
    transient Document document; // Current Document to create
    transient DocumentInfo documentInfo; // Current edited DocumentInfo
    String clientFilename; // Name of file to upload on the client machine
    String tmpMimeType = "text/plain"; // Temporary mimeType of document to
    // upload (default: text/plain)
    byte[] tmpBytes = new byte[]{}; // Temporary byte array for new documents
    // (upload)
    byte[] tmpBytesDownload = new byte[]{}; // Temporary byte array of document


    transient Department selectedDepartment;

    @PostConstruct
    public void init() {
        document = new Document();
        selectedDocumentGroup = "";

    }


    public String getSelectedItemCollectionName() {
        return selectedItemCollectionName;
    }

    public void setSelectedItemCollectionName(String selectedItemCollectionName) {
        this.selectedItemCollectionName = selectedItemCollectionName;
    }


    @Named
    public void setDocumentInfo(DocumentInfo documentInfo) {
        this.documentInfo = documentInfo;
    }

    public DocumentInfo getDocumentInfo() {
        return documentInfo;
    }

    public List<DocumentInfo> getDocumentInfos() {
        if ((this.selectedDocumentGroup != null) && (!this.selectedDocumentGroup.isEmpty())) {
            return controller.getDocumentInfosInDocumentGroup(Integer.parseInt(this.selectedDocumentGroup), sessionController.getUser());
        } else {
            return new ArrayList<>();
        }
    }

    @Named
    public void setDocument(Document document) {
        this.document = document;
    }


    @Named
    public Document getDocument() {
        return document;
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
        return companyController.getAllDepartments(sessionController.getUser());
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
            return controller.getDocumentGroupsForDepartment(Integer.parseInt(selectedDepartmentId), sessionController.getUser());
        } else {
            return new ArrayList<>();
        }

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
        } else {
            ViewUtilities.addErrorMessage(null,
                "A document group with the name " + documentGroupName + " already exists in this department!");
        }
        return NAVIGATION_DOCUMENTS;
    }

    public String deleteDocumentGroup() {
        controller.deleteDocumentGroup(
            controller.getDocumentGroup(Integer.parseInt(this.selectedDocumentGroup), sessionController.getUser()).getId(),
            sessionController.getUser());
        return NAVIGATION_DOCUMENTS;
    }


    /**
     * Calls "editdocument" for the given {@link DocumentInfo} object.
     *
     * @param info {@link DocumentInfo} object to be edited.
     * @return page to be dsplayed next editdocument.xhtml
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
     * @param info The {@link DocumentInfo} object to ge the history for.
     * @return next page
     */
    @Named
    public String history(DocumentInfo info) {
        this.documentInfo = info;
        return NAVIGATION_HISTORY;
    }

    @Named
    public String history(String documentInfoId) {

        this.documentInfo = controller.getDocumentInfo(Integer.parseInt(documentInfoId), sessionController.getUser());
        return NAVIGATION_HISTORY;
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


    private void sortDocumentList(List<DocumentInfo> docList) {
        docList.sort(new Comparator<DocumentInfo>() {
            @Override
            public int compare(DocumentInfo o1, DocumentInfo o2) {
                return o2.getCurrentDocument().getLastModified().compareTo(o1.getCurrentDocument().getLastModified());
            }
        });
    }

    public boolean isNewRequest() {
        final FacesContext fc = FacesContext.getCurrentInstance();
        final boolean getMethod = ((HttpServletRequest) fc.getExternalContext().getRequest()).getMethod().equals("GET");
        final boolean ajaxRequest = fc.getPartialViewContext().isAjaxRequest();
        final boolean validationFailed = fc.isValidationFailed();
        return getMethod && !ajaxRequest && !validationFailed;
    }


    public void updateDocumentGroupId(String groupId) {
        this.selectedDocumentGroup = groupId;
    }

    public String goBack() {
        return NAVIGATION_DOCUMENTS;
    }

}
