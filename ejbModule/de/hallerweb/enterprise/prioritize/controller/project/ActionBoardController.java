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
package de.hallerweb.enterprise.prioritize.controller.project;

import java.util.List;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

import de.hallerweb.enterprise.prioritize.controller.InitializationController;
import de.hallerweb.enterprise.prioritize.controller.event.EventRegistry;
import de.hallerweb.enterprise.prioritize.model.PObject;
import de.hallerweb.enterprise.prioritize.model.event.Event;
import de.hallerweb.enterprise.prioritize.model.event.EventListener;
import de.hallerweb.enterprise.prioritize.model.event.PEventConsumerProducer;
import de.hallerweb.enterprise.prioritize.model.project.ActionBoard;
import de.hallerweb.enterprise.prioritize.model.project.ActionBoardEntry;
import de.hallerweb.enterprise.prioritize.model.security.User;

/**
 * ActionBoardController - Manages ActionBoard's, ActionBoardEntries and subscribers to ActionBoard's.
 * @author peter
 *
 */
@Stateless
public class ActionBoardController extends PEventConsumerProducer {

	private static final String QUERY_FIND_ACTION_BOARD_BY_ID = "findActionBoardById";
	private static final String PARAM_ACTION_BOARD_ID = "actionBoardId";
	private static final String PARAM_ACTION_BOARD_NAME = "actionBoardName";

	private static final String EVENT_ENTRY_ADDED = "entry";

	@PersistenceContext
	EntityManager em;

	@Inject
	EventRegistry eventRegistry;

	public ActionBoard findActionBoardByName(String name) {
		Query q = em.createNamedQuery("findActionBoardByName");
		q.setParameter(PARAM_ACTION_BOARD_NAME, name);
		return (ActionBoard) q.getSingleResult();
	}

	public ActionBoard findActionBoardById(int id) {
		Query q = em.createNamedQuery(QUERY_FIND_ACTION_BOARD_BY_ID);
		q.setParameter(PARAM_ACTION_BOARD_ID, id);
		return (ActionBoard) q.getSingleResult();
	}

	public ActionBoard findActionBoardByOwner(int ownerId) {
		Query q = em.createNamedQuery("findActionBoardByOwner");
		q.setParameter("ownerId", ownerId);
		try {
			return (ActionBoard) q.getSingleResult();
		} catch (NoResultException ex) {
			return null;
		}

	}

	public ActionBoard createActionBoard(String name, String desc, PObject owner) {
		ActionBoard board = new ActionBoard();
		board.setName(name);
		board.setDescriprion(desc);
		board.setOwner(owner);
		em.persist(board);
		return board;
	}

	public void removeActionBoard(int actionBoardId) {
		Query q = em.createNamedQuery(QUERY_FIND_ACTION_BOARD_BY_ID);
		q.setParameter(PARAM_ACTION_BOARD_ID, actionBoardId);
		ActionBoard board = (ActionBoard) q.getSingleResult();
		em.remove(board);
	}

	public void editActionBoard(int actionBoardId, String name, String description, PObject owner) {
		Query q = em.createNamedQuery(QUERY_FIND_ACTION_BOARD_BY_ID);
		q.setParameter(PARAM_ACTION_BOARD_ID, actionBoardId);
		ActionBoard managedBoard = (ActionBoard) q.getSingleResult();

		managedBoard.setDescriprion(description);
		managedBoard.setName(name);
		managedBoard.setOwner(owner);
	}

	public ActionBoardEntry post(int actionBoardId, String title, String message, Event source) {
		Query q = em.createNamedQuery(QUERY_FIND_ACTION_BOARD_BY_ID);
		q.setParameter(PARAM_ACTION_BOARD_ID, actionBoardId);

		ActionBoardEntry entry = new ActionBoardEntry();
		entry.setTitle(title);
		entry.setMessage(message);
		em.persist(source);
		entry.setSource(source);

		ActionBoard board = (ActionBoard) q.getSingleResult();
		entry.setActionBoard(board);

		em.persist(entry);

		board.addEntry(entry);
		raiseEvent(board, EVENT_ENTRY_ADDED, "", "", 0);

		return entry;
	}

	public void remove(int entryId) {
		Query q = em.createNamedQuery("findActionBoardEntryById");
		q.setParameter("id", entryId);
		ActionBoardEntry entry = (ActionBoardEntry) q.getSingleResult();

		entry.getActionBoard().removeEntry(entry);
		em.remove(entry);
	}

	public void addSubscriber(int actionBoardId, User subscriber) {
		Query q = em.createNamedQuery(QUERY_FIND_ACTION_BOARD_BY_ID);
		q.setParameter(PARAM_ACTION_BOARD_ID, actionBoardId);
		ActionBoard board = (ActionBoard) q.getSingleResult();

		eventRegistry.createEventListener(board, subscriber, "entries", -1L, true);
	}

	public void removeSubscriber(int actionBoardId, EventListener subscriber) {
		Query q = em.createNamedQuery(QUERY_FIND_ACTION_BOARD_BY_ID);
		q.setParameter(PARAM_ACTION_BOARD_ID, actionBoardId);
		ActionBoard board = (ActionBoard) q.getSingleResult();

		EventListener listenerToRemove = null;
		List<EventListener> subscribers = eventRegistry.getEventListenersRegisteredFor(board, "entries");
		for (EventListener listener : subscribers) {
			if (listener.getId() == subscriber.getId()) {
				listenerToRemove = listener;
			}
		}
		if (listenerToRemove != null) {
			eventRegistry.removeEventListener(listenerToRemove.getId());
		}
	}

	@Override
	public void raiseEvent(PObject source, String name, String oldValue, String newValue, long lifetime) {
		if (InitializationController.getAsBoolean(InitializationController.FIRE_ACTIONBOARD_EVENTS)) {
			Event evt = eventRegistry.getEventBuilder().newEvent().setSource(source).setOldValue(oldValue).setNewValue(newValue)
					.setPropertyName(name).setLifetime(lifetime).getEvent();
			eventRegistry.addEvent(evt);
		}
	}

	@Override
	public void consumeEvent(PObject destination, Event evt) {
		// Auto-generated method stub

	}
}
