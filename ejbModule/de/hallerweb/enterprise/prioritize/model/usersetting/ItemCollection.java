package de.hallerweb.enterprise.prioritize.model.usersetting;

import java.util.List;
import java.util.Set;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToMany;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;

import de.hallerweb.enterprise.prioritize.model.document.Document;
import de.hallerweb.enterprise.prioritize.model.document.DocumentInfo;
import de.hallerweb.enterprise.prioritize.model.inbox.Message;
import de.hallerweb.enterprise.prioritize.model.resource.Resource;
import de.hallerweb.enterprise.prioritize.model.security.User;

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
@NamedQueries({
		@NamedQuery(name = "findItemCollectionByUserAndName", query = "select c FROM ItemCollection c WHERE c.name = :name AND c.owner.id = :id"),
		@NamedQuery(name = "findItemCollectionByUserAndId", query = "select c FROM ItemCollection c WHERE c.id = :id AND c.owner.id = :userid"),
		@NamedQuery(name = "findItemCollectionsByUser", query = "select c FROM ItemCollection c WHERE c.owner.id = :id")})
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

	public void addUser(User u) {
		this.users.add(u);
	}

	public void addUsers(List<User> userlist) {
		this.users.addAll(userlist);
	}

	public void removeUser(User u) {
		this.users.remove(u);
	}

	public Set<Resource> getResources() {
		return resources;
	}

	public void setResources(Set<Resource> resources) {
		this.resources = resources;
	}

	public void addResource(Resource r) {
		this.resources.add(r);
	}

	public void addResources(List<Resource> resourcelist) {
		this.resources.addAll(resourcelist);
	}

	public void removeResource(Resource r) {
		this.resources.remove(r);
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
