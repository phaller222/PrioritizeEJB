package de.hallerweb.enterprise.prioritize.view.search;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;

import javax.ejb.EJB;
import javax.enterprise.context.SessionScoped;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
//import javax.faces.context.ExternalContext;
//import javax.faces.context.FacesContext;
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

	public List<SearchResult> getSearchResults() {
		return searchResults;
	}

	public void setSearchResults(List<SearchResult> searchResults) {
		this.searchResults = searchResults;
	}

	private String searchPhrase;

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

	@EJB
	CompanyController companyController;

	List<SearchResult> searchResults; // Stores the SearchResult

	@Named
	public List<SearchResult> search() {
		this.searchResults = searchController.search(this.searchPhrase, sessionController.getUser());
		return this.searchResults;
	}

	@Named
	public List<SearchResult> search(String property) {
		this.searchResults = searchController.searchUser(this.searchPhrase, new SearchProperty(property), sessionController.getUser());
		Collections.sort(this.searchResults);
		for (SearchResult res : this.searchResults) {
			System.out.println(res.getExcerpt());
		}
		System.out.println("Found :" + searchResults.size());
		return this.searchResults;
	}

	@Named
	public String displayResult(SearchResult result) {
		ExternalContext context = FacesContext.getCurrentInstance().getExternalContext();

		if (result.getResultType().equalsIgnoreCase("User")) {
			try {
				User user = (User) result.getResult();
				userBean.setUser(user);
				context.redirect(context.getApplicationContextPath() + "/admin/users/edituser.xhtml");
			} catch (Exception e) {
				e.printStackTrace();
			}
			return "edituser?faces-redirect=true";
		}
		if (result.getResultType().equalsIgnoreCase("Role")) {
			try {
				Role role = (Role) result.getResult();
				roleBean.setRole(role);
				context.redirect(context.getApplicationContextPath() + "/admin/roles/editrole.xhtml");
			} catch (Exception e) {
				e.printStackTrace();
			}
			return "editrole";
		} else if (result.getResultType().equalsIgnoreCase("Document")) {
			try {
				DocumentInfo info = (DocumentInfo) result.getResult();
				documentBean.setDocumentInfo(info);
				documentBean.setDocument(info.getCurrentDocument());
				context.redirect(context.getApplicationContextPath() + "/admin/documents/editdocument.xhtml");
			} catch (Exception e) {
				e.printStackTrace();
			}
			return "documents";
		} else if (result.getResultType().equalsIgnoreCase("Resource")) {
			try {
				Resource res = (Resource) result.getResult();
				resourceBean.setResource(res);
				context.redirect(context.getApplicationContextPath() + "/admin/resources/editresource.xhtml");
			} catch (Exception e) {
				e.printStackTrace();
			}
			return "documents";
		} else if (result.getResultType().equalsIgnoreCase("Department")) {
			try {
				Department dept = (Department) result.getResult();
				companyBean.setDepartment(dept);
				context.redirect(context.getApplicationContextPath() + "/admin/companies/editdepartment.xhtml");
			} catch (Exception e) {
				e.printStackTrace();
			}
			return "companies";
		} else
			return "";
	}
}
