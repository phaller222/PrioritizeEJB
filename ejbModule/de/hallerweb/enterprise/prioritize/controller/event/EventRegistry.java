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
package de.hallerweb.enterprise.prioritize.controller.event;

import de.hallerweb.enterprise.prioritize.controller.InitializationController;
import de.hallerweb.enterprise.prioritize.controller.document.DocumentController;
import de.hallerweb.enterprise.prioritize.controller.security.UserRoleController;
import de.hallerweb.enterprise.prioritize.model.PObject;
import de.hallerweb.enterprise.prioritize.model.document.DocumentInfo;
import de.hallerweb.enterprise.prioritize.model.event.Event;
import de.hallerweb.enterprise.prioritize.model.event.EventListener;
import de.hallerweb.enterprise.prioritize.model.event.PEventConsumerProducer;
import de.hallerweb.enterprise.prioritize.model.security.User;
import org.jboss.logging.Logger;

import javax.annotation.PostConstruct;
import javax.ejb.*;
import javax.inject.Inject;
import javax.persistence.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

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
	DocumentController documentController;
	@EJB
	UserRoleController userController;
	@Inject
	InitializationController initController;

	public enum EventStrategy {
		IMMEDIATE, DELAYED
	}

	private EventStrategy eventStrategie = EventStrategy.DELAYED;  // Default = DELAYED

	private HashMap<Class<? extends PObject>, PEventConsumerProducer> destinationMapping;

	@PostConstruct
	public void initialize() {
		destinationMapping = new HashMap<>();
		destinationMapping.put(User.class, userController);
		destinationMapping.put(DocumentInfo.class, documentController);

		// Override Event Strategy value with config
		try {
			if (initController.config.get(InitializationController.EVENT_DEFAULT_STRATEGY).equals("IMMEDIATE")) {
				eventStrategie = EventStrategy.IMMEDIATE;
			} else {
				eventStrategie = EventStrategy.DELAYED;
			}
		} catch (NullPointerException ex) {
			eventStrategie = EventStrategy.IMMEDIATE;
		}
	}

	public void addEvent(Event evt) {
		mng.persist(evt);
		List<EventListener> listeners = getEventListenersRegisteredFor(evt.getSource(), evt.getPropertyName());
		if (!listeners.isEmpty() && eventStrategie == EventStrategy.IMMEDIATE) {
			// If immediate strategy, deliver event at once.
			processEvent(evt, listeners);
		}
	}

	private void processEvent(Event evt, List<EventListener> listeners) {
		if ((listeners != null) && (!listeners.isEmpty())) {
			List<EventListener> listenersToRemove = new ArrayList<>();
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
				return new ArrayList<>();
			} else {
				return result;
			}
		} catch (NoResultException ex) {
			return new ArrayList<>();
		}
	}

	public void removeEventListener(int eventListenerId) {
		Query query = mng.createNamedQuery("findEventListenersById");
		query.setParameter("id", eventListenerId);
		EventListener listener = (EventListener) query.getSingleResult();
		mng.remove(listener);
	}

	@Schedule(minute = "*/5", hour = "*", persistent = false)
	/**
	 * Process all event currently present in datastore. Checks if events lifetime has passed
	 * and removes them if necessary.
	 * Checks all event listeners for events to be delivered and delivers them.
	 */
	public void processEvents() {
		long currentDateMillis = new Date().getTime();
		Logger.getLogger(this.getClass()).debug("Runnig processEvents at " + currentDateMillis + "...");
		
		// Remove all events which lifetime is passed
		Query q1 = mng.createNamedQuery("findEventsWithLimitedLifetime");
		try {
			List<Event> lifetimeEvents = q1.getResultList();
			removeEventsWithExpiredLifetime(lifetimeEvents);
		} catch (EntityNotFoundException ex) {
			// Don't log
		}

		// Remove all EventListeners which lifetime is passed
		Query q2 = mng.createNamedQuery("findEventListenersWithLimitedLifetime");
		List<EventListener> lifetimeListeners = q2.getResultList();
		removeEventListenersWithExpiredLifetime(lifetimeListeners);
		
		// Finally process all remaining events
		processRemainingEvents();
	}

	private void processRemainingEvents() {
		Query q3 = mng.createNamedQuery("findAllEvents");
		List<Event> events = q3.getResultList();
		for (Event evt : events) {
			if (evt.getSource() != null) {
				Logger.getLogger(this.getClass()).debug(("Processing: " + evt.getLifetime()));
				processEvent(evt, getEventListenersRegisteredFor(evt.getSource(), evt.getPropertyName()));
			}
		}
	}

	private void removeEventListenersWithExpiredLifetime(List<EventListener> lifetimeListeners) {
		long currentDateMillis = new Date().getTime();
		List<EventListener> listenersToRemove = new ArrayList<>();
		if ((lifetimeListeners != null) && (!lifetimeListeners.isEmpty())) {
			for (EventListener listener : lifetimeListeners) {
				if ((listener.getCreatedAt().getTime() + listener.getLifetime()) <= currentDateMillis) {
					listenersToRemove.add(listener);
				}
			}
			for (EventListener listener : listenersToRemove) {
				Logger.getLogger(this.getClass()).debug(("Removing listener: " + listener.getLifetime()));
				mng.remove(listener);
			}

		}
	}

	private void removeEventsWithExpiredLifetime(List<Event> lifetimeEvents) {
		long currentDateMillis = new Date().getTime();
		List<Event> eventsToRemove = new ArrayList<>();
		if ((lifetimeEvents != null) && (!lifetimeEvents.isEmpty())) {
			for (Event evt : lifetimeEvents) {
				if ((evt.getEventDate().getTime() + evt.getLifetime()) <= currentDateMillis) {
					eventsToRemove.add(evt);
				}
			}
			for (Event evt : eventsToRemove) {
				Logger.getLogger(this.getClass()).debug(("Removing event: " + evt.getLifetime()));
				mng.remove(evt);
			}
		}
	}
}