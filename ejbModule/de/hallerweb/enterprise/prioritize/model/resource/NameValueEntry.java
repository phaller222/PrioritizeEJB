package de.hallerweb.enterprise.prioritize.model.resource;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

/**
 * JPA entity to represent a {@link NameValueEntry} pair. This entity is used to represent data variables sent from MQTT IoT devices (e.g.
 * SET:value:100)
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
@Entity
public class NameValueEntry implements Comparable<Object> {

	public int getId() {
		return id;
	}

	@Id
	@GeneratedValue
	int id;
	@Column(length = 65535)
	private String mqttValues; // comma separated mqttValues (if historic data).
	private String mqttName; // Name of the mqttName/value pair.

	public String getName() {
		return mqttName;
	}

	public void setName(String name) {
		this.mqttName = name;
	}

	public String getValues() {
		return mqttValues;
	}

	public void setValues(String values) {
		this.mqttValues = values;
	}

	@Override
	public int compareTo(Object obj) {
		NameValueEntry e = (NameValueEntry) obj;
		return mqttName.compareTo(e.getName());
	}
	
	@Override
	public String toString() {
		return this.mqttName;
	}

}
