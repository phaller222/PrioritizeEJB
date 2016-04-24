package de.hallerweb.enterprise.prioritize.controller;

import java.util.Date;
import java.util.List;

import javax.ejb.LocalBean;
import javax.ejb.Singleton;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

import de.hallerweb.enterprise.prioritize.model.security.LogEntry;
import de.hallerweb.enterprise.prioritize.model.security.User;

/**
 * Session Bean implementation class LoggingController. Responsible for logging important business actions like create, update or delete
 * objects. This Bean does not log low level technical events like http connections. Theese things should be covered by the Application
 * Server. Here also methods to analyse existing logs like finding all log entries for a specific user are defined.
 * 
 */
@Singleton
@LocalBean
public class LoggingController {

	static public boolean LOGGING_ENABLED = true;

	@PersistenceContext
	EntityManager em;

	public static enum Action {
		CREATE, UPDATE, DELETE
	}

	/**
	 * Default constructor.
	 */
	public LoggingController() {
		// TODO Auto-generated constructor stub
	}

	public void enableLogging() {
		LOGGING_ENABLED = true;
	}

	public void disableLogging() {
		LOGGING_ENABLED = false;
	}

	public boolean isLoggingEnabled() {
		return LOGGING_ENABLED;
	}

	public void log(String user, String relatedObject, Action what, int objectId) {
		log(user, relatedObject, what, objectId, "");
	}

	public void log(String user, String relatedObject, Action what, int objectId, String description) {
		// Log action to database
		if (LOGGING_ENABLED) {
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
	public List<LogEntry> findLogEntriesByUser(User u) {
		Query q = em.createNamedQuery("findLogEntryByUser");
		q.setParameter("username", u.getUsername());
		List<LogEntry> result = q.getResultList();
		return result;
	}

}
