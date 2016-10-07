package de.hallerweb.enterprise.prioritize.controller.event;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.ejb.LocalBean;
import javax.ejb.Schedule;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

import de.hallerweb.enterprise.prioritize.controller.CompanyController;
import de.hallerweb.enterprise.prioritize.controller.InitializationController;
import de.hallerweb.enterprise.prioritize.controller.document.DocumentController;
import de.hallerweb.enterprise.prioritize.controller.resource.ResourceController;
import de.hallerweb.enterprise.prioritize.controller.security.UserRoleController;
import de.hallerweb.enterprise.prioritize.model.PObject;
import de.hallerweb.enterprise.prioritize.model.document.DocumentInfo;
import de.hallerweb.enterprise.prioritize.model.event.Event;
import de.hallerweb.enterprise.prioritize.model.event.EventListener;
import de.hallerweb.enterprise.prioritize.model.event.PEventConsumerProducer;
import de.hallerweb.enterprise.prioritize.model.security.User;

/**
 * Session Bean implementation class EventRegistry. Handles Events and EventListeners and their lifecycle.
 * It is responsible for delivering events to the registered Listeners and removing Listeners and events 
 * if they shall no longer be active.
 */
@Singleton
@LocalBean
@Startup
public class EventRegistry {

	@PersistenceContext
	EntityManager mng;

	@EJB
	ResourceController resourceController;
	@EJB
	DocumentController documentController;
	@EJB
	UserRoleController userController;
	@EJB
	CompanyController companyController;
	@Inject
	InitializationController initController;

	public enum EventStrategy {
		IMMEDIATE, DELAYED
	}

	public static EventStrategy EVENT_STRATEGY = EventStrategy.DELAYED;  // Default = DELAYED

	private HashMap<Class<? extends PObject>, PEventConsumerProducer> destinationMapping;

	@PostConstruct
	public void initialize() {
		destinationMapping = new HashMap<Class<? extends PObject>, PEventConsumerProducer>();
		destinationMapping.put(User.class, userController);
		destinationMapping.put(DocumentInfo.class, documentController);

		// Override Event Strategy value with config
		try {
		if (InitializationController.config.get(InitializationController.EVENT_DEFAULT_STRATEGY).equals("IMMEDIATE")) {
			EVENT_STRATEGY = EventStrategy.IMMEDIATE;
		} else {
			EVENT_STRATEGY = EventStrategy.DELAYED;
		}
		} catch (NullPointerException ex) {
			EVENT_STRATEGY = EventStrategy.IMMEDIATE;
		}
	}

	public void addEvent(Event evt) {
		mng.persist(evt);
		List<EventListener> listeners = getEventListenersRegisteredFor(evt.getSource(), evt.getPropertyName());
		if (!listeners.isEmpty()) {

			// If immediate strategy, deliver event at once.
			if (EVENT_STRATEGY == EventStrategy.IMMEDIATE) {
				processEvent(evt, listeners);
			}
		}
	}

	private void processEvent(Event evt, List<EventListener> listeners) {
		if ((listeners != null) && (!listeners.isEmpty())) {
			List<EventListener> listenersToRemove = new ArrayList<EventListener>();
			for (EventListener listener : listeners) {
				PObject destination = listener.getDestination();
				destinationMapping.get(destination.getClass()).consumeEvent(destination, evt);
				if (listener.isOneShot()) {
					listenersToRemove.add(listener);
				}
			}
			if (!listenersToRemove.isEmpty()) {
				for (EventListener listener : listenersToRemove) {
					EventListener managedListener = mng.find(EventListener.class, listener.getId());
					mng.remove(managedListener);
				}
			}
		}
	}

	public EventBuilder getEventBuilder() {
		return new EventRegistry.EventBuilder();
	}

	public class EventBuilder {
		Event event;

		public EventBuilder newEvent() {
			event = new Event();
			return EventBuilder.this;
		}

		public EventBuilder setSource(PObject source) {
			this.event.setSource(source);
			return this;
		}

		public EventBuilder setPropertyName(String propertyName) {
			this.event.setPropertyName(propertyName);
			return this;
		}

		public EventBuilder setOldValue(String oldVValue) {
			this.event.setOldValue(oldVValue);
			return this;
		}

		public EventBuilder setNewValue(String newValue) {
			this.event.setNewValue(newValue);
			return this;
		}

		public EventBuilder setLifetime(long lifetime) {
			this.event.setLifetime(lifetime);
			return this;
		}

		public Event getEvent() {
			this.event.setEventDate(new Date());
			return this.event;
		}
	}

	public EventListener createEventListener(PObject source, PObject destination, String propertyName, long lifetime, boolean oneShot) {
		EventListener listener = new EventListener();
		listener.setSource(source);
		listener.setDestination(destination);
		listener.setProperyName(propertyName);
		listener.setCreatedAt(new Date());
		listener.setLifetime(lifetime);
		listener.setOneShot(oneShot);
		mng.persist(listener);
		return listener;
	}

	public List<EventListener> getEventListenersRegisteredFor(PObject source, String propertyName) {
		Query query = mng.createNamedQuery("findEventListenersBySourceAndPropertyName");
		query.setParameter("propertyName", propertyName);
		query.setParameter("id", source.getId());
		try {
			List<EventListener> result = query.getResultList();
			if (result.isEmpty()) {
				return new ArrayList<EventListener>();
			} else
				return result;
		} catch (NoResultException ex) {
			return new ArrayList<EventListener>();
		}
	}

	public void removeEventListener(int eventListenerId) {
		Query query = mng.createNamedQuery("findEventListenersById");
		query.setParameter("id", eventListenerId);
		EventListener listener = (EventListener) query.getSingleResult();
		mng.remove(listener);
	}

	@Schedule(second = "*/10", minute = "*", hour = "*", persistent = false)
	/**
	 * Process all event currently present in datastore. Checks if events lifetime has passed
	 * and removes them if necessary.
	 * Checks all event listeners for events to be delivered and delivers them.
	 */
	public void processEvents() {
		long currentDateMillis = new Date().getTime();
		System.out.println("Runnig processEvents at " + currentDateMillis + "...");
		// Remove all events which lifetime is passed
		List<Event> eventsToRemove = new ArrayList<Event>();
		Query q1 = mng.createNamedQuery("findEventsWithLimitedLifetime");
		List<Event> lifetimeEvents = q1.getResultList();
		if ((lifetimeEvents != null) && (!lifetimeEvents.isEmpty())) {
			for (Event evt : lifetimeEvents) {
				if ((evt.getEventDate().getTime() + evt.getLifetime()) <= currentDateMillis) {
					eventsToRemove.add(evt);
				}
			}
			for (Event evt : eventsToRemove) {
				System.out.println("Removing event: " + evt.getLifetime());
				mng.remove(evt);
			}
		}

		// Remove all EventListeners which lifetime is passed
		List<EventListener> listenersToRemove = new ArrayList<EventListener>();
		Query q2 = mng.createNamedQuery("findEventListenersWithLimitedLifetime");
		List<EventListener> lifetimeListeners = q2.getResultList();
		if ((lifetimeListeners != null) && (!lifetimeListeners.isEmpty())) {
			for (EventListener listener : lifetimeListeners) {
				if ((listener.getCreatedAt().getTime() + listener.getLifetime()) <= currentDateMillis) {
					listenersToRemove.add(listener);
				}
			}
			for (EventListener listener : listenersToRemove) {
				System.out.println("Removing listener: " + listener.getLifetime());
				mng.remove(listener);
			}

		}
		// Finally process all remaining events
		Query q3 = mng.createNamedQuery("findAllEvents");
		List<Event> events = q3.getResultList();
		for (Event evt : events) {
			if (evt.getSource() != null) {
				System.out.println("Processing: " + evt.getLifetime());
				processEvent(evt, getEventListenersRegisteredFor(evt.getSource(), evt.getPropertyName()));
			}
		}

	}

}