package de.hallerweb.enterprise.prioritize.controller.usersetting;

import java.util.List;

import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

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

	public ItemCollection getItemCollection(User user, String name) {
		Query q = em.createNamedQuery("findItemCollectionByUserAndName");
		q.setParameter("name",name);
		q.setParameter("user",user.getId());
		return (ItemCollection) q.getSingleResult();
	}
	
	public List<ItemCollection> getItemCollections(User user) {
		Query q = em.createNamedQuery("findItemCollectionsByUser");
		q.setParameter("id",user.getId());
		return  q.getResultList();
	}
	
	
	public void addUser(ItemCollection collection, User user) {
		ItemCollection managedCollection = em.find(ItemCollection.class, collection.getId());
		User managedUser = em.find(User.class, user.getId());
		managedCollection.addUser(managedUser);
	}
	
	public void removeUser(ItemCollection collection, User user) {
		ItemCollection managedCollection = em.find(ItemCollection.class, collection.getId());
		User managedUser = em.find(User.class, user.getId());
		managedCollection.removeUser(managedUser);
	}
	
}
