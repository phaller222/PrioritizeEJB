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

import de.hallerweb.enterprise.prioritize.controller.InitializationController;
import de.hallerweb.enterprise.prioritize.model.PObject;
import de.hallerweb.enterprise.prioritize.model.project.ActionBoard;
import de.hallerweb.enterprise.prioritize.model.project.ActionBoardEntry;

import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

/**
 * ActionBoardController - Manages ActionBoard's, ActionBoardEntries and subscribers to ActionBoard's.
 * @author peter
 *
 */
@Stateless
public class ActionBoardController  {

	private static final String QUERY_FIND_ACTION_BOARD_BY_ID = "findActionBoardById";
	private static final String PARAM_ACTION_BOARD_ID = "actionBoardId";
	private static final String PARAM_ACTION_BOARD_NAME = "actionBoardName";

	@PersistenceContext
	EntityManager em;

	@EJB
	InitializationController initController;

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

	public ActionBoardEntry post(int actionBoardId, String title, String message) {
		Query q = em.createNamedQuery(QUERY_FIND_ACTION_BOARD_BY_ID);
		q.setParameter(PARAM_ACTION_BOARD_ID, actionBoardId);

		ActionBoardEntry entry = new ActionBoardEntry();
		entry.setTitle(title);
		entry.setMessage(message);

		ActionBoard board = (ActionBoard) q.getSingleResult();
		entry.setActionBoard(board);

		em.persist(entry);

		board.addEntry(entry);
		return entry;
	}

	public void remove(int entryId) {
		Query q = em.createNamedQuery("findActionBoardEntryById");
		q.setParameter("id", entryId);
		ActionBoardEntry entry = (ActionBoardEntry) q.getSingleResult();

		entry.getActionBoard().removeEntry(entry);
		em.remove(entry);
	}
}

