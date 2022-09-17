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

import de.hallerweb.enterprise.prioritize.model.security.User;

import javax.ejb.EJB;
import javax.ejb.LocalBean;
import javax.ejb.Stateful;
import javax.enterprise.context.SessionScoped;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.io.Serializable;

/**
 * Session Bean implementation class SessionController. Holds information of the current logged in user.
 */
@Stateful
@SessionScoped
@LocalBean
public class SessionController implements Serializable {

    private User user;

    @PersistenceContext
    transient EntityManager em;
    @EJB
    UserRoleController controller;

    public void setUser(User user) {
        this.user = user;
    }

    public User getUser() {
        if (user == null) {
            return null;
        } else {
            return em.find(User.class, user.getId());
        }
    }
}
