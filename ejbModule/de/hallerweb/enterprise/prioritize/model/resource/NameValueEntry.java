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
	public int compareTo(Object o) {
		NameValueEntry e = (NameValueEntry) o;
		return mqttName.compareTo(e.getName());
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + id;
		result = prime * result + ((mqttName == null) ? 0 : mqttName.hashCode());
		result = prime * result + ((mqttValues == null) ? 0 : mqttValues.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		NameValueEntry other = (NameValueEntry) obj;
		if (id != other.id)
			return false;
		if (mqttName == null) {
			if (other.mqttName != null)
				return false;
		} else if (!mqttName.equals(other.mqttName))
			return false;
		if (mqttValues == null) {
			if (other.mqttValues != null)
				return false;
		} else if (!mqttValues.equals(other.mqttValues))
			return false;
		return true;
	}

}
