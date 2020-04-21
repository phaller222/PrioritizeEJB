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
package de.hallerweb.enterprise.prioritize.model.usersetting;

import java.util.List;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToMany;
import javax.persistence.OneToOne;

import org.jboss.resteasy.logging.Logger;

import de.hallerweb.enterprise.prioritize.model.resource.Resource;
import de.hallerweb.enterprise.prioritize.model.security.User;

/**
 * JPA entity to represent an {@link UserPreference}.
 * UserPreferences store the current user settings.
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
public class UserPreference {

	@Id
	@GeneratedValue
	int id;

	@OneToOne
	User owner;

	@ManyToMany(fetch = FetchType.EAGER)
	List<Resource> watchedResources;

	public UserPreference() {
		// Empty constructor due to EJB requirement
	}

	public UserPreference(User owner) {
		this.owner = owner;

	}

	public int getId() {
		return id;
	}

	public User getOwner() {
		return owner;
	}

	public void setOwner(User owner) {
		this.owner = owner;
	}

	public List<Resource> getWatchedResources() {
		return watchedResources;
	}

	public void setWatchedResources(List<Resource> resources) {
		this.watchedResources = resources;
	}

	public boolean addWatchedResource(Resource res) {
		if (!this.watchedResources.contains(res)) {
			return this.watchedResources.add(res);
		} else {
			return false;
		}

	}

	public boolean removeWatchedResource(Resource res) {
		Resource resToRemove = null;
		for (Resource resource : this.watchedResources) {
			if (res.getId() == resource.getId()) {
				resToRemove = resource;
			}
		}
		if (resToRemove != null) {
			try {
				// TODO: Klären warum dies 2 mal aufgerufen werden muss!!!
				this.watchedResources.remove(resToRemove);
				this.watchedResources.remove(resToRemove);
			} catch (Exception ex) {
				Logger.getLogger(getClass()).error(ex.getMessage());
			}
			return true;
		} else {
			return false;
		}
	}
}
