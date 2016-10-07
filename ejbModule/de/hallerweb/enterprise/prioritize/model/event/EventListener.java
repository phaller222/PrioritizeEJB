package de.hallerweb.enterprise.prioritize.model.event;

import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToOne;

import de.hallerweb.enterprise.prioritize.model.PObject;

/**
 * {@link EventListener} holds information about Prioritize-Objects which are interrested
 * in changes of other Prioritize-Objects.
 *
 */
@Entity
@NamedQueries({ @NamedQuery(name = "findEventListenersBySourceAndPropertyName", query = "select el FROM EventListener el WHERE el.propertyName = :propertyName AND el.source.id = :id"),
	@NamedQuery(name = "findEventListenersWithLimitedLifetime", query = "select el FROM EventListener el WHERE el.lifetime > 0")
})
public class EventListener {

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	int id;
	
	@OneToOne
	PObject destination;						// Type of the Prioritize-Object which is interrested in events
	@OneToOne
	PObject source;								// Source of the Event to listen to
	String propertyName;							// Name of the property of which changes should be tracked.
	boolean oneShot;							// If true, an Event(change) is only send once and then this listener is deleted.
	Date createdAt;								// Date when this Listener has been created
	long lifetime;								// Time in milliseconds until this Listener should be removed automatically. -1 if never.
	
	public PObject getDestination() {
		return destination;
	}
	
	public void setDestination(PObject destination) {
		this.destination = destination;
	}
	
	public PObject getSource() {
		return source;
	}
	
	public void setSource(PObject source) {
		this.source = source;
	}
	
	public String getProperyName() {
		return propertyName;
	}
	
	public void setProperyName(String properyName) {
		this.propertyName = properyName;
	}
	
	public boolean isOneShot() {
		return oneShot;
	}
	
	public void setOneShot(boolean oneShot) {
		this.oneShot = oneShot;
	}
	
	public Date getCreatedAt() {
		return createdAt;
	}
	
	public void setCreatedAt(Date createdAt) {
		this.createdAt = createdAt;
	}
	
	public long getLifetime() {
		return lifetime;
	}
	
	public void setLifetime(long lifetime) {
		this.lifetime = lifetime;
	}
	
	public int getId() {
		return id;
	}
	
}
