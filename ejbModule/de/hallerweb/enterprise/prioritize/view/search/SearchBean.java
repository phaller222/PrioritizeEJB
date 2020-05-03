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
package de.hallerweb.enterprise.prioritize.view.search;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.ejb.EJB;
import javax.enterprise.context.SessionScoped;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.inject.Named;

import de.hallerweb.enterprise.prioritize.controller.CompanyController;
import de.hallerweb.enterprise.prioritize.controller.search.SearchController;
import de.hallerweb.enterprise.prioritize.controller.security.AuthorizationController;
import de.hallerweb.enterprise.prioritize.controller.security.SessionController;
import de.hallerweb.enterprise.prioritize.model.Department;
import de.hallerweb.enterprise.prioritize.model.document.DocumentInfo;
import de.hallerweb.enterprise.prioritize.model.resource.Resource;
import de.hallerweb.enterprise.prioritize.model.search.SearchProperty;
import de.hallerweb.enterprise.prioritize.model.search.SearchResult;
import de.hallerweb.enterprise.prioritize.model.security.Role;
import de.hallerweb.enterprise.prioritize.model.security.User;
import de.hallerweb.enterprise.prioritize.view.CompanyBean;
import de.hallerweb.enterprise.prioritize.view.document.DocumentBean;
import de.hallerweb.enterprise.prioritize.view.resource.ResourceBean;
import de.hallerweb.enterprise.prioritize.view.security.RoleBean;
import de.hallerweb.enterprise.prioritize.view.security.UserBean;

/**
 * SearchBean - JSF Backing-Bean to handle searches.
 * <p>
 * Copyright: (c) 2015
 * </p>
 * <p>
 * Peter Haller
 * </p>
 * 
 * @author peter
 */
@Named
@SessionScoped
public class SearchBean implements Serializable {

	@Inject
	UserBean userBean;
	@Inject
	RoleBean roleBean;
	@Inject
	DocumentBean documentBean;
	@Inject
	ResourceBean resourceBean;
	@Inject
	CompanyBean companyBean;
	@Inject
	SessionController sessionController;
	@EJB
	AuthorizationController authController;
	@EJB
	SearchController searchController;
	@EJB
	CompanyController companyController;

	private String searchPhrase;

	transient List<SearchResult> searchResults; // Stores the SearchResult

	public List<SearchResult> getSearchResults() {
		return searchResults;
	}

	public void setSearchResults(List<SearchResult> searchResults) {
		this.searchResults = searchResults;
	}

	public String getSearchPhrase() {
		return searchPhrase;
	}

	public void setSearchPhrase(String searchPhrase) {
		this.searchPhrase = searchPhrase;
	}

	public List<SearchResult> getSearchResult() {
		return searchResults;
	}

	public void setSearchResult(List<SearchResult> searchResult) {
		this.searchResults = searchResult;
	}

	@Named
	public List<SearchResult> search() {
		this.searchResults = searchController.search(this.searchPhrase, sessionController.getUser());
		return this.searchResults;
	}

	@Named
	public List<SearchResult> search(String property) {
		this.searchResults = searchController.searchUser(this.searchPhrase, new SearchProperty(property), sessionController.getUser());
		Collections.sort(this.searchResults);
		return this.searchResults;
	}

	@Named
	public String displayResult(SearchResult result) {
		if (result.getResultType().equalsIgnoreCase("User")) {
			displayUserResult(result);
			return "edituser?faces-redirect=true";
		} else if (result.getResultType().equalsIgnoreCase("Role")) {
			displayRoleResult(result);
			return "editrole";
		} else if (result.getResultType().equalsIgnoreCase("Document")) {
			displayDocumentResult(result);
			return "documents";
		} else if (result.getResultType().equalsIgnoreCase("Resource")) {
			displayResourceResult(result);
			return "documents";
		} else if (result.getResultType().equalsIgnoreCase("Department")) {
			displayDepartmentResult(result);
			return "companies";
		} else {
			return "";
		}
	}

	private void displayDepartmentResult(SearchResult result) {
		ExternalContext context = FacesContext.getCurrentInstance().getExternalContext();
		try {
			Department dept = (Department) result.getResult();
			companyBean.setDepartment(dept);
			context.redirect(context.getApplicationContextPath() + "/admin/companies/editdepartment.xhtml");
		} catch (Exception e) {
			Logger.getLogger(getClass().getName()).log(Level.SEVERE, e.getMessage(), e);
		}
	}

	private void displayResourceResult(SearchResult result) {
		ExternalContext context = FacesContext.getCurrentInstance().getExternalContext();
		try {
			Resource res = (Resource) result.getResult();
			resourceBean.setResource(res);
			context.redirect(context.getApplicationContextPath() + "/admin/resources/editresource.xhtml");
		} catch (Exception e) {
			Logger.getLogger(getClass().getName()).log(Level.SEVERE, e.getMessage(), e);
		}
	}

	private void displayDocumentResult(SearchResult result) {
		ExternalContext context = FacesContext.getCurrentInstance().getExternalContext();
		try {
			DocumentInfo info = (DocumentInfo) result.getResult();
			documentBean.setDocumentInfo(info);
			documentBean.setDocument(info.getCurrentDocument());
			context.redirect(context.getApplicationContextPath() + "/admin/documents/editdocument.xhtml");
		} catch (Exception e) {
			Logger.getLogger(getClass().getName()).log(Level.SEVERE, e.getMessage(), e);
		}
	}

	private void displayRoleResult(SearchResult result) {
		ExternalContext context = FacesContext.getCurrentInstance().getExternalContext();
		try {
			Role role = (Role) result.getResult();
			roleBean.setRole(role);
			context.redirect(context.getApplicationContextPath() + "/admin/roles/editrole.xhtml");
		} catch (Exception e) {
			Logger.getLogger(getClass().getName()).log(Level.SEVERE, e.getMessage(), e);
		}
	}

	private void displayUserResult(SearchResult result) {
		ExternalContext context = FacesContext.getCurrentInstance().getExternalContext();
		try {
			User user = (User) result.getResult();
			userBean.setUser(user);
			context.redirect(context.getApplicationContextPath() + "/admin/users/edituser.xhtml");
		} catch (Exception e) {
			Logger.getLogger(getClass().getName()).log(Level.SEVERE, e.getMessage(), e);
		}
	}
}
