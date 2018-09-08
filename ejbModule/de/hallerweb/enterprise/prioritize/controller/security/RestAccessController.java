package de.hallerweb.enterprise.prioritize.controller.security;

import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import de.hallerweb.enterprise.prioritize.model.security.User;

/**
 * RestAccessController.java - Authorizes REST users and assigns the session
 * 
 * */
@Stateless
public class RestAccessController {

	@PersistenceContext(unitName = "MySqlDS")
	EntityManager em;

	@EJB
	UserRoleController userRoleController;

	@Inject
	SessionController sessionController;

	public User checkApiKey(String key) {
		User u = userRoleController.findUserByApiKey(key);
		if (u != null) {
			sessionController.setUser(u);
			return u;
		} else {
			return null;
		}
	}
}
