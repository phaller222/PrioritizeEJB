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
package de.hallerweb.enterprise.prioritize.model.usersetting;

import de.hallerweb.enterprise.prioritize.model.document.DocumentInfo;
import de.hallerweb.enterprise.prioritize.model.inbox.Message;
import de.hallerweb.enterprise.prioritize.model.resource.Resource;
import de.hallerweb.enterprise.prioritize.model.security.User;
import jakarta.persistence.*;

import java.util.List;
import java.util.Set;

/**
 * JPA entity to represent an {@link ItemCollection}. ItemCollections represent personal grouped items (e.G. Users, Devices...) of
 * a User. It can be used for  example to send a message to a group of recipients.
 *
 * <p> Copyright: (c) 2014
 * </p>
 * <p>
 * Peter Haller
 * </p>
 *
 * @author peter
 */
@Entity
@NamedQuery(name = "findItemCollectionByUserAndName", query = "select c FROM ItemCollection c WHERE c.name = :name AND c.owner.id = :id")
@NamedQuery(name = "findItemCollectionByUserAndId", query = "select c FROM ItemCollection c WHERE c.id = :id AND c.owner.id = :userid")
@NamedQuery(name = "findItemCollectionsByUser", query = "select c FROM ItemCollection c WHERE c.owner.id = :id")
public class ItemCollection {

    @Id
    @GeneratedValue
    int id;
    String name;
    String description;

    @OneToOne
    User owner;
    @ManyToMany(fetch = FetchType.EAGER)
    Set<DocumentInfo> documents;
    @ManyToMany(fetch = FetchType.EAGER)
    Set<User> users;
    @ManyToMany(fetch = FetchType.EAGER)
    Set<Resource> resources;
    @ManyToMany(fetch = FetchType.EAGER)
    Set<Message> messages;

    public ItemCollection() {
        // Empty constructor due to EJB requirement
    }

    public ItemCollection(String name, String description, User owner) {
        this.name = name;
        this.description = description;
        this.owner = owner;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public User getOwner() {
        return owner;
    }

    public void setOwner(User owner) {
        this.owner = owner;
    }

    public Set<User> getUsers() {
        return users;
    }

    public void setUsers(Set<User> users) {
        this.users = users;
    }

    public void addUser(User user) {
        this.users.add(user);
    }

    public void addUsers(List<User> userlist) {
        this.users.addAll(userlist);
    }

    public void removeUser(User user) {
        this.users.remove(user);
    }

    public Set<Resource> getResources() {
        return resources;
    }

    public void setResources(Set<Resource> resources) {
        this.resources = resources;
    }

    public void addResource(Resource resource) {
        this.resources.add(resource);
    }

    public void addResources(List<Resource> resourcelist) {
        this.resources.addAll(resourcelist);
    }

    public void removeResource(Resource resource) {
        this.resources.remove(resource);
    }

    public Set<Message> getMessages() {
        return messages;
    }

    public void setMessages(Set<Message> messages) {
        this.messages = messages;
    }

    public void addMessage(Message msg) {
        this.messages.add(msg);
    }

    public void addMessages(List<Message> messagelist) {
        this.messages.addAll(messagelist);
    }

    public void removeMessage(Message msg) {
        this.messages.remove(msg);
    }

    public Set<DocumentInfo> getDocuments() {
        return documents;
    }

    public void setDocuments(Set<DocumentInfo> documents) {
        this.documents = documents;
    }

    public void addDocument(DocumentInfo doc) {
        this.documents.add(doc);
    }

    public void addDocuents(List<DocumentInfo> documentlist) {
        this.documents.addAll(documentlist);
    }

    public void removeDocument(DocumentInfo doc) {
        this.documents.remove(doc);
    }
}
