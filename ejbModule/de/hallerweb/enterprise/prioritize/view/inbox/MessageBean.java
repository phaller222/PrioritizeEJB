package de.hallerweb.enterprise.prioritize.view.inbox;

import java.io.Serializable;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.enterprise.context.SessionScoped;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;
import javax.inject.Named;

import de.hallerweb.enterprise.prioritize.controller.inbox.MessageController;
import de.hallerweb.enterprise.prioritize.controller.security.AuthorizationController;
import de.hallerweb.enterprise.prioritize.controller.security.SessionController;
import de.hallerweb.enterprise.prioritize.controller.security.UserRoleController;
import de.hallerweb.enterprise.prioritize.model.inbox.Message;
import de.hallerweb.enterprise.prioritize.model.security.User;

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

	@Inject
	SessionController sessionController;
	@EJB
	MessageController controller;
	@EJB
	UserRoleController userRoleController;
	@EJB
	AuthorizationController authController;

	List<Message> messages; // List of messages
	String message; // Stores the newly composed Message
	String subject; // Stores the newly composed Message subject
	String readMessageSubject; // Subject of currently viewed message
	String readMessageMessage; // Message of currently viewed message
	String to; // Stores the User to receive the message
	List<User> recipientList; // List of Users in the system.
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
		this.readMessageMessage = readMessage.getMessage();
		return "readmessage";
	}

	@Named
	public User getCurrentUser() {
		return sessionController.getUser();
	}

	@PostConstruct
	public void init() {
		to = new String();
	}

	@Produces
	@Named
	public List<User> getRecipiantList() {
		return userRoleController.getAllUsers(sessionController.getUser());
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
		return "messages";
	}

	@Named
	public String sendMessage() {
		controller.createMessage(sessionController.getUser(), userRoleController.findUserById(Integer.parseInt(this.to)), this.subject,
				this.message);
		return "messages";
	}

	@Named
	public String overview() {
		return "messages";
	}

	@Named
	public void setMessageRead() {
		Message msg = controller.findMessageById(Integer.parseInt(readMessageId));
		controller.setMessageRead(msg, true);
	}
	
	@Named
	public List<Message> getUnreadMessages() {
		 return (List<Message>) controller.getUnreadMessages(sessionController.getUser());
	}
	
}
