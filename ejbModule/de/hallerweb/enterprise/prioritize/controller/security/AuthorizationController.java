package de.hallerweb.enterprise.prioritize.controller.security;

import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import de.hallerweb.enterprise.prioritize.model.Department;
import de.hallerweb.enterprise.prioritize.model.security.PAuthorizedObject;
import de.hallerweb.enterprise.prioritize.model.security.PermissionRecord;
import de.hallerweb.enterprise.prioritize.model.security.Role;
import de.hallerweb.enterprise.prioritize.model.security.User;

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
		if (user.equals(systemUser)) {
			return true;
		}
		String absoluteObjectType = targetObject.getClass().getCanonicalName();
		for (Role role : user.getRoles()) {
			for (PermissionRecord perm : role.getPermissions()) {
				if (perm.isCreatePermission()
						&& (perm.getAbsoluteObjectType() == null || perm.getAbsoluteObjectType().equals(absoluteObjectType))) {
					boolean canCreate = perm.getDepartment() == null || perm.getDepartment().equals(targetObject.getDepartment());
					if (canCreate) {
						return true;
					}
				}
			}
		}
		return false;
	}
	// public boolean canCreate(PAuthorizedObject targetObject, User user) {
	// // if no user provided, always deny permissions!
	// if (user == null)
	// return false;
	//
	// // System user overrides everything
	// String apiKey = user.getApiKey();
	// if (apiKey != null) {
	// if (apiKey.equalsIgnoreCase(SYSTEM_USER_API_KEY)) {
	// return true;
	// }
	// }
	// // Find target department
	// Department targetDepartment = targetObject.getDepartment();
	// // Find Users create permission records for that department, return false if no create permission.
	// Set<Role> roles = user.getRoles();
	// for (Role r : roles) {
	// Set<PermissionRecord> records = r.getPermissions();
	// for (PermissionRecord rec : records) {
	// if ((targetDepartment == null) || (rec.getDepartment() == null)
	// || (rec.getDepartment().getId() == targetDepartment.getId())) {
	// // If there is a PermissionRecord without a target resource type (User,Document...)
	// // the permissions count for all resource types (provided createPermission = true)
	// if ((rec.getTargetResourceType() == null) && (rec.isCreatePermission())) {
	// return true;
	// } else {
	// if (rec.isCreatePermission() && targetObject.getClass().equals(rec.getTargetResourceType())) {
	// return true;
	// }
	// }
	// }
	//
	// }
	// }
	// return false;
	// }

	/**
	 * Generally check create permission of {@link User} for a given {@link Department}
	 * 
	 * @param departmentId
	 * @param user
	 * @return
	 */
	public boolean canCreate(int departmentId, PAuthorizedObject targetObject, User user) {
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

	// // if no user provided, always deny permissions!
	// if (user == null)
	// return false;
	//
	// // System user overrides everything
	// String apiKey = user.getApiKey();
	// if (apiKey != null) {
	// if (apiKey.equalsIgnoreCase(SYSTEM_USER_API_KEY)) {
	// return true;
	// }
	// }
	//
	// // Find target department
	// Department targetDepartment = em.find(Department.class, departmentId);
	// if (targetDepartment == null) {
	// // Find Users create permission records for that department, return false if no create permission.
	// Set<Role> roles = user.getRoles();
	// if (roles == null) {
	// return false;
	// }
	//
	// for (Role r : roles) {
	// Set<PermissionRecord> records = r.getPermissions();
	// for (PermissionRecord rec : records) {
	// if ((rec.getDepartment() == null) || (targetClass.equals(Company.class))) {
	// if (rec.isCreatePermission()) {
	// if ((rec.getTargetResourceType() == null) || (targetClass.equals(rec.getTargetResourceType())))
	// return true;
	// }
	// }
	// }
	// }
	// return false;
	// } else {
	//
	// // Find Users create permission records for that department, return false if no create permission.
	// Set<Role> roles = user.getRoles();
	// for (Role r : roles) {
	// Set<PermissionRecord> records = r.getPermissions();
	// for (PermissionRecord rec : records) {
	// if ((rec.getDepartment() == null) || (rec.getDepartment().getId() == targetDepartment.getId())) {
	// if (rec.isCreatePermission()) {
	// if ((rec.getTargetResourceType() == null) || (targetClass.equals(rec.getTargetResourceType())))
	// return true;
	// }
	// }
	// }
	// }
	// return false;
	// }
	// }

	/**
	 * Checks if a given {@link User} can read the given {@link PAuthorizedObject}
	 * 
	 * @param targetObject
	 * @param user
	 * @return
	 */
	public boolean canRead(PAuthorizedObject targetObject, User user) {
		if (user.equals(systemUser)) {
			return true;
		}
		String absoluteObjectType = targetObject.getClass().getCanonicalName();
		for (Role role : user.getRoles()) {
			for (PermissionRecord perm : role.getPermissions()) {
				if (perm.isReadPermission()
						&& (perm.getAbsoluteObjectType() == null || perm.getAbsoluteObjectType().equals(absoluteObjectType))) {
					boolean canRead = perm.getDepartment() == null || perm.getDepartment().equals(targetObject.getDepartment());
					if (canRead) {
						return true;
					}
				}
			}
		}
		return false;
	}

	// // if no user provided, always deny permissions!
	// if (user == null)
	// return false;
	//
	// // System user overrides everything
	// String apiKey = user.getApiKey();
	// if (apiKey != null) {
	// if (apiKey.equalsIgnoreCase(SYSTEM_USER_API_KEY)) {
	// return true;
	// }
	// }
	// // Find target department
	// Department targetDepartment = targetObject.getDepartment();
	//
	// // Find Users read permission records for that department, return false if no read permission.
	// Set<Role> roles = user.getRoles();
	// for (Role r : roles) {
	// Set<PermissionRecord> records = r.getPermissions();
	// for (PermissionRecord rec : records) {
	// if ((rec.getDepartment() == null) || (targetDepartment == null)
	// || (rec.getDepartment().getId() == targetDepartment.getId())) {
	// if (rec.isReadPermission()) {
	// if (rec.getTargetResourceType() == null) {
	// return true;
	// } else {
	// if (targetObject.getClass().equals(rec.getTargetResourceType())) {
	// return true;
	// }
	// }
	// }
	// }
	// }
	// }
	// return false;
	// }

	/**
	 * Checks if a given {@link User} can update the given {@link PAuthorizedObject}
	 * 
	 * @param targetObject
	 * @param user
	 * @return
	 */
	public boolean canUpdate(PAuthorizedObject targetObject, User user) {
		if (user.equals(systemUser)) {
			return true;
		}
		String absoluteObjectType = targetObject.getClass().getCanonicalName();
		for (Role role : user.getRoles()) {
			for (PermissionRecord perm : role.getPermissions()) {
				if (perm.isUpdatePermission()
						&& (perm.getAbsoluteObjectType() == null || perm.getAbsoluteObjectType().equals(absoluteObjectType))) {
					boolean canUpdate = perm.getDepartment() == null || perm.getDepartment().equals(targetObject.getDepartment());
					if (canUpdate) {
						return true;
					}
				}
			}
		}
		return false;
	}

	// // if no user provided, always deny permissions!
	// if (user == null)
	// return false;
	//
	// // System user overrides everything
	// String apiKey = user.getApiKey();
	// if (apiKey != null) {
	// if (apiKey.equalsIgnoreCase(SYSTEM_USER_API_KEY)) {
	// return true;
	// }
	// }
	// // Find target department
	// Department targetDepartment = targetObject.getDepartment();
	//
	// // Find Users update permission records for that department, return false if no update permission.
	// Set<Role> roles = user.getRoles();
	// for (Role r : roles) {
	// Set<PermissionRecord> records = r.getPermissions();
	// for (PermissionRecord rec : records) {
	// if ((rec.getDepartment() == null) || (targetDepartment == null)
	// || (rec.getDepartment().getId() == targetDepartment.getId())) {
	// if (rec.isUpdatePermission()) {
	// if (rec.getTargetResourceType() == null) {
	// return true;
	// } else {
	// if (targetObject.getClass().equals(rec.getTargetResourceType())) {
	// return true;
	// }
	// }
	// }
	// }
	// }
	// }
	// return false;
	// }

	/**
	 * Checks if a given {@link User} can delete the given {@link PAuthorizedObject}
	 * 
	 * @param targetObject
	 * @param user
	 * @return
	 */
	public boolean canDelete(PAuthorizedObject targetObject, User user) {
		if (user.equals(systemUser)) {
			return true;
		}
		String absoluteObjectType = targetObject.getClass().getCanonicalName();
		for (Role role : user.getRoles()) {
			for (PermissionRecord perm : role.getPermissions()) {
				if (perm.isDeletePermission()
						&& (perm.getAbsoluteObjectType() == null || perm.getAbsoluteObjectType().equals(absoluteObjectType))) {
					boolean canDelete = perm.getDepartment() == null || perm.getDepartment().equals(targetObject.getDepartment());
					if (canDelete) {
						return true;
					}
				}
			}
		}
		return false;
	}

	// // if no user provided, always deny permissions!
	// if (user == null)
	// return false;
	//
	// // System user overrides everything
	// String apiKey = user.getApiKey();
	// if (apiKey != null) {
	// if (apiKey.equalsIgnoreCase(SYSTEM_USER_API_KEY)) {
	// return true;
	// }
	// }
	// // Find target department
	// Department targetDepartment = targetObject.getDepartment();
	//
	// // Find Users delete permission records for that department, return false if no delete permission.
	// Set<Role> roles = user.getRoles();
	// for (Role r : roles) {
	// Set<PermissionRecord> records = r.getPermissions();
	// for (PermissionRecord rec : records) {
	// if ((rec.getDepartment() == null) || (targetDepartment == null)
	// || (rec.getDepartment().getId() == targetDepartment.getId())) {
	// if (rec.isDeletePermission()) {
	// if (rec.getTargetResourceType() == null) {
	// return true;
	// } else {
	// if (targetObject.getClass().equals(rec.getTargetResourceType())) {
	// return true;
	// }
	// }
	// }
	// }
	// }
	// }
	// return false;
	// }

}
