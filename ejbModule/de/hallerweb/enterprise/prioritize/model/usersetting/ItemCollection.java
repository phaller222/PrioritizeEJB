package de.hallerweb.enterprise.prioritize.model.usersetting;

import java.util.Set;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;

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
public class ItemCollection {

	@Id
	@GeneratedValue
	int id;
	String name;
	String description;
	
	@OneToOne
	User owner;
	@OneToMany(fetch = FetchType.EAGER)
	Set<DocumentInfo> documents;
	@OneToMany(fetch = FetchType.EAGER)
	Set<User> users;
	@OneToMany(fetch = FetchType.EAGER)
	Set<Resource> resources;
	@OneToMany(fetch = FetchType.EAGER)
	Set<Message> message;
	
	public ItemCollection(){
		// Empty constructor due to EJB requirement
	}
	
	public ItemCollection(String name, String description, User owner) {
		this.name = name;
		this.description = description;
		this.owner = owner;
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
	
}
