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
package de.hallerweb.enterprise.prioritize.view.inbox;

import de.hallerweb.enterprise.prioritize.controller.inbox.MessageController;
import de.hallerweb.enterprise.prioritize.controller.security.SessionController;
import de.hallerweb.enterprise.prioritize.controller.security.UserRoleController;
import de.hallerweb.enterprise.prioritize.model.inbox.Message;
import de.hallerweb.enterprise.prioritize.model.security.User;
import org.jboss.resteasy.logging.Logger;

import jakarta.annotation.PostConstruct;
import jakarta.ejb.EJB;
import jakarta.enterprise.context.SessionScoped;
import jakarta.enterprise.inject.Produces;
import jakarta.faces.context.ExternalContext;
import jakarta.faces.context.FacesContext;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * MessageBean - JSF Backing-Bean to store information about Messages.
 * <p>
 * Copyright: (c) 2015
 * </p>
 * <p>
 * Peter Haller
 * </p>
 * 
 * @author peter
 */
@Named
@SessionScoped
public class MessageBean implements Serializable {

	private static final String NAVIGATION_MESSAGES = "messages";
	
	@Inject
	SessionController sessionController;
	@EJB
	transient MessageController controller;
	@EJB
	transient UserRoleController userRoleController;

	transient List<Message> messages; // List of messages
	String message; // Stores the newly composed Message
	String subject; // Stores the newly composed Message subject
	String readMessageSubject; // Subject of currently viewed message
	String readMessageMessage; // Message of currently viewed message
	String to; // Stores the User to receive the message
	transient List<User> recipientList; // List of Users in the system.
	String readMessageId; // ID of currently viewed message

	@Named
	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	@Named
	public String getSubject() {
		return subject;
	}

	public void setSubject(String subject) {
		this.subject = subject;
	}

	@Named
	public List<Message> getMessages() {
		return controller.getReceivedMessages(sessionController.getUser());
	}

	@Named
	public List<Message> getSentMessages() {
		return controller.getSentMessages(sessionController.getUser());
	}

	public String getReadMessageSubject() {
		return readMessageSubject;
	}

	@Named
	public String getReadMessageMessage() {
		return readMessageMessage;
	}

	@Named
	public String readMessage(String readMessageId) {
		this.readMessageId = readMessageId;
		Message readMessage = controller.findMessageById(Integer.parseInt(readMessageId));
		this.readMessageSubject = readMessage.getSubject();
		this.readMessageMessage = readMessage.getContent();
		return "readmessage";
	}

	@Named
	public User getCurrentUser() {
		return sessionController.getUser();
	}

	@PostConstruct
	public void init() {
		to = "";
	}

	@Produces
	@Named
	public List<User> getRecipiantList() {
		return userRoleController.getAllUsers(sessionController.getUser());
	}

	public List<String> completeRecipiantList(String query) {
		List<String> users = userRoleController.getAllUserNames(sessionController.getUser());
		List<String> result = new ArrayList<>();
		for (String username : users) {
			if (username.startsWith(query)) {
				result.add(username);
			}
		}
		return result;

	}

	@Named
	public String getTo() {
		return to;
	}

	@Named
	public void setTo(String user) {
		this.to = user;
	}

	@Named
	public String delete(Message msg) {
		controller.deleteMessage(msg.getId());
		init();
		return NAVIGATION_MESSAGES;
	}

	@Named
	public String sendMessage() {
		controller.createMessage(sessionController.getUser(), userRoleController.findUserByUsername(this.to,sessionController.getUser()).getUsername(),
				this.subject, this.message);
		return NAVIGATION_MESSAGES;
	}

	@Named
	public String overview() {
		return NAVIGATION_MESSAGES;
	}

	@Named
	public String inbox() {
		return "inbox";
	}

	@Named
	public void gotoInbox() {
		ExternalContext context = FacesContext.getCurrentInstance().getExternalContext();
		try {
			context.redirect(context.getApplicationContextPath() + "/client/messages/inbox.xhtml");
		} catch (IOException e) {
			Logger.getLogger(getClass()).error(e.getMessage());
		}
	}

	@Named
	public void setMessageRead() {
		Message msg = controller.findMessageById(Integer.parseInt(readMessageId));
		controller.setMessageRead(msg, true);
	}

	@Named
	public List<Message> getUnreadMessages() {
		return controller.getUnreadMessages(sessionController.getUser());
	}

}
