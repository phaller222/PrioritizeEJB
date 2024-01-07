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
package de.hallerweb.enterprise.prioritize.controller.project.task;

import de.hallerweb.enterprise.prioritize.controller.nfc.NFCUnitController;
import de.hallerweb.enterprise.prioritize.controller.security.AuthorizationController;
import de.hallerweb.enterprise.prioritize.model.calendar.TimeSpan;
import de.hallerweb.enterprise.prioritize.model.calendar.TimeSpan.TimeSpanType;
import de.hallerweb.enterprise.prioritize.model.nfc.NFCUnit;
import de.hallerweb.enterprise.prioritize.model.nfc.NFCUnit.NFCUnitType;
import de.hallerweb.enterprise.prioritize.model.project.task.Task;
import de.hallerweb.enterprise.prioritize.model.project.task.TaskStatus;
import de.hallerweb.enterprise.prioritize.model.project.task.TimeTracker;
import de.hallerweb.enterprise.prioritize.model.security.User;

import jakarta.ejb.EJB;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Stateless
public class TimeTrackerController implements Serializable {

    @PersistenceContext
    transient EntityManager em;

    @EJB
    AuthorizationController authController;

    @EJB
    NFCUnitController nfcUnitController;

    public TimeTracker createTimeTracker(String uuid, Task task, User sessionUser) {
        if (authController.canCreate(new TimeTracker(), sessionUser) && (getTimeTracker(uuid, sessionUser) == null)) {
            NFCUnit unit = nfcUnitController.createNFCUnit(task.getName(), task.getDescription(), NFCUnitType.TIMETRACKER, null, "", uuid);
            TimeTracker tracker = new TimeTracker();
            tracker.setNfcUnit(unit);
            tracker.setTask(task);
            em.persist(tracker);
        }
        return null;
    }

    /**
     * startTracking() : Performs the following operations on the {@link TimeTracker} object found by the given UUID:
     * - Set active to "true"
     * - Create new "activeTimeSpan" and set start time to current server time
     * - Assigns the task to the sessionUser
     * - Sets the underlying task to "ASSIGNED" or "STARTED" depending on the previous state.
     *
     * @param sessionUser
     */
    public void startTracking(TimeTracker tracker, User sessionUser) {
        tracker.setActive(true);

        TimeSpan ts = new TimeSpan();
        ts.setDateFrom(new Date());
        ts.setDescription(tracker.getTask().getDescription());
        ts.setTitle(tracker.getTask().getName());
        ts.addInvolvedUser(sessionUser);
        ts.setType(TimeSpanType.TIME_TRACKER);
        em.persist(ts);
        tracker.setActiveTimeSpan(ts);


        tracker.getTask().removeAssignee();
        tracker.getTask().setAssignee(sessionUser);


    }

    /**
     * stopTracking() : Performs the following operations on the {@link TimeTracker} object found by the given UUID:
     * - if the user is the same user who has started tracking:
     * - Sets active to false
     * - Set end time of "activeTimeSpan" to current server time
     * - Add the "activeTimeSpan" to the underlying task as new time tracking entry. User = sessionUser
     * - Sets the underlying task status to "STOPPED".
     * - If the user is a different user:
     * - Temporarilly sets active to "false".
     * - Temporarilly sets underlying task status to "STOPPED".
     * - Set end time of "activeTimeSpan" to current server time
     * - Add the "activeTimeSpan" to the underlying task as new time tracking entry. User = old user.
     * - Assigns the underlying task to the given sessionsUser
     * - Creates a new activeTimeSpan object with current user and sets start time to server time.
     * - Sets active to "true" again.
     * - Sets task status to "STARTED".
     *
     * @param sessionUser
     */
    public void stopTracking(TimeTracker tracker, User sessionUser) {
        // User is the same who started tracking
        if (sessionUser.getId() == ((User) tracker.getActiveTimeSpan().getInvolvedUsers().toArray()[0]).getId()) {
            tracker.setActive(false);
            tracker.getActiveTimeSpan().setDateUntil(new Date());
            tracker.getTask().addTimeSpent(tracker.getActiveTimeSpan());
            tracker.getTask().setTaskStatus(TaskStatus.STOPPED);
            // User is a different user
        } else {
            // Handle TimeTracking entry for old user
            tracker.setActive(false);
            tracker.getTask().setTaskStatus(TaskStatus.STOPPED);
            tracker.getActiveTimeSpan().setDateUntil(new Date());
            tracker.getTask().addTimeSpent(tracker.getActiveTimeSpan());

            // Activate new User
            tracker.getTask().removeAssignee();
            tracker.getTask().setAssignee(sessionUser);
            TimeSpan ts = new TimeSpan();
            ts.setDateFrom(new Date());
            ts.setDescription(tracker.getTask().getDescription());
            ts.setTitle(tracker.getTask().getName());
            ts.addInvolvedUser(sessionUser);
            ts.setType(TimeSpanType.TIME_TRACKER);
            em.persist(ts);
            tracker.setActiveTimeSpan(ts);
            tracker.setActive(true);
            tracker.getTask().setTaskStatus(TaskStatus.STARTED);
        }
    }


    /**
     * Calls startTracking and/or StopTracking based on the "active" state.
     */
    public void toggle(String uuid, User sessionUser) {
        TimeTracker tracker = getTimeTracker(uuid, sessionUser);
        if (tracker != null) {
            if (tracker.isActive()) {
                stopTracking(tracker, sessionUser);
            } else {
                startTracking(tracker, sessionUser);
            }
        }
    }


    public TimeTracker getTimeTracker(String uuid, User sessionUser) {
        if (authController.canRead(new TimeTracker(), sessionUser)) {
            Query q = em.createNamedQuery("findAllTimeTrackers");
            try {
                List<TimeTracker> trackers = q.getResultList();
                for (TimeTracker t : trackers) {
                    if (t.getNfcUnit().getUuid().equals(uuid)) {
                        return t;
                    }
                }
                return null;
            } catch (Exception ex) {
                return null;
            }
        } else {
            return null;
        }
    }

    public List<TimeTracker> getAllTimeTrackers(User sessionUser) {
        if (authController.canRead(new TimeTracker(), sessionUser)) {
            Query q = em.createNamedQuery("findAllTimeTrackers");
            return q.getResultList();
        } else {
            return new ArrayList<>();
        }
    }
}
