package de.hallerweb.enterprise.prioritize.model.calendar;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToMany;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Version;

import com.fasterxml.jackson.annotation.JsonBackReference;

import de.hallerweb.enterprise.prioritize.model.Department;
import de.hallerweb.enterprise.prioritize.model.resource.Resource;
import de.hallerweb.enterprise.prioritize.model.security.PAuthorizedObject;
import de.hallerweb.enterprise.prioritize.model.security.User;

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
@NamedQueries({ @NamedQuery(name = "findTimeSpansByUser", query = "select ts FROM TimeSpan ts WHERE :user MEMBER OF ts.involvedUsers") })
public class TimeSpan implements PAuthorizedObject {

	public static enum TimeSpanType {
		RESOURCE_RESERVATION, VACATION, ILLNESS, OTHER, ALL
	}

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	int id;

	private String title;

	@Column(length = 3000)
	private String description;

	@ManyToMany(fetch = FetchType.EAGER)
	@JsonBackReference
	private Set<Resource> involvedResources;

	@ManyToMany(fetch = FetchType.EAGER)
	@JsonBackReference
	private Set<User> involvedUsers;

	@Version
	private int entityVersion; // For optimistic locks

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
			this.involvedResources = new HashSet<Resource>();
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
			this.involvedUsers = new HashSet<User>();
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
		} else if (until.before(this.dateFrom)) {
			return false;
		} else
			return true;
	}

	@Override
	public Department getDepartment() {
		// Implementation for PAuthorizedObjeect
		return null;
	}

}
