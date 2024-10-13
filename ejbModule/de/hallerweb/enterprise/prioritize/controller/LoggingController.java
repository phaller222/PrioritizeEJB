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

package de.hallerweb.enterprise.prioritize.controller;

import de.hallerweb.enterprise.prioritize.model.security.LogEntry;
import de.hallerweb.enterprise.prioritize.model.security.User;
import jakarta.ejb.LocalBean;
import jakarta.ejb.Singleton;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;

import java.util.Date;
import java.util.List;

/**
 * Session Bean implementation class LoggingController. Responsible for logging important business actions like create, update or delete
 * objects. This Bean does not log low level technical events like http connections. Theese things should be covered by the Application
 * Server. Here also methods to analyse existing logs like finding all log entries for a specific user are defined.
 */
@Singleton
@LocalBean
public class LoggingController {

    private static boolean loggingEnabled = true;

    @PersistenceContext
    EntityManager em;

    public enum Action {
        CREATE, UPDATE, DELETE
    }

    /**
     * Default constructor.
     */
    public LoggingController() {
        // Auto-generated constructor stub
    }

    public void enableLogging() {
        loggingEnabled = true;
    }

    public void disableLogging() {
        loggingEnabled = false;
    }

    public boolean isLoggingEnabled() {
        return loggingEnabled;
    }

    public void log(String user, String relatedObject, Action what, int objectId) {
        log(user, relatedObject, what, objectId, "");
    }

    public void log(String user, String relatedObject, Action what, int objectId, String description) {
        // Log action to database
        if (loggingEnabled) {
            LogEntry entry = new LogEntry();
            entry.setUser(user);
            entry.setRelatedObject(relatedObject);
            entry.setWhat(what.toString());
            entry.setObjectId(objectId);
            entry.setDescription(description);
            entry.setTimestamp(new Date());
            em.persist(entry);
            em.flush();
        }
    }

    @SuppressWarnings("unchecked")
    public List<LogEntry> findLogEntriesByUser(User user) {
        Query q = em.createNamedQuery("findLogEntryByUser");
        q.setParameter("username", user.getUsername());
        return q.getResultList();
    }

}
