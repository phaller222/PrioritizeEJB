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
package de.hallerweb.enterprise.prioritize.view.boundary;

import java.util.ArrayList;
import java.util.List;

import javax.ejb.EJB;
import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.NotAuthorizedException;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import de.hallerweb.enterprise.prioritize.controller.inbox.MessageController;
import de.hallerweb.enterprise.prioritize.controller.security.RestAccessController;
import de.hallerweb.enterprise.prioritize.controller.security.SessionController;
import de.hallerweb.enterprise.prioritize.controller.security.UserRoleController;
import de.hallerweb.enterprise.prioritize.model.inbox.Message;
import de.hallerweb.enterprise.prioritize.model.security.User;

/**
 * 
 * <p>
 * Copyright: (c) 2015
 * </p>
 * <p>
 * Peter Haller
 * </p>
 * 
 * @author peter REST-Service to send and receive messages.
 */
@RequestScoped
@Path("v1/inbox")
public class InboxService {

	@EJB
	RestAccessController accessController;
	@EJB
	MessageController messagController;
	@EJB
	UserRoleController userRoleController;
	@Inject
	SessionController sessionController;

	public static final String LITERAL_MESSAGE_WITH_ID = "Message with ID ";
	
	
	/**
	 * Returns all received messages for this user
	 * @return JSON object with users in that department.
	 */
	@GET
	@Path("list/received")
	@Produces(MediaType.APPLICATION_JSON)
	public List<Message> getInboxMessages(@QueryParam(value = "apiKey") String apiKey, @QueryParam(value = "from") String fromUserId) {
		User user = accessController.checkApiKey(apiKey);
		if (user != null) {
			List<Message> messages = messagController.getReceivedMessages(user);
			if (fromUserId == null) {
				return messages;
			} else {
				List<Message> messagesReceivedFrom = new ArrayList<>();
				User userReceivedFrom = userRoleController.getUserById(Integer.parseInt(fromUserId), user);
				for (Message msg : messages) {
					if (msg.getFrom().getId() == userReceivedFrom.getId()) {
						messagesReceivedFrom.add(msg);
					}
				}
				return messagesReceivedFrom;
			}

		} else {
			throw new NotAuthorizedException(Response.serverError());
		}
	}

	/**
	 * Returns all sent messages for this user
	 * 
	 * @return JSON object with users in that department.
	 */
	@GET
	@Path("list/sent")
	@Produces(MediaType.APPLICATION_JSON)
	public List<Message> getSentMessages(@QueryParam(value = "apiKey") String apiKey, @QueryParam(value = "to") String toUserId) {
		User user = accessController.checkApiKey(apiKey);
		if (user != null) {
			List<Message> messages = messagController.getSentMessages(user);
			if (toUserId == null) {
				return messages;
			} else {
				List<Message> messagesSentTo = new ArrayList<>();
				User userSentTo = userRoleController.getUserById(Integer.parseInt(toUserId), user);
				for (Message msg : messages) {
					if (msg.getTo().getId() == userSentTo.getId()) {
						messagesSentTo.add(msg);
					}
				}
				return messagesSentTo;
			}
		} else {
			throw new NotAuthorizedException(Response.serverError());
		}
	}

	/**
	 * Return the {@link Message} object with the given id.
	 *
	 * @param id
	 *            - The id of the {@link Message}.
	 * @return {@link Message} - JSON Representation of the message.
	 */
	@GET
	@Path("id/{id}")
	@Produces(MediaType.TEXT_HTML)
	public String getMessageById(@PathParam(value = "id") String id, @QueryParam(value = "apiKey") String apiKey) {
		User user = accessController.checkApiKey(apiKey);
		if (user != null) {
			List<Message> messages = messagController.getReceivedMessages(user);
			if (messages != null) {
				for (Message msg : messages) {
					if (msg.getId() == Integer.parseInt(id)) {
						messagController.setMessageRead(msg, true);
						return msg.getContent();
					}
				}
				throw new NotFoundException(createNegativeResponse("User has no message with the given id!"));

			} else {
				throw new NotFoundException(createNegativeResponse("User has no message with the given id!"));
			}

		} else {
			throw new NotFoundException(createNegativeResponse("User not found or api key invalid!"));
		}

	}

	@POST
	@Path("new")
	@Produces(MediaType.APPLICATION_JSON)
	public Response newMessageByUsername(@QueryParam(value = "apiKey") String apiKey, @QueryParam(value = "username") String username,
			@QueryParam(value = "subject") String subject, @QueryParam(value = "message") String message) {
		User from = accessController.checkApiKey(apiKey);
		if (from != null) {
			User to = userRoleController.findUserByUsername(username, from);
			if (to != null) {
				messagController.createMessage(from, to, subject, message);
				return createPositiveResponse("Message " + subject + "has succcessfully been sent to User " + username + ".");
			} else {
				return createNegativeResponse("User with username " + username + " not found!");
			}
		} else {
			throw new NotAuthorizedException(Response.serverError());
		}
	}

	@POST
	@Path("new")
	@Produces(MediaType.APPLICATION_JSON)
	public Response newMessageById(@QueryParam(value = "apiKey") String apiKey, @QueryParam(value = "id") String id,
			@QueryParam(value = "subject") String subject, @QueryParam(value = "message") String message) {
		User from = accessController.checkApiKey(apiKey);
		if (from != null) {
			User to = userRoleController.findUserById(Integer.parseInt(id), from);
			if (to != null) {
				messagController.createMessage(from, to, subject, message);
				return createPositiveResponse("Message " + subject + "has succcessfully been sent to User " + to.getUsername() + ".");
			} else {
				return createNegativeResponse("User with ID " + id + " not found!");
			}
		} else {
			throw new NotAuthorizedException(Response.serverError());
		}
	}

	@DELETE
	@Path("remove")
	@Produces(MediaType.APPLICATION_JSON)
	public Response removeMessage(@QueryParam(value = "apiKey") String apiKey, @QueryParam(value = "id") String messageId) {
		User user = accessController.checkApiKey(apiKey);
		if (user != null) {
			List<Message> receivedMessages = messagController.getReceivedMessages(user);
			for (Message msg : receivedMessages) {
				if (msg.getId() == Integer.parseInt(messageId)) {
					messagController.deleteMessage(msg.getId());
					return createPositiveResponse(LITERAL_MESSAGE_WITH_ID + messageId + " has been removed.");
				}
			}
			List<Message> sentMessages = messagController.getSentMessages(user);
			for (Message msg : sentMessages) {
				if (msg.getId() == Integer.parseInt(messageId)) {
					messagController.deleteMessage(msg.getId());
					return createPositiveResponse(LITERAL_MESSAGE_WITH_ID + messageId + " has been removed.");
				}
			}
			return createNegativeResponse(LITERAL_MESSAGE_WITH_ID + messageId + " not found. Nothing removed!");
		} else {
			throw new NotAuthorizedException(Response.serverError());
		}
	}

	private Response createPositiveResponse(String responseText) {
		return Response.status(200).entity("{\"response\" : \"" + responseText + "\"}").build();
	}

	private Response createNegativeResponse(String responseText) {
		return Response.status(500).entity("{\"response\" : \"" + responseText + "\"}").build();
	}
}
