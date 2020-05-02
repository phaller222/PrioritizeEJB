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

import de.hallerweb.enterprise.prioritize.controller.inbox.MessageController;
import de.hallerweb.enterprise.prioritize.controller.security.RestAccessController;
import de.hallerweb.enterprise.prioritize.controller.security.SessionController;
import de.hallerweb.enterprise.prioritize.controller.security.UserRoleController;
import de.hallerweb.enterprise.prioritize.model.inbox.Message;
import de.hallerweb.enterprise.prioritize.model.security.User;

import javax.ejb.EJB;
import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.List;

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
	 * Returns all the messages received for the current user or the user specified.
	 *
	 * @api {get} /list/received getInboxMessages
	 * @apiName getInboxMessages
	 * @apiGroup /inbox
	 * @apiDescription Returns all the messages received for the current user or the user specified by param from.
	 * @apiParam {String} apiKey The API-Key of the user accessing the service.
	 * @apiParam {String} from The user which inbox to read - if specified
	 * @apiSuccess {List} messages of the user.
	 * @apiSuccessExample Success-Response:
	 *     HTTP/1.1 200 OK
	*[
		*{
			*"id": 48,
			*"dateReceived": 1588448673369,
			*"dateRead": 1588448711115,
			*"messageRead": true,
			*"subject": "test11",
			*"from": {
				*"id": 44,
				*"name": "admin",
				*"username": "admin",
				*"address": null
			*},
			*"to": {
				*"id": 44,
				*"name": "admin",
				*"username": "admin",
				*"address": null
			*}
		*}
	 *]
	 *
	 * @apiError NotAuthorized  APIKey incorrect.
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
				User userReceivedFrom = userRoleController.findUserByUsername(fromUserId, user);
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
	 * Returns all the messages sent by the current user or the user specified.
	 *
	 * @api {get} /list/sent getSentMessages
	 * @apiName getSentMessages
	 * @apiGroup /inbox
	 * @apiDescription Returns all the messages sent by the current user or the user specified by param from.
	 * @apiParam {String} apiKey The API-Key of the user accessing the service.
	 * @apiParam {String} from The user which outbox to read - if specified
	 * @apiSuccess {List} messages sent by the user.
	 * @apiSuccessExample Success-Response:
	 *     HTTP/1.1 200 OK
	 *[
	 	*{
	 		*"id": 48,
	 		*"dateReceived": 1588448673369,
	 		*"dateRead": 1588448711115,
			 *"messageRead": true,
			 *"subject": "test11",
			 *"from": {
				 *"id": 44,
				 *"name": "admin",
				 *"username": "admin",
	 			*"address": null
			 *},
	 		*"to": {
				 *"id": 44,
				 *"name": "admin",
				 *"username": "admin",
				 *"address": null
			 *}
		 *}
	 *]
	 *
	 * @apiError NotAuthorized  APIKey incorrect.
	 */
	@GET
	@Path("list/sent")
	@Produces(MediaType.APPLICATION_JSON)
	public List<Message> getSentMessages(@QueryParam(value = "apiKey") String apiKey, @QueryParam(value = "from") String fromUserId) {
		User user = accessController.checkApiKey(apiKey);
		List<Message> messages = new ArrayList<>();
		if (user != null) {
			if (fromUserId ==null) {
				return messagController.getSentMessages(user);
			}
			else {
				User userSentTo = userRoleController.findUserByUsername(fromUserId, user);
				return messagController.getSentMessages(userSentTo);
			}
		} else {
			throw new NotAuthorizedException(Response.serverError());
		}
	}

	/**
	 * Returns the message with the given id.
	 *
	 * @api {get} /id/{id} getMessageById
	 * @apiName getMessageById
	 * @apiGroup /inbox
	 * @apiDescription Returns the message with the given id.
	 * @apiParam {String} apiKey The API-Key of the user accessing the service.
	 * @apiParam {int} id The id of the message content to read.
	 * @apiSuccess {String} message content
	 * @apiSuccessExample Success-Response:
	 *     HTTP/1.1 200 OK
	 *  Test message OK.
	 *
	 * @apiError NotAuthorized  APIKey incorrect.
	 *
	 *
	 */
	@GET
	@Path("id/{id}")
	@Produces(MediaType.TEXT_HTML)
	public Response getMessageById(@PathParam(value = "id") String id, @QueryParam(value = "apiKey") String apiKey) {
		User user = accessController.checkApiKey(apiKey);
		if (user != null) {
			List<Message> messages = messagController.getReceivedMessages(user);
			if (messages != null) {
				for (Message msg : messages) {
					if (msg.getId() == Integer.parseInt(id)) {
						messagController.setMessageRead(msg, true);
						return Response.status(200).entity(msg.getContent()).build();
					}
				}
				return Response.status(404).entity("{\"response\" : \"" + "\"User has no message with the given id!\"" + "\"}").build();

			} else {
				return Response.status(404).entity("{\"response\" : \"" + "\"User has no message with the given id!\"" + "\"}").build();
			}

		} else {
			return Response.status(404).entity("{\"response\" : \"" + "\"User not found or invalid api key!\"" + "\"}").build();

		}

	}


	/**
	 * Sends a new message to the inbox of a user.
	 *
	 * @api {post} /new newMessage
	 * @apiName newMessage
	 * @apiGroup /inbox
	 * @apiDescription sends a new message to an inbox.
	 * @apiParam {String} apiKey The API-Key of the user accessing the service.
	 * @apiParam {String} username the recipiants username.
	 * @apiParam {String} subject the subject of the message.
	 * @apiParam {String} message the message to send.
	 * @apiSuccess {String} success message.
	 * @apiSuccessExample Success-Response:
	 *     HTTP/1.1 200 OK
	 *{
	 * "response": "Message has succcessfully been sent to User admin."
	 *}
	 *
	 * @apiError NotAuthorized  APIKey incorrect.
	 *
	 *
	 */
	@POST
	@Path("new")
	@Produces(MediaType.APPLICATION_JSON)
	public Response newMessage(@QueryParam(value = "apiKey") String apiKey, @QueryParam(value = "username") String username,
			@QueryParam(value = "subject") String subject, @QueryParam(value = "message") String message) {
		User from = accessController.checkApiKey(apiKey);
		if (from != null) {
			if (username != null) {
				messagController.createMessage(from, username, subject, message);
				return createPositiveResponse("Message " + subject + "has succcessfully been sent to User " + username + ".");
			} else {
				return createNegativeResponse("User with username " + username + " not found!");
			}
		} else {
			throw new NotAuthorizedException(Response.serverError());
		}
	}


	/**
	 * Deletes the message with the given id.
	 *
	 * @api {delete} /remove removeMessage
	 * @apiName removeMessage
	 * @apiGroup /inbox
	 * @apiDescription deletes the message with the given id.
	 * @apiParam {String} apiKey The API-Key of the user accessing the service.
	 * @apiParam {Integer} id the id of the message to be deleted.
	 * @apiSuccess {String} success message.
	 * @apiSuccessExample Success-Response:
	 *     HTTP/1.1 200 OK
	 *{
	 * "response": "Message has succcessfully been removed."
	 *}
	 *
	 * @apiError NotAuthorized  APIKey incorrect.
	 */
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
