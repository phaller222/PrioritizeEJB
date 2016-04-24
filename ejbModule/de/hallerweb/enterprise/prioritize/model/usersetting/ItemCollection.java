package de.hallerweb.enterprise.prioritize.model.usersetting;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

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
}
