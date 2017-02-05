package de.hallerweb.enterprise.prioritize.controller.security;

import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import de.hallerweb.enterprise.prioritize.model.Company;
import de.hallerweb.enterprise.prioritize.model.Department;
import de.hallerweb.enterprise.prioritize.model.document.Document;
import de.hallerweb.enterprise.prioritize.model.document.DocumentGroup;
import de.hallerweb.enterprise.prioritize.model.resource.Resource;
import de.hallerweb.enterprise.prioritize.model.resource.ResourceGroup;
import de.hallerweb.enterprise.prioritize.model.security.PAuthorizedObject;
import de.hallerweb.enterprise.prioritize.model.security.PermissionRecord;
import de.hallerweb.enterprise.prioritize.model.security.Role;
import de.hallerweb.enterprise.prioritize.model.security.User;
import de.hallerweb.enterprise.prioritize.model.skill.Skill;
import de.hallerweb.enterprise.prioritize.model.skill.SkillCategory;

/**
 * AuthorizationController.java - Retrieves information of the permissions a user has for the creation, modification and deletion of
 * objects. Objects which inherit from PAuthorizedObject are considered protected resources which are also checked here.
 * 
 * */
@Stateless
public class AuthorizationController {

	@PersistenceContext(unitName = "MySqlDS")
	EntityManager em;

	static User systemUser;
	static final String SYSTEM_USER_API_KEY = "e685567d-38d3-49be-8ab9-2adf80eef508";
	
	// static Proxy instances for use during permission checks. Just used to get Cannonical class name.
	// DO NOT CHANGE THEESE INSTANCES OR USE AS REAL WORLD OBJECTS!
	public static Company COMPANY_TYPE = new Company();
	public static Department DEPARTMENT_TYPE = new Department();
	public static Role ROLE_TYPE = new Role();
	public static User USER_TYPE = new User();
	public static PermissionRecord PERMISSION_RECORD_TYPE = new PermissionRecord();
	public static DocumentGroup DOCUMENT_GROUP_TYPE = new DocumentGroup();
	public static Document DOCUMENT_TYPE = new Document();
	public static ResourceGroup RESOURCE_GROUP_TYPE = new ResourceGroup();
	public static Resource RESOURCE_TYPE = new Resource();
	public static SkillCategory SKILL_CATEGORY = new SkillCategory();
	public static Skill SKILL_TYPE = new Skill();
	

	public static User getSystemUser() {
		if (systemUser == null) {
			systemUser = new User();
			systemUser.setUsername("system");
			systemUser.setApiKey(SYSTEM_USER_API_KEY);
		}
		return systemUser;
	}

	/**
	 * Checks if a given {@link User} can create objects of the target type. Target type must implement {@link PAuthorizedObject}.
	 * 
	 * @param targetObject
	 * @param user
	 * @return
	 */
	public boolean canCreate(PAuthorizedObject targetObject, User user) {
		// if no user provided, always deny permissions!
		if (user == null) {
			return false;
		}
		if (user.equals(systemUser)) {
			return true;
		}
		String absoluteObjectType = targetObject.getClass().getCanonicalName();
		for (Role role : user.getRoles()) {
			for (PermissionRecord perm : role.getPermissions()) {
				if (perm.isCreatePermission()
						&& (perm.getAbsoluteObjectType() == null || perm.getAbsoluteObjectType().equals(absoluteObjectType))) {
					boolean canCreate = perm.getDepartment() == null || user.getDepartment() == null
							|| (perm.getDepartment().getId() == user.getDepartment().getId());
					if (canCreate) {
						return true;
					}
				}
			}
		}
		return false;
	}

	/**
	 * Generally check create permission of {@link User} for a given {@link Department}
	 * 
	 * @param departmentId
	 * @param user
	 * @return
	 */
	public boolean canCreate(int departmentId, PAuthorizedObject targetObject, User user) {
		// if no user provided, always deny permissions!
		if (user == null) {
			return false;
		}
		if (user.equals(systemUser)) {
			return true;
		}
		String absoluteObjectType = targetObject.getClass().getCanonicalName();
		for (Role role : user.getRoles()) {
			for (PermissionRecord perm : role.getPermissions()) {
				if (perm.isCreatePermission()
						&& (perm.getAbsoluteObjectType() == null || perm.getAbsoluteObjectType().equals(absoluteObjectType))) {
					boolean canCreate = perm.getDepartment() == null || perm.getDepartment().getId() == departmentId;
					if (canCreate) {
						return true;
					}
				}
			}
		}
		return false;
	}

	/**
	 * Checks if a given {@link User} can read the given {@link PAuthorizedObject}
	 * 
	 * @param targetObject
	 * @param user
	 * @return
	 */
	public boolean canRead(PAuthorizedObject targetObject, User user) {
		// if no user provided, always deny permissions!
		if (user == null) {
			return false;
		}
		if (user.equals(systemUser)) {
			return true;
		}
		String absoluteObjectType = targetObject.getClass().getCanonicalName();
		for (Role role : user.getRoles()) {
			for (PermissionRecord perm : role.getPermissions()) {
				if (perm.isReadPermission()
						&& (perm.getAbsoluteObjectType() == null || perm.getAbsoluteObjectType().equals(absoluteObjectType))) {
					boolean canRead = perm.getDepartment() == null
							|| (perm.getDepartment().getId() == targetObject.getDepartment().getId());
					if (canRead) {
						return true;
					}
				}
			}
		}
		return false;
	}

	/**
	 * Checks if a given {@link User} can update the given {@link PAuthorizedObject}
	 * 
	 * @param targetObject
	 * @param user
	 * @return
	 */
	public boolean canUpdate(PAuthorizedObject targetObject, User user) {
		// if no user provided, always deny permissions!
		if (user == null) {
			return false;
		}
		if (user.equals(systemUser)) {
			return true;
		}
		String absoluteObjectType = targetObject.getClass().getCanonicalName();
		for (Role role : user.getRoles()) {
			for (PermissionRecord perm : role.getPermissions()) {
				if (perm.isUpdatePermission()
						&& (perm.getAbsoluteObjectType() == null || perm.getAbsoluteObjectType().equals(absoluteObjectType))) {
					boolean canUpdate = perm.getDepartment() == null
							|| (perm.getDepartment().getId() == targetObject.getDepartment().getId());
					if (canUpdate) {
						return true;
					}
				}
			}
		}
		return false;
	}

	/**
	 * Checks if a given {@link User} can delete the given {@link PAuthorizedObject}
	 * 
	 * @param targetObject
	 * @param user
	 * @return
	 */
	public boolean canDelete(PAuthorizedObject targetObject, User user) {
		// if no user provided, always deny permissions!
		if (user == null) {
			return false;
		}
		if (user.equals(systemUser)) {
			return true;
		}
		String absoluteObjectType = targetObject.getClass().getCanonicalName();
		for (Role role : user.getRoles()) {
			for (PermissionRecord perm : role.getPermissions()) {
				if (perm.isDeletePermission()
						&& (perm.getAbsoluteObjectType() == null || perm.getAbsoluteObjectType().equals(absoluteObjectType))) {
					boolean canDelete = perm.getDepartment() == null
							|| (perm.getDepartment().getId() == targetObject.getDepartment().getId());
					if (canDelete) {
						return true;
					}
				}
			}
		}
		return false;
	}
}
