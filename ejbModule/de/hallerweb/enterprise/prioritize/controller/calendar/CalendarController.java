package de.hallerweb.enterprise.prioritize.controller.calendar;

import java.util.List;

import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

import de.hallerweb.enterprise.prioritize.controller.LoggingController;
import de.hallerweb.enterprise.prioritize.controller.security.SessionController;
import de.hallerweb.enterprise.prioritize.controller.security.UserRoleController;
import de.hallerweb.enterprise.prioritize.model.calendar.TimeSpan;
import de.hallerweb.enterprise.prioritize.model.calendar.TimeSpan.TimeSpanType;
import de.hallerweb.enterprise.prioritize.model.security.User;

/**
 * CalendarController.java 
 * CalenderController
 */
@Stateless
public class CalendarController {

	@PersistenceContext(unitName = "MySqlDS")
	EntityManager em;


	@EJB
	LoggingController logger;
	@Inject
	SessionController sessionController;

	public void mergeTimeSpan(TimeSpan newTimeSpan) {
		TimeSpan managedTimeSpan = em.find(TimeSpan.class, newTimeSpan.getId());
		managedTimeSpan.setDateFrom(newTimeSpan.getDateFrom());
		managedTimeSpan.setDateUntil(newTimeSpan.getDateUntil());
	}

	public List<TimeSpan> getTimeSpansForUser(User user) {
		Query query = em.createNamedQuery("findTimeSpansByUser");
		query.setParameter("user", user);
		List<TimeSpan> timespans = query.getResultList();
		if (!timespans.isEmpty()) {
			return timespans;
		} else {
			return null;
		}
	}

	public List<TimeSpan> getTimeSpansForUser(User user, TimeSpanType type) {
		Query query = em.createNamedQuery("findTimeSpansByUserAndType");
		query.setParameter("user", user);
		query.setParameter("type", type);
		List<TimeSpan> timespans = query.getResultList();
		if (!timespans.isEmpty()) {
			return timespans;
		} else {
			return null;
		}
	}

}
