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
import de.hallerweb.enterprise.prioritize.model.document.DocumentInfo;
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
	ItemCollection currentItemCollection;									// Selected item collection / to be edited.
	
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
	
	@Named
	public void deleteItemCollection(ItemCollection collection) {
		itemCollectionController.deleteItemCollection(collection);
	}
	
	@Named 
	public String editItemCollection(ItemCollection collection) {
		this.currentItemCollection = collection;
		return "edititemcollections";
	}

	public void addUser(String itemCollectionName, User user) {
		ItemCollection collection = itemCollectionController.getItemCollection(sessionController.getUser(), itemCollectionName);
		itemCollectionController.addUser(collection, user);
	}

	public String removeUser(User user) {
		ItemCollection collection = itemCollectionController.getItemCollection(sessionController.getUser(), this.currentItemCollection.getName());
		itemCollectionController.removeUser(collection, user);
		return "itemcollections";
	}

	public Set<User> getUsers(String itemCollectionName) {
		ItemCollection collection = itemCollectionController.getItemCollection(sessionController.getUser(), itemCollectionName);
		return collection.getUsers();
	}
	
	public void addDocument(String itemCollectionName, DocumentInfo document) {
		ItemCollection collection = itemCollectionController.getItemCollection(sessionController.getUser(), itemCollectionName);
		itemCollectionController.addDocumentInfo(collection, document);
	}

	public String removeDocument(DocumentInfo document) {
		ItemCollection collection = itemCollectionController.getItemCollection(sessionController.getUser(), this.currentItemCollection.getName());
		itemCollectionController.removeDocumentInfo(collection, document);
		return "itemcollections";
	}

	public Set<DocumentInfo> getDocuments(String itemCollectionName) {
		ItemCollection collection = itemCollectionController.getItemCollection(sessionController.getUser(), itemCollectionName);
		return collection.getDocuments();
	}
	
	public ItemCollectionController getItemCollectionController() {
		return itemCollectionController;
	}

	public void setItemCollectionController(ItemCollectionController itemCollectionController) {
		this.itemCollectionController = itemCollectionController;
	}
	
	public ItemCollection getCurrentItemCollection() {
		return currentItemCollection;
	}

	public void setCurrentItemCollection(ItemCollection currentItemCollection) {
		this.currentItemCollection = currentItemCollection;
	}

}
