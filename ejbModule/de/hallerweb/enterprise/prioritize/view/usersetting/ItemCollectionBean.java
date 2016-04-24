package de.hallerweb.enterprise.prioritize.view.usersetting;

import java.io.Serializable;

import javax.ejb.EJB;
import javax.enterprise.context.SessionScoped;
import javax.inject.Inject;
import javax.inject.Named;

import de.hallerweb.enterprise.prioritize.controller.security.SessionController;
import de.hallerweb.enterprise.prioritize.controller.usersetting.ItemCollectionController;
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


	@Named
	public ItemCollection createItemCollection(String name, String description) {
		return itemCollectionController.createItemCollection("MyItems", "First test to create ITemCollection",sessionController.getUser());
	}
	

}
