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
package de.hallerweb.enterprise.prioritize.controller.inbox;

import java.util.Date;
import java.util.List;

import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.enterprise.context.ContextNotActiveException;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

import de.hallerweb.enterprise.prioritize.controller.LoggingController;
import de.hallerweb.enterprise.prioritize.controller.LoggingController.Action;
import de.hallerweb.enterprise.prioritize.controller.security.SessionController;
import de.hallerweb.enterprise.prioritize.controller.security.UserRoleController;
import de.hallerweb.enterprise.prioritize.model.inbox.Message;
import de.hallerweb.enterprise.prioritize.model.security.User;

/**
 * InboxController.java - Controls the creation, modification and deletion of {@link Message} objects.
 * 
 */
@Stateless
public class MessageController {

	@PersistenceContext(unitName = "Prioritze")
	EntityManager em;

	@EJB
	UserRoleController userRoleController;

	@EJB
	LoggingController logger;

	@Inject
	SessionController sessionController;
	
	private static final String PARAMETER_USER_ID = "userId";
	private static final String LITERAL_MESSAGE = "Message";

	public Message createMessage(User from, User to, String subject, String message) {
		Message msg = new Message();
		msg.setFrom(userRoleController.findUserById(from.getId(),from));
		msg.setTo(userRoleController.findUserById(to.getId(),from));
		msg.setSubject(subject);
		msg.setContent(message);
		msg.setMessageRead(false);
		msg.setDateReceived(new java.util.Date());
		msg.setDateRead(null);
		em.persist(msg);
		em.flush();

		try {
			logger.log(sessionController.getUser().getUsername(), LITERAL_MESSAGE, Action.CREATE, msg.getId(), " " + LITERAL_MESSAGE + " \"" + msg.getSubject()
					+ "\" created.");
		} catch (ContextNotActiveException ex) {
			logger.log("SYSTEM", LITERAL_MESSAGE , Action.CREATE, msg.getId(), " Message \"" + msg.getSubject() + "\" created.");
		}
		return msg;
	}

	public void deleteMessage(int id) {
		Message msg = findMessageById(id);
		em.remove(msg);
		em.flush();
		logger.log(sessionController.getUser().getUsername(), LITERAL_MESSAGE, Action.DELETE, msg.getId(), " Message \"" + msg.getSubject()
				+ "\" deleted.");
	}

	public Message findMessageById(int id) {
		return em.find(Message.class, id);
	}

	@SuppressWarnings("unchecked")
	public List<Message> getReceivedMessages(User user) {
		Query query = em.createNamedQuery("findReceivedMessagesForUser");
		query.setParameter(PARAMETER_USER_ID, user.getId());
		return (List<Message>) query.getResultList();
	}
	
	@SuppressWarnings("unchecked")
	public List<Message> getUnreadMessages(User user) {
		Query query = em.createNamedQuery("findUnreadMessagesForUser");
		query.setParameter(PARAMETER_USER_ID, user.getId());
		return (List<Message>) query.getResultList();
	}

	@SuppressWarnings("unchecked")
	public List<Message> getSentMessages(User user) {
		Query query = em.createNamedQuery("findSentMessagesForUser");
		query.setParameter(PARAMETER_USER_ID, user.getId());
		return (List<Message>) query.getResultList();
	}

	public void setMessageRead(Message msg, boolean msgRead) {
		Message message = em.find(Message.class, msg.getId());
		if (msgRead) {
			message.setDateRead(new Date());
			message.setMessageRead(true);
		} else {
			message.setDateRead(null);
			message.setMessageRead(false);
		}
	}

}
