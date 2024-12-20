/*
 * Copyright 2015-2024 Peter Michael Haller and contributors
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

package de.hallerweb.enterprise.prioritize.controller.usersetting;

import de.hallerweb.enterprise.prioritize.model.resource.Resource;
import de.hallerweb.enterprise.prioritize.model.security.User;
import de.hallerweb.enterprise.prioritize.model.usersetting.UserPreference;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

import java.util.List;

/**
 * UserSettingController.java - Controls the creation, modification and deletion of
 * User settings
 */
@Stateless
public class UserPreferenceController {

    @PersistenceContext
    EntityManager em;

    /**
     * Default constructor.
     */
    public UserPreferenceController() {
        // Auto-generated constructor stub
    }

    public UserPreference createUserPreference(User owner) {
        UserPreference pref = new UserPreference(owner);
        em.persist(pref);
        return pref;
    }


    public boolean deleteWatchedResource(UserPreference settings, Resource res) {
        UserPreference managedSettings = em.find(UserPreference.class, settings.getId());
        Resource managedResource = em.find(Resource.class, res.getId());
        managedSettings.removeWatchedResource(managedResource);
        return true;
    }


    public boolean addWatchedResource(UserPreference settings, Resource res) {
        UserPreference managedSettings = em.find(UserPreference.class, settings.getId());
        Resource managedResource = em.find(Resource.class, res.getId());
        return managedSettings.addWatchedResource(managedResource);
    }

    public boolean isResourceWached(UserPreference settings, Resource res) {
        UserPreference managedSettings = em.find(UserPreference.class, settings.getId());
        Resource managedResource = em.find(Resource.class, res.getId());
        return managedSettings.getWatchedResources().contains(managedResource);
    }

    public List<Resource> getWatchedResources(UserPreference settings) {
        UserPreference managedSettings = em.find(UserPreference.class, settings.getId());
        return managedSettings.getWatchedResources();
    }
}
