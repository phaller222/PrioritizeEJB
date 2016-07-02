package de.hallerweb.enterprise.prioritize.controller.security;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import javax.ejb.EJB;
import javax.ejb.EJBException;
import javax.ejb.Stateless;
import javax.enterprise.context.ContextNotActiveException;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

import de.hallerweb.enterprise.prioritize.controller.InitializationController;
import de.hallerweb.enterprise.prioritize.controller.LoggingController;
import de.hallerweb.enterprise.prioritize.controller.LoggingController.Action;
import de.hallerweb.enterprise.prioritize.controller.event.EventRegistry;
import de.hallerweb.enterprise.prioritize.model.Company;
import de.hallerweb.enterprise.prioritize.model.Department;
import de.hallerweb.enterprise.prioritize.model.PObject;
import de.hallerweb.enterprise.prioritize.model.calendar.TimeSpan;
import de.hallerweb.enterprise.prioritize.model.event.Event;
import de.hallerweb.enterprise.prioritize.model.event.PEventConsumerProducer;
import de.hallerweb.enterprise.prioritize.model.event.PObjectType;
import de.hallerweb.enterprise.prioritize.model.security.PermissionRecord;
import de.hallerweb.enterprise.prioritize.model.security.Role;
import de.hallerweb.enterprise.prioritize.model.security.User;
import de.hallerweb.enterprise.prioritize.model.skill.Skill;
import de.hallerweb.enterprise.prioritize.model.skill.SkillRecord;
import de.hallerweb.enterprise.prioritize.model.usersetting.UserPreference;

/**
 * UserRoleController.java - Controls the creation, modification and deletion of
 * {@link Role} and {@link User} objects.
 */
@Stateless
public class UserRoleController extends PEventConsumerProducer {

	@PersistenceContext(unitName = "MySqlDS")
	EntityManager em;

	@EJB
	LoggingController logger;

	@EJB
	AuthorizationController authController;

	@Inject
	SessionController sessionController;

	@Inject
	EventRegistry eventRegistry;

	/**
	 * Default constructor.
	 */
	public UserRoleController() {
		// TODO Auto-generated constructor stub
	}

	public Role findRoleByRolename(String roleName, User user) {
		Query query = em.createNamedQuery("findRoleByRolename");
		query.setParameter(1, roleName);
		try {
			Role r = (Role) query.getSingleResult();
			if (authController.canRead(r, user)) {
				return r;
			} else {
				return null;
			}
		} catch (Exception ex) {
			return null;
		}
	}

	/**
	 * Creates a new role with the given name and {@link PermissionRecord}s.
	 * 
	 * @param name - Name of company
	 * @param mainAddress Main address
	 * @param isSubcompany Is this company a subcompany of another?
	 * @return the created {@link Company}
	 */
	public Role createRole(String name, String description, Set<PermissionRecord> permissions, User sessionUser) {
		if (findRoleByRolename(name, sessionUser) == null) {
			Role r = new Role();
			if (authController.canCreate(r, sessionUser)) {
				r.setName(name);
				r.setDescription(description);
				em.persist(r);
				em.flush();
				Department managedDepartment;
				Set<PermissionRecord> managedPermissions = new HashSet<PermissionRecord>();

				for (PermissionRecord rec : permissions) {
					PermissionRecord recNew = new PermissionRecord();
					recNew.setCreatePermission(rec.isCreatePermission());
					recNew.setReadPermission(rec.isReadPermission());
					recNew.setUpdatePermission(rec.isUpdatePermission());
					recNew.setDeletePermission(rec.isDeletePermission());

					// Resource type can be null for ALL resource types!
					if (rec.getTargetResourceType() != null) {
						recNew.setTargetResourceType(rec.getTargetResourceType().getSimpleName());
					}

					// rec.gertDepartment() can be null if permission record set for
					// all departments!
					if (rec.getDepartment() != null) {
						managedDepartment = em.find(Department.class, rec.getDepartment().getId());
						recNew.setDepartment(managedDepartment);
					}

					try {
						em.persist(recNew);
					} catch (Exception ex) {
						em.merge(recNew);
					}
					managedPermissions.add(recNew);
				}
				r.setPermissions(managedPermissions);

				em.flush();
				try {
					logger.log(sessionController.getUser().getUsername(), "Role", Action.CREATE, r.getId(),
							"Role \"" + r.getName() + "\" created.");
				} catch (ContextNotActiveException ex) {
					logger.log("SYSTEM", "Role", Action.CREATE, r.getId(), " Role \"" + r.getName() + "\" created.");
				}
				return r;
			} else {
				return null;
			}
		} else {
			// Rolename already exists, return null.
			return null;
		}
	}

	public void deleteRole(int id, User sessionUser) {
		Role r = findRoleById(id);
		if (authController.canDelete(r, sessionUser)) {
			Set<PermissionRecord> records = r.getPermissions();
			for (PermissionRecord rec : records) {
				PermissionRecord orig = em.find(PermissionRecord.class, rec.getId());
				em.remove(orig);
			}

			// Remove the role from all of it's users.
			Set<User> users = r.getUsers();
			for (User user : users) {
				User managedUser = em.find(User.class, user.getId());
				managedUser.removeRole(r);
			}

			em.remove(r);
			em.flush();

			logger.log(sessionController.getUser().getUsername(), "Role", Action.DELETE, r.getId(),
					"Role \"" + r.getName() + "\" deleted.");
		}
	}

	/**
	 * Removes a permission record from a role.
	 * 
	 * @param roleId
	 *            - id of the {@link Role}
	 * @param permissionId
	 *            - id of {@link PermissionRecord} to be removed.
	 */
	public void deletePermissionRecord(int roleId, int permissionId) {
		Role r = em.find(Role.class, roleId);
		PermissionRecord rec = em.find(PermissionRecord.class, permissionId);

		if (rec.getDepartment() != null) {
			rec.setDepartment(null);
		}

		r.getPermissions().remove(rec);
		em.remove(rec);
		em.flush();

		logger.log(sessionController.getUser().getUsername(), "PermissionRecord", Action.DELETE, rec.getId(),
				"PermissionRecord \"" + rec.getId() + "\" deleted.");
	}

	/**
	 * Adds a {@link PermissionRecord} to the given {@link Role}
	 * 
	 * @param roleId
	 *            - ID of the {@link Role}
	 * @param recNew
	 *            - {@link PermissionRecord} to be added.
	 */
	public void addPermissionRecord(int roleId, PermissionRecord recNew) {
		Role r = em.find(Role.class, roleId);
		try {
			em.persist(recNew);
			em.flush();
		} catch (Exception ex) {
			em.merge(recNew);
		}
		r.addPermission(recNew);
		em.flush();
		logger.log(sessionController.getUser().getUsername(), "PermissionRecord", Action.CREATE, recNew.getId(),
				"PermissionRecord \"" + recNew.getId() + "\" added.");
	}

	public List<Role> getAllRoles(User sessionUser) throws EJBException {
		Query query = em.createQuery("SELECT r FROM Role r ORDER BY r.name");
		List<Role> roles = query.getResultList();
		Role r = roles.get(0);
		if (authController.canRead(r, sessionUser)) {
			return roles;
		} else {
			return new ArrayList<Role>();
		}
	}

	public Role findRoleById(int id) {
		return em.find(Role.class, id);
	}

	// -------------------------- Users ---------------------------------------------------

	public User createUser(String username, String password, String name, String email, Department initialDepartment, String occupation,
			Set<Role> roles, User sessionUser) {
		User user = new User();
		
		if (authController.canCreate(user, sessionUser)) {
			if (findUserByUsername(username, AuthorizationController.getSystemUser()) == null) {
				user.setUsername(username);
				user.setName(name);
				user.setEmail(email);
				user.setOccupation(occupation);
				user.setDepartment(initialDepartment);
				user.setPassword(String.valueOf(password.hashCode()));
				UserPreference preference = new UserPreference(user);
				em.persist(preference);
				user.setPreference(preference);
				em.persist(user);
				

				for (Role role : roles) {
					Role managedRole = em.find(Role.class, role.getId());
					user.addRole(managedRole);
					managedRole.addUser(user);
				}

				em.flush();
				try {
					logger.log(sessionController.getUser().getUsername(), "User", Action.CREATE, user.getId(),
							"User \"" + user.getUsername() + "\" created.");
				} catch (ContextNotActiveException ex) {
					logger.log("SYSTEM", "User", Action.CREATE, user.getId(), " User \"" + user.getUsername() + "\" created.");
				}
				return user;
			} else {
				// Username already exists, return null.
				return null;
			}
		} else {
			return null;
		}
	}

	/**
	 * Edit User information.
	 * 
	 * @param id int - ID of the {@link User} to edit.
	 * @param username String - the new Username
	 * @param name String - the new full name.
	 * @param email String - the new E-Mail Address.
	 * @param initialDepartmentId int - ID of the Department the user belongs to.
	 * @param occupation String - the new Occupation of the user
	 */
	public void editUser(int id, String username, String password, String name, String email, int initialDepartmentId, String occupation,
			User sessionUser) {
		User user = em.find(User.class, id);
		if (authController.canUpdate(user, sessionUser)) {

			// Raise events if configured
			if (InitializationController.getAsBoolean(InitializationController.FIRE_USER_EVENTS)) {
				if (!user.getName().equals(name)) {
					this.raiseEvent(user, User.PROPERTY_NAME, user.getName(), name,
							InitializationController.getAsInt(InitializationController.EVENT_DEFAULT_TIMEOUT));
				}
				if (!user.getUsername().equals(username)) {
					this.raiseEvent(user, User.PROPERTY_USERNAME, user.getUsername(), username,
							InitializationController.getAsInt(InitializationController.EVENT_DEFAULT_TIMEOUT));
				}
				if (!user.getEmail().equals(email)) {
					this.raiseEvent(user, User.PROPERTY_EMAIL, user.getEmail(), email,
							InitializationController.getAsInt(InitializationController.EVENT_DEFAULT_TIMEOUT));
				}
				if (!user.getOccupation().equals(occupation)) {
					this.raiseEvent(user, User.PROPERTY_OCCUPATION, user.getOccupation(), occupation,
							InitializationController.getAsInt(InitializationController.EVENT_DEFAULT_TIMEOUT));
				}
				if (user.getDepartment() != null) {
					if (!(user.getDepartment().getId() == initialDepartmentId)) {
						this.raiseEvent(user, User.PROPERTY_DEPARTMENT,
								String.valueOf(user.getDepartment().getId()), String.valueOf(initialDepartmentId),
								InitializationController.getAsInt(InitializationController.EVENT_DEFAULT_TIMEOUT));
					}
				}
			}

			user.setUsername(username);
			user.setName(name);
			user.setEmail(email);
			if ((password != null) && (password.length() > 0)) {
				user.setPassword(String.valueOf(password.hashCode()));
			}
			user.setOccupation(occupation);

			Department dept = em.find(Department.class, initialDepartmentId);
			user.setDepartment(dept);

			em.flush();
			logger.log(sessionController.getUser().getUsername(), "User", Action.UPDATE, user.getId(),
					"User \"" + user.getUsername() + "\" updated.");
		}
	}

	public List<User> getAllUsers(User sessionUser) throws EJBException {
		Query query = em.createNamedQuery("findAllUsers");
		User dummy = new User();
		List<User> users = query.getResultList();
		if (authController.canRead(dummy, sessionUser)) {
			return users;
		} else {
			return new ArrayList<User>();
		}

	}
	
	public List<String> getAllUserNames(User sessionUser) throws EJBException {
		Query query = em.createNamedQuery("findAllUserNames");
		List<String> users = query.getResultList();
		return users;
	}

	public User getUserById(int id, User sessionUser) {
		User user = em.find(User.class, id);
		if (authController.canRead(user, sessionUser)) {
			return user;
		} else {
			return null;
		}

	}

	public List<User> getUsersForDepartment(Department dept, User sessionUser) {
		Query query = em.createNamedQuery("findUserByDepartment");
		query.setParameter("deptId", dept.getId());
		List<User> deptUsers = query.getResultList();
		User userToCheck = deptUsers.get(0);
		if (authController.canRead(userToCheck, sessionUser)) {
			return deptUsers;
		} else {
			return new ArrayList<User>();
		}
	}

	public User findUserByUsername(String username, User sessionUser) throws EJBException {
		Query query = em.createNamedQuery("findUserByUsername");
		query.setParameter(1, username);
		try {
			User u = (User) query.getSingleResult();
			if (authController.canRead(u, sessionUser)) {
				return u;
			} else {
				return null;
			}
		} catch (NoResultException ex) {
			return null;
		}
	}

	public User findUserByApiKey(String apiKey) throws EJBException {
		Query query = em.createNamedQuery("findUserByApiKey");
		query.setParameter("apiKey", apiKey);
		User user = null;
		try {
			user = (User) query.getSingleResult();
		} catch (NoResultException ex) {
			return null;
		}
		return user;
	}

	public User findUserById(int id) {
		return em.find(User.class, id);
	}

	public void deleteUser(int id, User sessionUser) {
		User u = findUserById(id);
		if (authController.canDelete(u, sessionUser)) {
			int userId = u.getId();
			String userName = u.getUsername();

			Set<Role> roles = u.getRoles();
			for (Role r : roles) {
				r.removeUser(u);
				u.removeRole(r);
			}

			em.flush();

			em.remove(u);
			em.flush();

			logger.log(sessionController.getUser().getUsername(), "User", Action.DELETE, userId, "User \"" + userName + "\" deleted.");
		}
	}

	public void removeRoleFromUser(int userId, int roleId, User sessionUser) {
		// TODO: Sometimes either the role or the user is not beeing updated!
		// ISSUE:
		// http://mantis.hallerweb.de/view.php?id=67
		User u = em.find(User.class, userId);
		if (authController.canUpdate(u, sessionUser)) {
			Role r = em.find(Role.class, roleId);
			r.removeUser(u);
			u.removeRole(r);
			em.flush();

			// TODO: Workaround: just check and try it again if deletion did not
			// succeed.everytime.
			if (r.getUsers().contains(u)) {
				r.removeUser(u);
			}
			if (u.getRoles().contains(r)) {
				u.removeRole(r);
			}
		}

	}

	public void removeSkillFromUser(SkillRecord record, User user, User sessionUser) {
		User u = em.find(User.class, user.getId());
		if (authController.canUpdate(u, sessionUser)) {
			SkillRecord skillRecord = em.find(SkillRecord.class, record.getId());
			skillRecord.setUser(null);
			u.removeSkill(skillRecord);
			em.remove(skillRecord);
			em.flush();
		}
	}

	public void addRoleToUser(int userId, int roleId, User sessionUser) {
		User u = em.find(User.class, userId);
		if (authController.canUpdate(u, sessionUser)) {
			Role r = em.find(Role.class, roleId);

			u.addRole(r);
			r.addUser(u);
		}
	}

	public void addSkillToUser(int userId, int skillRecordId, User sessionUser) {
		boolean alreadyAssigned = false; // is set to true if skill type already assigned.
		User u = em.find(User.class, userId);
		if (authController.canUpdate(u, sessionUser)) {
			SkillRecord rec = em.find(SkillRecord.class, skillRecordId);
			Skill skill = em.find(Skill.class, rec.getSkill().getId());

			for (SkillRecord userSkillRecord : u.getSkills()) {
				if (userSkillRecord.getSkill().getId() == rec.getId()) {
					alreadyAssigned = true;
				}
			}
			// Only assign new skill if user does not already have
			// a skill of that kind assigned.
			if (!alreadyAssigned) {
				u.addSkill(rec);
				rec.setUser(u);
				em.flush();
			}
		}
	}

	public Set<SkillRecord> getSkillRecordsForUser(int userId) {
		User u = em.find(User.class, userId);
		return u.getSkills();
	}

	public void addVacation(User user, TimeSpan timespan, User sessionUser) {
		if (authController.canUpdate(user, sessionUser)) {
			boolean intersects = false;
			User managedUser = em.find(User.class, user.getId());
			List<TimeSpan> userVacation = managedUser.getVacation();
			if (!userVacation.isEmpty()) {
				for (TimeSpan ts : userVacation) {
					if (timespan.intersects(ts)) {
						intersects = true; // Don't add vacation, it's a doublette
					}
				}
			}
			if (!intersects) {
				em.persist(timespan);
				em.flush();
				managedUser.addVacation(timespan);
				logger.log(sessionController.getUser().getUsername(), "User", Action.UPDATE, user.getId(),
						"Vacation added for User \"" + user.getUsername() + "\" .");
			}
		}
	}

	public void removeVacation(User user, int timeSpanId, User sessionUser) {
		if (authController.canUpdate(user, sessionUser)) {
			User managedUser = em.find(User.class, user.getId());
			TimeSpan managedTimeSpan = em.find(TimeSpan.class, timeSpanId);
			managedUser.removeVacation(managedTimeSpan.getId());
			em.remove(managedTimeSpan);
			em.flush();
			logger.log(sessionController.getUser().getUsername(), "User", Action.UPDATE, user.getId(),
					"Vacation removed for User \"" + user.getUsername() + "\" .");
		}
	}

	public List<TimeSpan> getVacation(User user) {
		return user.getVacation();
	}

	public void setIllness(User user, TimeSpan timespan, User sessionUser) {
		User managedUser = em.find(User.class, user.getId());
		if (authController.canUpdate(managedUser, sessionUser)) {
			em.persist(timespan);
			em.flush();
			managedUser.setIllness(timespan);
			logger.log(sessionController.getUser().getUsername(), "User", Action.UPDATE, user.getId(),
					"Illness added for User \"" + user.getUsername() + "\" .");
		}
	}

	public void removeIllness(User user, int timeSpanId, User sessionUser) {
		User managedUser = em.find(User.class, user.getId());
		if (authController.canUpdate(managedUser, sessionUser)) {
			TimeSpan managedTimeSpan = em.find(TimeSpan.class, timeSpanId);
			managedUser.removeIllness();
			em.remove(managedTimeSpan);
			em.flush();
			logger.log(sessionController.getUser().getUsername(), "User", Action.DELETE, user.getId(),
					"Illness removed for User \"" + user.getUsername() + "\" .");
		}
	}

	public TimeSpan getIllness(User user) {
		return user.getIllness();
	}

	public String generateApiKey(User user, User sessionUser) {
		String apiKey = UUID.randomUUID().toString();
		User u = em.find(User.class, user.getId());
		if (authController.canUpdate(u, sessionUser)) {
			u.setApiKey(apiKey);
			return apiKey;
		} else {
			return null;
		}

	}

	public void raiseEvent(PObject source, String name, String oldValue, String newValue, long lifetime) {
		if (InitializationController.getAsBoolean(InitializationController.FIRE_USER_EVENTS)) {
			Event evt = eventRegistry.getEventBuilder().newEvent().setSource(source).setOldValue(oldValue)
					.setNewValue(newValue).setPropertyName(name).setLifetime(lifetime).getEvent();
			eventRegistry.addEvent(evt);
		}
	}

	@Override
	public void consumeEvent(PObject o, Event evt) {
		System.out.println("Object " + o.toString() + " raised event: " + evt.getPropertyName()
				+ " with new Value: " + evt.getNewValue() + "--- User listening: " + ((User)o).getUsername());

	}

}
