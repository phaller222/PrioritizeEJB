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

package de.hallerweb.enterprise.prioritize.controller.usersetting;

import de.hallerweb.enterprise.prioritize.model.document.DocumentInfo;
import de.hallerweb.enterprise.prioritize.model.resource.Resource;
import de.hallerweb.enterprise.prioritize.model.security.Role;
import de.hallerweb.enterprise.prioritize.model.security.User;
import de.hallerweb.enterprise.prioritize.model.usersetting.ItemCollection;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;

import java.util.List;

/**
 * UserRoleController.java - Controls the creation, modification and deletion of
 * {@link Role} and {@link User} objects.
 */
@Stateless
public class ItemCollectionController {

    @PersistenceContext
    EntityManager em;

    /**
     * Default constructor.
     */
    public ItemCollectionController() {
        // Auto-generated constructor stub
    }

    public ItemCollection createItemCollection(String name, String description, User owner) {
        ItemCollection c = new ItemCollection(name, description, owner);
        em.persist(c);
        return c;
    }

    public ItemCollection getItemCollection(User user, String name) {
        try {
            Query q = em.createNamedQuery("findItemCollectionByUserAndName");
            q.setParameter("name", name);
            q.setParameter("id", user.getId());
            return (ItemCollection) q.getSingleResult();
        } catch (NoResultException ex) {
            return null;
        }
    }

    public List<ItemCollection> getItemCollections(User user) {
        Query q = em.createNamedQuery("findItemCollectionsByUser");
        q.setParameter("id", user.getId());
        return q.getResultList();
    }

    public void deleteItemCollection(ItemCollection collection) {
        ItemCollection c = em.find(ItemCollection.class, collection.getId());
        em.remove(c);
    }


    public void addUser(ItemCollection collection, User user) {
        ItemCollection managedCollection = em.find(ItemCollection.class, collection.getId());
        User managedUser = em.find(User.class, user.getId());
        if (managedUser.getId() != managedCollection.getOwner().getId()) {
            managedCollection.addUser(managedUser);
        }
    }

    public void removeUser(ItemCollection collection, User user) {
        ItemCollection managedCollection = em.find(ItemCollection.class, collection.getId());
        User managedUser = em.find(User.class, user.getId());
        managedCollection.removeUser(managedUser);
    }

    public void addDocumentInfo(ItemCollection collection, DocumentInfo info) {
        ItemCollection managedCollection = em.find(ItemCollection.class, collection.getId());
        DocumentInfo managedDocInfo = em.find(DocumentInfo.class, info.getId());
        managedCollection.addDocument(managedDocInfo);
    }

    public void removeDocumentInfo(ItemCollection collection, DocumentInfo info) {
        ItemCollection managedCollection = em.find(ItemCollection.class, collection.getId());
        DocumentInfo managedDocInfo = em.find(DocumentInfo.class, info.getId());
        managedCollection.removeDocument(managedDocInfo);
    }

    public void addResource(ItemCollection collection, Resource resource) {
        ItemCollection managedCollection = em.find(ItemCollection.class, collection.getId());
        Resource managedResource = em.find(Resource.class, resource.getId());
        managedCollection.addResource(managedResource);
    }

    public void removeResource(ItemCollection collection, Resource resource) {
        ItemCollection managedCollection = em.find(ItemCollection.class, collection.getId());
        Resource managedResource = em.find(Resource.class, resource.getId());
        managedCollection.removeResource(managedResource);
    }

}
