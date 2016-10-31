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
public class ActionBoardController extends PEventConsumerProducer{

	@PersistenceContext(unitName = "MySqlDS")
	EntityManager em;

	@Inject
	EventRegistry eventRegistry;

	
	public ActionBoard findActionBoardByName(String name) {
		Query q = em.createNamedQuery("findActionBoardByName");
		q.setParameter("actionBoardName", name);
		ActionBoard board = (ActionBoard) q.getSingleResult();
		return board;
	}
	
	public ActionBoard findActionBoardById(int id) {
		Query q = em.createNamedQuery("findActionBoardById");
		q.setParameter("actionBoardId",id);
		ActionBoard board = (ActionBoard) q.getSingleResult();
		return board;
	}
	
	public ActionBoard findActionBoardByOwner(int ownerId) {
		Query q = em.createNamedQuery("findActionBoardByOwner");
		q.setParameter("ownerId",ownerId);
		try {
		ActionBoard board = (ActionBoard) q.getSingleResult();
		return board;
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
		Query q = em.createNamedQuery("findActionBoardById");
		q.setParameter("actionBoardId", actionBoardId);
		ActionBoard board = (ActionBoard) q.getSingleResult();
		em.remove(board);
	}

	public void editActionBoard(int actionBoardId, String name, String description, PObject owner) {
		Query q = em.createNamedQuery("findActionBoardById");
		q.setParameter("actionBoardId", actionBoardId);
		ActionBoard managedBoard = (ActionBoard) q.getSingleResult();

		managedBoard.setDescriprion(description);
		managedBoard.setName(name);
		managedBoard.setOwner(owner);
	}

	public ActionBoardEntry post(int actionBoardId, String title, String message, Event source) {
		Query q = em.createNamedQuery("findActionBoardById");
		q.setParameter("actionBoardId", actionBoardId);
		ActionBoard board = (ActionBoard) q.getSingleResult();

		
		
		ActionBoardEntry entry = new ActionBoardEntry();
		entry.setTitle(title);
		entry.setMessage(message);
		em.persist(source);
		entry.setSource(source);
		entry.setActionBoard(board);
		
		em.persist(entry);
		
		board.addEntry(entry);
		
		
		// Raise event "New entry"
		raiseEvent(board, "entries", "","",0);
		
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
		Query q = em.createNamedQuery("findActionBoardById");
		q.setParameter("actionBoardId", actionBoardId);
		ActionBoard board = (ActionBoard) q.getSingleResult();

		eventRegistry.createEventListener(board, subscriber, "entries", -1L, true);
	}

	public void removeSubscriber(int actionBoardId, EventListener subscriber) {
		Query q = em.createNamedQuery("findActionBoardById");
		q.setParameter("actionBoardId", actionBoardId);
		ActionBoard board = (ActionBoard) q.getSingleResult();

		EventListener listenerToRemove = null;
		List<EventListener> subscribers = eventRegistry.getEventListenersRegisteredFor(board, "entries");
		for (EventListener listener : subscribers) {
			if (listener.getId() == subscriber.getId()) {
				listenerToRemove = listener;
			}
		}
		eventRegistry.removeEventListener(listenerToRemove.getId());
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
		// TODO Auto-generated method stub
		
	}
}
