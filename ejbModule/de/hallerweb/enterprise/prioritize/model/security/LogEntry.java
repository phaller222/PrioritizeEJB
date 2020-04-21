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
package de.hallerweb.enterprise.prioritize.model.security;

import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;

/**
 * JPA entity to represent a {@link LogEntry}. Theese kinds of logs represent a kind of auditing log. So for example events like when a User
 * is created, a document is beeing deleted or new resource is beeing detected are beeing logged. theese logs contain no technical stuff
 * like exceptions, ip adresses and so on. They are ment for auditing purposes only.
 * 
 * <p>
 * Copyright: (c) 2014
 * </p>
 * <p>
 * Peter Haller
 * </p>
 * 
 * @author peter
 */

@Entity
@Table(name = "log")
@NamedQueries({ @NamedQuery(name = "findLogEntryByUser", query = "select en FROM LogEntry en WHERE en.user = :username") })
public class LogEntry {

	@Id
	@GeneratedValue
	int id;

	String user; // User who was responsible for the logged action.
	String relatedObject; // The representative name of the object an action was performed on.
	int objectId; // The object-id of the object
	String what; // The action that was performed (CREATE/READ/UPDATE/DELETE)
	String description; // a Short description what happened.
	Date timestamp; // the exact timestamp the action was beeing performed.

	public Date getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(Date when) {
		this.timestamp = when;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getUser() {
		return user;
	}

	public void setUser(String user) {
		this.user = user;
	}

	public String getRelatedObject() {
		return relatedObject;
	}

	public void setRelatedObject(String relatedObject) {
		this.relatedObject = relatedObject;
	}

	public int getObjectId() {
		return objectId;
	}

	public void setObjectId(int objectId) {
		this.objectId = objectId;
	}

	public String getWhat() {
		return what;
	}

	public void setWhat(String what) {
		this.what = what;
	}

}
