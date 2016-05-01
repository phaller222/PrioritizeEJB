package de.hallerweb.enterprise.prioritize.controller.search;

import java.util.ArrayList;
import java.util.List;

import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import de.hallerweb.enterprise.prioritize.controller.CompanyController;
import de.hallerweb.enterprise.prioritize.controller.LoggingController;
import de.hallerweb.enterprise.prioritize.controller.document.DocumentController;
import de.hallerweb.enterprise.prioritize.controller.resource.ResourceController;
import de.hallerweb.enterprise.prioritize.controller.security.AuthorizationController;
import de.hallerweb.enterprise.prioritize.controller.security.SessionController;
import de.hallerweb.enterprise.prioritize.controller.security.UserRoleController;
import de.hallerweb.enterprise.prioritize.controller.skill.SkillController;
import de.hallerweb.enterprise.prioritize.model.Department;
import de.hallerweb.enterprise.prioritize.model.document.DocumentInfo;
import de.hallerweb.enterprise.prioritize.model.resource.Resource;
import de.hallerweb.enterprise.prioritize.model.search.PSearchable;
import de.hallerweb.enterprise.prioritize.model.search.SearchProperty;
import de.hallerweb.enterprise.prioritize.model.search.SearchResult;
import de.hallerweb.enterprise.prioritize.model.security.Role;
import de.hallerweb.enterprise.prioritize.model.security.User;
import de.hallerweb.enterprise.prioritize.model.skill.Skill;

/**
 * SerchController.java - Performs differnt kinds of searches on {@link PSearchable} objects.
 */
@Stateless
public class SearchController {

	@PersistenceContext(unitName = "MySqlDS")
	EntityManager em;

	@EJB
	UserRoleController userRoleController;

	@EJB
	DocumentController documentController;

	@EJB
	ResourceController resourceController;

	@EJB
	CompanyController companyController;

	@EJB
	SkillController skillController;

	@EJB
	LoggingController logger;

	@EJB
	AuthorizationController authController;

	@Inject
	SessionController sessionController;

	public List<SearchResult> searchUsers(String phrase, User sessionUser) {
		List<SearchResult> result = new ArrayList<SearchResult>();
		List<User> users = userRoleController.getAllUsers(sessionUser);
		for (User user : users) {
			result.addAll(user.find(phrase));
		}
		return result;

	}

	public List<SearchResult> searchDocuments(String phrase, User user) {
		List<SearchResult> result = new ArrayList<SearchResult>();
		List<DocumentInfo> documentInfos = documentController.getAllDocumentInfos(sessionController.getUser());
		for (DocumentInfo docInfo : documentInfos) {
			if (authController.canRead(docInfo, user)) {
				result.addAll(docInfo.find(phrase));
			}
		}
		return result;
	}

	public List<SearchResult> searchResources(String phrase, User user) {
		List<SearchResult> result = new ArrayList<SearchResult>();
		List<Resource> resources = resourceController.getAllResources(sessionController.getUser());
		for (Resource res : resources) {
			if (authController.canRead(res, user)) {
				result.addAll(res.find(phrase));
			}
		}
		return result;
	}

	public List<SearchResult> searchSkills(String phrase, User user) {
		List<SearchResult> result = new ArrayList<SearchResult>();
		List<Skill> skills = skillController.getAllSkills(user);
		if (skills != null && !skills.isEmpty()) {
			for (Skill skill : skills) {
				if (authController.canRead(skill, user)) {
					result.addAll(skill.find(phrase));
				}
			}
		}
		return result;
	}

	public List<SearchResult> searchRoles(String phrase, User sessionUser) {
		List<SearchResult> result = new ArrayList<SearchResult>();
		List<Role> roles = userRoleController.getAllRoles(sessionUser);
		for (Role role : roles) {
			result.addAll(role.find(phrase));
		}
		return result;
	}

	public List<SearchResult> searchDepartments(String phrase, User user) {
		List<SearchResult> result = new ArrayList<SearchResult>();
		List<Department> departments = companyController.getAllDepartments();
		for (Department dept : departments) {
			if (authController.canRead(dept, user)) {
				result.addAll(dept.find(phrase));
			}
		}
		return result;
	}

	// TODO: Skills durchsuchbar machen!
	public List<SearchResult> search(String phrase, User user) {
		List<SearchResult> result = new ArrayList<SearchResult>();
		result.addAll(searchUsers(phrase, user));
		result.addAll(searchDocuments(phrase, user));
		result.addAll(searchResources(phrase, user));
		result.addAll(searchRoles(phrase, user));
		result.addAll(searchDepartments(phrase, user));
		result.addAll(searchSkills(phrase, user));
		return result;
	}

	/**
	 * Search all Users on the systems
	 * 
	 * @param phrase String - the phrase to search for
	 * @return List<SearchResult> searchresult
	 */
	public List<SearchResult> searchUser(String phrase, SearchProperty property, User sessionUser) {
		List<SearchResult> result = new ArrayList<SearchResult>();
		List<User> users = userRoleController.getAllUsers(sessionUser);
		for (User user : users) {
			if (authController.canRead(user, sessionUser)) {
				List<SearchResult> res = user.find(phrase, property);
				result.addAll(user.find(phrase, property));
			}
		}
		return result;
	}
}
