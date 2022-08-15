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
package de.hallerweb.enterprise.prioritize.controller.security;

import de.hallerweb.enterprise.prioritize.model.Company;
import de.hallerweb.enterprise.prioritize.model.Department;
import de.hallerweb.enterprise.prioritize.model.document.Document;
import de.hallerweb.enterprise.prioritize.model.document.DocumentGroup;
import de.hallerweb.enterprise.prioritize.model.resource.Resource;
import de.hallerweb.enterprise.prioritize.model.resource.ResourceGroup;
import de.hallerweb.enterprise.prioritize.model.security.*;
import de.hallerweb.enterprise.prioritize.model.skill.Skill;
import de.hallerweb.enterprise.prioritize.model.skill.SkillCategory;

import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import java.util.ArrayList;
import java.util.List;

/**
 * AuthorizationController.java - Retrieves information of the permissions a user has for the creation, modification and deletion of
 * objects. Objects which inherit from PAuthorizedObject are considered protected resources which are also checked here.
 * 
 * */
@Stateless
public class AuthorizationController {

	@PersistenceContext
	EntityManager em;

	@EJB
	UserRoleController userRoleController;

	static User systemUser;
	static final String SYSTEM_USER_API_KEY = "e685567d-38d3-49be-8ab9-2adf80eef508";

	// static Proxy instances for use during permission checks. Just used to get Cannonical class name.
	// DO NOT CHANGE THEESE INSTANCES OR USE AS REAL WORLD OBJECTS!
	public static final Company COMPANY_TYPE = new Company();
	public static final Department DEPARTMENT_TYPE = new Department();
	public static final Role ROLE_TYPE = new Role();
	public static final User USER_TYPE = new User();
	public static final PermissionRecord PERMISSION_RECORD_TYPE = new PermissionRecord();
	public static final DocumentGroup DOCUMENT_GROUP_TYPE = new DocumentGroup();
	public static final Document DOCUMENT_TYPE = new Document();
	public static final ResourceGroup RESOURCE_GROUP_TYPE = new ResourceGroup();
	public static final Resource RESOURCE_TYPE = new Resource();
	public static final SkillCategory SKILL_CATEGORY = new SkillCategory();
	public static final Skill SKILL_TYPE = new Skill();

	public User getSystemUser() {
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
		Boolean x = canCreatePreCheck(targetObject, user);
		if (x != null) return x;

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

	private Boolean canCreatePreCheck(PAuthorizedObject targetObject, User user) {
		// if no user provided, always deny permissions!
		if (user == null) {
			return false;
		}
		if (user.equals(systemUser)) {
			return true;
		}

		if (targetObject instanceof User) {
			User u = (User) targetObject;
			if (u.getUsername() != null && u.getUsername().equals("admin") && !user.getUsername().equalsIgnoreCase("admin")) {
				return false;
			}
		}

		if (targetObject instanceof Company && !checkCompanyPermission(targetObject, user)) {
			// User must not create foreign companies!
			return false;
		}
		return null;
	}

	/**
	 * Generally check create permission of {@link User} for a given {@link Department}
	 * 
	 * @param departmentId
	 * @param user
	 * @return
	 */
	public boolean canCreate(int departmentId, PAuthorizedObject targetObject, User user) {
		Boolean x = canCreatePreCheck(targetObject, user);
		if (x != null) return x;

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
		
		if (targetObject instanceof User) {
			User u = (User) targetObject;
			if (u.getUsername() != null && u.getUsername().equals("admin") && !user.getUsername().equalsIgnoreCase("admin")) {
				return false;
			}
		}

		if ((targetObject instanceof Company) && (!checkCompanyPermission(targetObject, user))) {
			// User must not read foreign companies!
			return false;
		}

		String absoluteObjectType = targetObject.getClass().getCanonicalName();
		user = userRoleController.findUserByUsername(user.getUsername(),getSystemUser());
		for (Role role : user.getRoles()) {
			for (PermissionRecord perm : role.getPermissions()) {
				if (perm.isReadPermission()
						&& (perm.getAbsoluteObjectType() == null || perm.getAbsoluteObjectType().equals(absoluteObjectType))) {
					// Object with this ID is explicitly readable by this role.
					if (perm.getObjectId() == targetObject.getId()) {
						return true;
					}
					boolean canRead;
					if (targetObject.getDepartment() != null) {
						canRead = perm.getDepartment() == null || (perm.getDepartment().getId() == targetObject.getDepartment().getId());
					} else {
						return true;
					}
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
		
		if (targetObject instanceof User) {
			User u = (User) targetObject;
			if (u.getUsername() != null && u.getUsername().equals("admin") && !user.getUsername().equalsIgnoreCase("admin")) {
				return false;
			}
		}

		if (targetObject instanceof Company && !checkCompanyPermission(targetObject, user)) {
			// User must not update foreign companies!
			return false;
		}

		String absoluteObjectType = targetObject.getClass().getCanonicalName();
		user = userRoleController.findUserByUsername(user.getUsername(),getSystemUser());
		for (Role role : user.getRoles()) {
			for (PermissionRecord perm : role.getPermissions()) {
				if (perm.isUpdatePermission()
						&& (perm.getAbsoluteObjectType() == null || perm.getAbsoluteObjectType().equals(absoluteObjectType))) {
					// Object with this ID is explicitly updatable by this role.
					if (perm.getObjectId() == targetObject.getId()) {
						return true;
					}
					boolean canUpdate = perm.getDepartment() == null || targetObject.getDepartment() == null
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
		
		if (targetObject instanceof User) {
			User u = (User) targetObject;
			if (u.getUsername() != null && u.getUsername().equals("admin") && !user.getUsername().equalsIgnoreCase("admin")) {
				return false;
			}
		}

		if (targetObject instanceof Company && !checkCompanyPermission(targetObject, user)) {
			// User must not delete foreign companies!
			return false;
		}

		String absoluteObjectType = targetObject.getClass().getCanonicalName();
		user = userRoleController.findUserByUsername(user.getUsername(),getSystemUser());
		for (Role role : user.getRoles()) {
			for (PermissionRecord perm : role.getPermissions()) {
				if (perm.isDeletePermission()
						&& (perm.getAbsoluteObjectType() == null || perm.getAbsoluteObjectType().equals(absoluteObjectType))) {
					// Object with this ID is explicitly deletable by this role.
					if (perm.getObjectId() == targetObject.getId()) {
						return true;
					}
					boolean canDelete = perm.getDepartment() == null || targetObject.getDepartment() == null ||
							 (perm.getDepartment().getId() == targetObject.getDepartment().getId());
					if (canDelete) {
						return true;
					}
				}
			}
		}
		return false;
	}

	public void addObservedObjectType(String absoluteClassName) {
		Query query = em.createNamedQuery("findAllObjectTypes");
		List<ObservedObjectType> types = query.getResultList();
		for (ObservedObjectType type : types) {
			if (type.getObjectType().equalsIgnoreCase(absoluteClassName)) {
				return;
			}
		}
		ObservedObjectType newType = new ObservedObjectType();
		newType.setObjectType(absoluteClassName);
		em.persist(newType);
	}

	public List<ObservedObjectType> getObservableObjectTypes() {
		Query query = em.createNamedQuery("findAllObjectTypes");
		List<ObservedObjectType> types = query.getResultList();
		if (types != null && !types.isEmpty()) {
			return types;
		} else {
			return new ArrayList<>();
		}
	}

	private boolean checkCompanyPermission(PAuthorizedObject targetObject, User user) {
		Company comp = (Company) targetObject;
		Department dept = user.getDepartment();
		// User must not read foreign companies!
		if (dept != null) {
			return dept.getCompany().getId() == comp.getId();
		} else {
			return true;
		}
	}
}