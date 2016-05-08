package de.hallerweb.enterprise.prioritize.model.event;

import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

/**
 * Event Object to holf information about an occured event. It holds information
 * about the source object which triggered the event, the time when the event occured
 * and the lifetime of the event (= Number of milliseconds the event is held alive and can be
 * send to target listeners. After that time, the event is not delivered anymore and erased).
 * @author peter
 *
 */
@Entity
public class Event {

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	int id;

	Date when;
	PObjectType sourceType;
	int sourceId;
	String propertyName;
	Object oldValue;
	Object newValue;
	long lifetime;
	public Date getWhen() {
		return when;
	}
	public void setWhen(Date when) {
		this.when = when;
	}
	public PObjectType getSourceType() {
		return sourceType;
	}
	public void setSourceType(PObjectType sourceType) {
		this.sourceType = sourceType;
	}
	public int getSourceId() {
		return sourceId;
	}
	public void setSourceId(int sourceId) {
		this.sourceId = sourceId;
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
	public void setOldValue(Object oldValue) {
		this.oldValue = oldValue;
	}
	public Object getNewValue() {
		return newValue;
	}
	public void setNewValue(Object newValue) {
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
