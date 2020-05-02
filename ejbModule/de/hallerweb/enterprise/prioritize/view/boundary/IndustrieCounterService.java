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

import de.hallerweb.enterprise.prioritize.controller.nfc.counter.IndustrieCounterController;
import de.hallerweb.enterprise.prioritize.controller.nfc.counter.IndustrieCounterController.CounterType;
import de.hallerweb.enterprise.prioritize.controller.security.RestAccessController;
import de.hallerweb.enterprise.prioritize.controller.security.SessionController;
import de.hallerweb.enterprise.prioritize.model.nfc.counter.IndustrieCounter;
import de.hallerweb.enterprise.prioritize.model.security.User;

import javax.ejb.EJB;
import javax.enterprise.context.RequestScoped;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@RequestScoped
@Path("v1/counters")
public class IndustrieCounterService {

	@EJB
	RestAccessController accessController;
	@EJB
	SessionController sessionController;
	@EJB
	IndustrieCounterController counterController;


	/**
	 * Gets the data for a counter
	 *
	 * @api {get} /uuid/{uuid} getIndustrieCounter
	 * @apiName getIndustrieCounter
	 * @apiGroup /counters
	 * @apiDescription deletes the message with the given id.
	 * @apiParam {String} apiKey The API-Key of the user accessing the service.
	 * @apiParam {String} uuid  the uuid of the counter to be retrieved.
	 * @apiSuccess {String} JSON object representing a counter.
	 * @apiSuccessExample Success-Response:
	 *     HTTP/1.1 200 OK
	 *{
	 	*"id": 64,
		*"counter": {
			*"id": 63,
			*"nfcUnit": {
				*"id": 62,
				*"uuid": "49ee74df-9c9f-4410-8177-4b33095e939d",
				*"name": "Counter",
				*"description": "NFC counter",
				*"payload": "1",
				*"payloadSize": 1,
				*"lastConnectedTime": 1588453253120,
				*"latitude": null,
				*"longitude": null,
				*"sequenceNumber": 0,
				*"unitType": "COUNTER",
				*"lastConnectedDevice": null,
				*"wrappedResource": null
			*},
		*"uuid": "49ee74df-9c9f-4410-8177-4b33095e939d",
		*"value": 1
	*	},
	*"name": "test",
	*"description": "testcounter",
	*"department": null
	*}
	 *
	 * @apiError NotAuthorized  APIKey incorrect.
	 *
	 *
	 */
	@GET
	@Path("/uuid/{uuid}")
	@Produces(MediaType.APPLICATION_JSON)
	public IndustrieCounter getIndustrieCounter(@PathParam(value = "uuid") String uuid, @QueryParam(value = "apiKey") String apiKey) {
		if (accessController.checkApiKey(apiKey) != null) {
			return counterController.getIndustrieCounter(uuid);
		} else {
			throw new NotAuthorizedException(Response.serverError());
		}
	}


	/**
	 * Creates a counter
	 *
	 * @api {post} /create createCounter
	 * @apiName createCounter
	 * @apiGroup /counters
	 * @apiDescription creates a counter
	 * @apiParam {String} apiKey The API-Key of the user accessing the service.
	 * @apiParam {String} uuid  the uuid of the counter to be created.
	 * @apiParam {String} name  the name of the counter to be created.
	 * @apiParam {String} description the description of the counter to be created.
	 * @apiParam {String} initialValue  the initial value (long) of the counter to be created.
	 * @apiSuccess {{@link IndustrieCounter} JSON representation of the counter just created.
	 * @apiSuccessExample Success-Response:
	 *     HTTP/1.1 200 OK
	 *{
	 *"id": 64,
	 *"counter": {
	 *"id": 63,
	 *"nfcUnit": {
	 *"id": 62,
	 *"uuid": "49ee74df-9c9f-4410-8177-4b33095e939d",
	 *"name": "Counter",
	 *"description": "NFC counter",
	 *"payload": "1",
	 *"payloadSize": 1,
	 *"lastConnectedTime": 1588453253120,
	 *"latitude": null,
	 *"longitude": null,
	 *"sequenceNumber": 0,
	 *"unitType": "COUNTER",
	 *"lastConnectedDevice": null,
	 *"wrappedResource": null
	 *},
	 *"uuid": "49ee74df-9c9f-4410-8177-4b33095e939d",
	 *"value": 1
	 *	},
	 *"name": "test",
	 *"description": "testcounter",
	 *"department": null
	 *}
	 *
	 * @apiError NotAuthorized  APIKey incorrect.
	 *
	 *
	 */
	@POST
	@Path("/create")
	@Produces(MediaType.APPLICATION_JSON)
	/**
	 * 
	 * @param apiKey - API-Key
	 * @return IndustrieCounter
	 */
	public IndustrieCounter createCounter(@QueryParam(value = "apiKey") String apiKey, @FormParam(value = "uuid") String uuid,
			@FormParam(value = "name") String name, @FormParam(value = "description") String desc,
			@FormParam(value = "initialValue") long initialValue) {
		User sessionUser = accessController.checkApiKey(apiKey);
		if (sessionUser != null) {
			return counterController.createCounter(initialValue, CounterType.NFC, uuid, name, desc,
					sessionUser);
			
		} else {
			throw new NotAuthorizedException(Response.serverError());
		}
	}


	/**
	 * increase, decrease or reset a counter
	 *
	 * @api {put} /uuid/{uuid} editCounter
	 * @apiName editCounter
	 * @apiGroup /counters
	 * @apiDescription performs an increase, decrease or reset on the given counter
	 * @apiParam {String} apiKey The API-Key of the user accessing the service.
	 * @apiParam {String} action  - on of increase, decrease or reset.
	 * @apiParam {String} uuid  the uuid of the counter to be retrieved.
	 * @apiSuccess {{@link IndustrieCounter} JSON representation of the counter just edited.
	 * @apiSuccessExample Success-Response:
	 *     HTTP/1.1 200 OK
	 *{
	 *"id": 64,
	 *"counter": {
	 *"id": 63,
	 *"nfcUnit": {
	 *"id": 62,
	 *"uuid": "49ee74df-9c9f-4410-8177-4b33095e939d",
	 *"name": "Counter",
	 *"description": "NFC counter",
	 *"payload": "2",
	 *"payloadSize": 1,
	 *"lastConnectedTime": 1588453253120,
	 *"latitude": null,
	 *"longitude": null,
	 *"sequenceNumber": 1,
	 *"unitType": "COUNTER",
	 *"lastConnectedDevice": null,
	 *"wrappedResource": null
	 *},
	 *"uuid": "49ee74df-9c9f-4410-8177-4b33095e939d",
	 *"value": 2
	 *	},
	 *"name": "test",
	 *"description": "testcounter",
	 *"department": null
	 *}
	 *
	 * @apiError NotAuthorized  APIKey incorrect.
	 *
	 *
	 */
	@PUT
	@Path("/uuid/{uuid}/{action}")
	@Produces(MediaType.APPLICATION_JSON)
	public IndustrieCounter editCounter(@PathParam(value = "uuid") String uuid, @PathParam(value = "action") String action,
			@QueryParam(value = "apiKey") String apiKey) {
		if (accessController.checkApiKey(apiKey) != null) {
			IndustrieCounter c = counterController.getIndustrieCounter(uuid);
			if (action.equalsIgnoreCase("increase")) {
				counterController.incCounter(c);
			} else if (action.equalsIgnoreCase("decrease")) {
				counterController.decCounter(c);
			} else if (action.equalsIgnoreCase("reset")) {
				counterController.resetCounter(c);
			}
			return c;
		} else {
			throw new NotAuthorizedException(Response.serverError());
		}
	}
}
