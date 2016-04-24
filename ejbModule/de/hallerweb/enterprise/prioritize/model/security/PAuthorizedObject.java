package de.hallerweb.enterprise.prioritize.model.security;

import de.hallerweb.enterprise.prioritize.model.Department;

/**
 * Interface to indicate that the implementing JPA entity is a protected resource. Access to it can be controlled by assigning a
 * {@link Role} with an adequate {@link PermissionRecord}. All relevant Objects in Prioritize which need to be protected implement this
 * interface.
 * 
 * @author peter
 *
 */
public interface PAuthorizedObject {

	public int getId();

	public Department getDepartment();
}
