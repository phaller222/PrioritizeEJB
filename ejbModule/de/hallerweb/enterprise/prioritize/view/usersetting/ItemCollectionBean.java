/*
 * Copyright 2015-2020 Peter Michael Haller and contributors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package de.hallerweb.enterprise.prioritize.view.usersetting;

import de.hallerweb.enterprise.prioritize.controller.security.SessionController;
import de.hallerweb.enterprise.prioritize.controller.usersetting.ItemCollectionController;
import de.hallerweb.enterprise.prioritize.model.document.DocumentInfo;
import de.hallerweb.enterprise.prioritize.model.security.User;
import de.hallerweb.enterprise.prioritize.model.usersetting.ItemCollection;
import jakarta.annotation.PostConstruct;
import jakarta.ejb.EJB;
import jakarta.enterprise.context.SessionScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;

import java.io.Serializable;
import java.util.List;
import java.util.Set;

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
    transient ItemCollectionController itemCollectionController;

    @Inject
    SessionController sessionController;

    transient ItemCollection newItemCollection;            // ItemCollection to be created.
    transient ItemCollection currentItemCollection;            // Selected item collection / to be edited.

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
        ItemCollection collection = itemCollectionController.getItemCollection(sessionController.getUser(),
            this.currentItemCollection.getName());
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
        ItemCollection collection = itemCollectionController.getItemCollection(sessionController.getUser(),
            this.currentItemCollection.getName());
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
