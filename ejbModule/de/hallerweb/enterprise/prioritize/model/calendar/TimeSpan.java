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
package de.hallerweb.enterprise.prioritize.model.calendar;

import com.fasterxml.jackson.annotation.JsonBackReference;
import de.hallerweb.enterprise.prioritize.model.Department;
import de.hallerweb.enterprise.prioritize.model.resource.Resource;
import de.hallerweb.enterprise.prioritize.model.security.PAuthorizedObject;
import de.hallerweb.enterprise.prioritize.model.security.User;

import jakarta.persistence.*;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

/**
 * JPA entity to represent a {@link TimeSpan}. A TimeSpan object represents a time span between 2 dates and is used by the calendar to
 * manage reservations and other things. A TimeSpan consists mainly of the start and end time, involved resources and involved users.
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
@NamedQueries({ @NamedQuery(name = "findTimeSpansByUser", query = "select ts FROM TimeSpan ts WHERE :user MEMBER OF ts.involvedUsers"),
	@NamedQuery(name = "findTimeSpansByUserAndType", query = "select ts FROM TimeSpan ts WHERE :user MEMBER OF ts.involvedUsers AND ts.type = :type")})
public class TimeSpan implements PAuthorizedObject {

	public enum TimeSpanType {
		RESOURCE_RESERVATION, VACATION, ILLNESS, TIME_TRACKER, OTHER, ALL
	}

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	int id;

	private String title;


	private String description;

	@ManyToMany(fetch = FetchType.EAGER)
	@JsonBackReference(value="involvedResourcesBackRef")
	private Set<Resource> involvedResources;

	@ManyToMany(fetch = FetchType.EAGER)
	@JsonBackReference(value="involvedUsersBackRef")
	private Set<User> involvedUsers;

	private Date dateFrom;
	private Date dateUntil;

	private TimeSpanType type;

	public TimeSpanType getType() {
		return type;
	}

	public void setType(TimeSpanType type) {
		this.type = type;
	}

	public Date getDateFrom() {
		return dateFrom;
	}

	public void setDateFrom(Date from) {
		this.dateFrom = from;
	}

	public Date getDateUntil() {
		return dateUntil;
	}

	public void setDateUntil(Date until) {
		this.dateUntil = until;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public Set<Resource> getInvolvedResources() {
		return involvedResources;
	}

	public void setInvolvedResources(Set<Resource> resources) {
		this.involvedResources = resources;
	}

	public void addInvolvedResource(Resource resource) {
		if (this.involvedResources == null) {
			this.involvedResources = new HashSet<>();
		}
		this.involvedResources.add(resource);

	}

	public Set<User> getInvolvedUsers() {
		return involvedUsers;
	}

	public void setInvolvedUsers(Set<User> involvedUsers) {
		this.involvedUsers = involvedUsers;
	}

	public void addInvolvedUser(User user) {
		if (this.involvedUsers == null) {
			this.involvedUsers = new HashSet<>();
		}
		this.involvedUsers.add(user);
	}

	/**
	 * Checks if the timeframe of a given TimeSpan intersects with this one.
	 * 
	 * @param ts The {@link TimeSpan} object to compare
	 * @return true if timeframes intersect, otherwise false.
	 */
	public boolean intersects(TimeSpan ts) {
		Date from = ts.getDateFrom();
		Date until = ts.getDateUntil();

		if (from.after(this.dateUntil)) {
			return false;
		} else return !until.before(this.dateFrom);
	}

	@Override
	public Department getDepartment() {
		// Implementation for PAuthorizedObjeect
		return null;
	}

}
