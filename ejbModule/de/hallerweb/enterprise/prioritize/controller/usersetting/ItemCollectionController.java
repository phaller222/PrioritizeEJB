package de.hallerweb.enterprise.prioritize.controller.usersetting;

import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import de.hallerweb.enterprise.prioritize.controller.LoggingController;
import de.hallerweb.enterprise.prioritize.controller.security.AuthorizationController;
import de.hallerweb.enterprise.prioritize.controller.security.SessionController;
import de.hallerweb.enterprise.prioritize.model.security.Role;
import de.hallerweb.enterprise.prioritize.model.security.User;
import de.hallerweb.enterprise.prioritize.model.usersetting.ItemCollection;

/**
 * UserRoleController.java - Controls the creation, modification and deletion of
 * {@link Role} and {@link User} objects.
 */
@Stateless
public class ItemCollectionController {

	@PersistenceContext(unitName = "MySqlDS")
	EntityManager em;

	@EJB
	LoggingController logger;

	@EJB
	AuthorizationController authController;

	@Inject
	SessionController sessionController;

	/**
	 * Default constructor.
	 */
	public ItemCollectionController() {
		// TODO Auto-generated constructor stub
	}
	
	public ItemCollection createItemCollection(String name, String description, User owner) {
		ItemCollection c = new ItemCollection(name, description, owner);
		em.persist(c);
		return c;
	}

}
