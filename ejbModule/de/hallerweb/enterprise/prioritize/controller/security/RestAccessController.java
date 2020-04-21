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
package de.hallerweb.enterprise.prioritize.controller.security;

import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import de.hallerweb.enterprise.prioritize.model.security.User;

/**
 * RestAccessController.java - Authorizes REST users and assigns the session
 * 
 * */
@Stateless
public class RestAccessController {

	@PersistenceContext
	EntityManager em;

	@EJB
	UserRoleController userRoleController;

	@Inject
	SessionController sessionController;

	public User checkApiKey(String key) {
		User u = userRoleController.findUserByApiKey(key);
		if (u != null) {
			sessionController.setUser(u);
			return u;
		} else {
			return null;
		}
	}
}
