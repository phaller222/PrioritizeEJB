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

package de.hallerweb.enterprise.prioritize.controller.calendar;

import de.hallerweb.enterprise.prioritize.model.calendar.TimeSpan;
import de.hallerweb.enterprise.prioritize.model.calendar.TimeSpan.TimeSpanType;
import de.hallerweb.enterprise.prioritize.model.security.User;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import jakarta.persistence.TemporalType;
import java.util.Collections;
import java.util.Date;
import java.util.List;

/**
 * CalendarController.java
 */
@Stateless
public class CalendarController {

    @PersistenceContext
    EntityManager em;

    public void updateTimeSpan(TimeSpan newTimeSpan) {
        TimeSpan managedTimeSpan = em.find(TimeSpan.class, newTimeSpan.getId());
        managedTimeSpan.setDateFrom(newTimeSpan.getDateFrom());
        managedTimeSpan.setDateUntil(newTimeSpan.getDateUntil());
    }

    public List<TimeSpan> getTimeSpansForUser(User user) {
        Query query = em.createNamedQuery("findTimeSpansByUser");
        query.setParameter("user", user);
        List<TimeSpan> timespans = query.getResultList();
        if (timespans.isEmpty()) {
            return Collections.emptyList();
        } else {
            return timespans;
        }
    }

    public List<TimeSpan> getTimeSpansForUser(User user, TimeSpanType type) {
        Query query = em.createNamedQuery("findTimeSpansByUserAndType");
        query.setParameter("user", user);
        query.setParameter("type", type);
        List<TimeSpan> timespans = query.getResultList();
        if (timespans.isEmpty()) {
            return Collections.emptyList();
        } else {
            return timespans;
        }
    }

    public List<TimeSpan> getTimeSpansForDate(Date d) {
        Query query = em.createNamedQuery("findTimeSpansByDate");
        query.setParameter("date", d, TemporalType.DATE);
        List<TimeSpan> timespans = query.getResultList();
        if (timespans.isEmpty()) {
            return Collections.emptyList();
        } else {
            return timespans;
        }
    }

}
