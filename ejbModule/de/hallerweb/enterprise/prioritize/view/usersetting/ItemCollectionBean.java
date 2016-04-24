package de.hallerweb.enterprise.prioritize.view.usersetting;

import java.io.Serializable;
import java.util.List;
import java.util.Set;

import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.enterprise.context.SessionScoped;
import javax.inject.Inject;
import javax.inject.Named;

import de.hallerweb.enterprise.prioritize.controller.security.SessionController;
import de.hallerweb.enterprise.prioritize.controller.usersetting.ItemCollectionController;
import de.hallerweb.enterprise.prioritize.model.security.Role;
import de.hallerweb.enterprise.prioritize.model.security.User;
import de.hallerweb.enterprise.prioritize.model.usersetting.ItemCollection;

/**
 * ItemCollectionBean - handels logins.
 * 
 * <p>
 * Copyright: (c) 2015
 * </p>
 * <p>
 * Peter Haller
 * </p>
 * 
 * @author peter
 */
@Named
@SessionScoped
public class ItemCollectionBean implements Serializable {

	@EJB
	ItemCollectionController itemCollectionController;

	@Inject
	SessionController sessionController;

	ItemCollection newItemCollection; 										// ItemCollection to be created.

	/**
	 * Initialize empty {@link ItemCollection}
	 */
	@PostConstruct
	public void init() {
		newItemCollection = new ItemCollection();
	}
	
	public ItemCollection getNewItemCollection() {
		return newItemCollection;
	}

	public void setNewItemCollection(ItemCollection newItemCollection) {
		this.newItemCollection = newItemCollection;
	}

	public List<ItemCollection> getItemCollections() {
		return itemCollectionController.getItemCollections(sessionController.getUser());
	}

	@Named
	public ItemCollection createItemCollection() {
		return itemCollectionController.createItemCollection(newItemCollection.getName(), newItemCollection.getDescription(),
				sessionController.getUser());
	}

	public void addUser(String itemCollectionName, User user) {
		ItemCollection collection = itemCollectionController.getItemCollection(sessionController.getUser(), itemCollectionName);
		itemCollectionController.addUser(collection, user);
	}

	public void removeUser(String itemCollectionName, User user) {
		ItemCollection collection = itemCollectionController.getItemCollection(sessionController.getUser(), itemCollectionName);
		itemCollectionController.removeUser(collection, user);
	}

	public Set<User> getUsers(String itemCollectionName) {
		ItemCollection collection = itemCollectionController.getItemCollection(sessionController.getUser(), itemCollectionName);
		return collection.getUsers();
	}

}
