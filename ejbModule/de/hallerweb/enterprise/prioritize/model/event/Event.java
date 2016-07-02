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
 * Event Object to holf information about an occured event. It holds information
 * about the source object which triggered the event, the time when the event occured
 * and the lifetime of the event (= Number of milliseconds the event is held alive and can be
 * send to target listeners. After that time, the event is not delivered anymore and erased).
 * @author peter
 *
 */
@Entity
@NamedQueries({ @NamedQuery(name = "findEventsWithLimitedLifetime", query = "select e FROM Event e WHERE e.lifetime > 0"),
	@NamedQuery(name = "findAllEvents", query = "select e FROM Event e")
})
public class Event {

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	int id;

	Date eventDate;
	@OneToOne
	PObject source;
	String propertyName;
	String oldValue;
	String newValue;
	long lifetime;
	public Date getEventDate() {
		return eventDate;
	}
	public void setEventDate(Date when) {
		this.eventDate = when;
	}
	public PObject getSource() {
		return source;
	}
	public void setSource(PObject source) {
		this.source = source;
	}
	public String getPropertyName() {
		return propertyName;
	}
	public void setPropertyName(String propertyName) {
		this.propertyName = propertyName;
	}
	public Object getOldValue() {
		return oldValue;
	}
	public void setOldValue(String oldValue) {
		this.oldValue = oldValue;
	}
	public String getNewValue() {
		return newValue;
	}
	public void setNewValue(String newValue) {
		this.newValue = newValue;
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
