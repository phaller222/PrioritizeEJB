package de.hallerweb.enterprise.prioritize.controller.usersetting;

import java.util.List;

import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

import de.hallerweb.enterprise.prioritize.controller.LoggingController;
import de.hallerweb.enterprise.prioritize.controller.resource.ResourceController;
import de.hallerweb.enterprise.prioritize.controller.security.AuthorizationController;
import de.hallerweb.enterprise.prioritize.controller.security.SessionController;
import de.hallerweb.enterprise.prioritize.controller.security.UserRoleController;
import de.hallerweb.enterprise.prioritize.model.document.DocumentInfo;
import de.hallerweb.enterprise.prioritize.model.resource.Resource;
import de.hallerweb.enterprise.prioritize.model.security.User;
import de.hallerweb.enterprise.prioritize.model.usersetting.ItemCollection;
import de.hallerweb.enterprise.prioritize.model.usersetting.UserPreference;

/**
 * UserSettingController.java - Controls the creation, modification and deletion of
 * User settings
 */
@Stateless
public class UserPreferenceController {

	@PersistenceContext(unitName = "MySqlDS")
	EntityManager em;

	@EJB
	LoggingController logger;

	@EJB
	AuthorizationController authController;
	
	@EJB
	ResourceController resourceController;

	@Inject
	SessionController sessionController;

	/**
	 * Default constructor.
	 */
	public UserPreferenceController() {
		// Auto-generated constructor stub
	}
	
	public UserPreference createUserPreference(User owner) {
		UserPreference pref = new UserPreference(owner);
		em.persist(pref);
		return pref;
	}

	
	
	public boolean deleteWatchedResource(UserPreference settings, Resource res) {
		UserPreference managedSettings  = em.find(UserPreference.class, settings.getId());
		Resource managedResource = em.find(Resource.class, res.getId()); 
		managedSettings.removeWatchedResource(managedResource);
		return true;
	}
	
	
	public boolean addWatchedResource(UserPreference settings, Resource res) {
		UserPreference managedSettings  = em.find(UserPreference.class, settings.getId());
		Resource managedResource = em.find(Resource.class, res.getId()); 
		return managedSettings.addWatchedResource(managedResource);
	}
	
	public boolean isResourceWached(UserPreference settings, Resource res) {
		UserPreference managedSettings  = em.find(UserPreference.class, settings.getId());
		Resource managedResource = em.find(Resource.class, res.getId()); 
		return managedSettings.getWatchedResources().contains(managedResource);
	}
	
	public List<Resource> getWatchedResources(UserPreference settings) {
		UserPreference managedSettings  = em.find(UserPreference.class, settings.getId());
		return managedSettings.getWatchedResources();
	}
}
