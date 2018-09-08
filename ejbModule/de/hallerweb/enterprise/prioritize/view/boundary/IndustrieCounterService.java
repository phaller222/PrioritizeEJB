package de.hallerweb.enterprise.prioritize.view.boundary;

import javax.ejb.EJB;
import javax.enterprise.context.RequestScoped;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.NotAuthorizedException;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import de.hallerweb.enterprise.prioritize.controller.nfc.counter.IndustrieCounterController;
import de.hallerweb.enterprise.prioritize.controller.nfc.counter.IndustrieCounterController.CounterType;
import de.hallerweb.enterprise.prioritize.controller.security.RestAccessController;
import de.hallerweb.enterprise.prioritize.controller.security.SessionController;
import de.hallerweb.enterprise.prioritize.model.nfc.counter.IndustrieCounter;

@RequestScoped
@Path("counters")
public class IndustrieCounterService {

	@EJB
	RestAccessController accessController;
	@EJB
	SessionController sessionController;
	@EJB
	IndustrieCounterController counterController;

	@GET
	@Path("/uuid/{uuid}")
	@Produces(MediaType.APPLICATION_JSON)
	/**
	 * @param uuid - UUID
	 * @param apiKey - API-Key
	 * @return IndustrieCounter
	 */
	public IndustrieCounter getIndustrieCounter(@PathParam(value = "uuid") String uuid, @QueryParam(value = "apiKey") String apiKey) {
		if (accessController.checkApiKey(apiKey) != null) {
			return counterController.getIndustrieCounter(uuid);
		} else {
			throw new NotAuthorizedException(Response.serverError());
		}
	}

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
		if (accessController.checkApiKey(apiKey) != null) {
			return counterController.createCounter(initialValue, CounterType.NFC, uuid, name, desc,
					sessionController.getUser());
			
		} else {
			throw new NotAuthorizedException(Response.serverError());
		}
	}

	@PUT
	@Path("/uuid/{uuid}/{action}")
	@Produces(MediaType.APPLICATION_JSON)
	/**
	 * 
	 * @param uuid - UUID
	 * @param action - can be one of increase, decrease or reset
	 * @param apiKey - API-Key
	 * @return IndustrieCounter
	 */
	public IndustrieCounter editCounter(@PathParam(value = "uuid") String uuid, @PathParam(value = "action") String action,
			@QueryParam(value = "apiKey") String apiKey) {
		if (accessController.checkApiKey(apiKey) != null) {
			IndustrieCounter c = counterController.getIndustrieCounter(uuid);
			if (action.equalsIgnoreCase("increase")) {
				counterController.incCounter(c);
			} else if (action.equalsIgnoreCase("decrease")) {
				counterController.decCounter(c);
			} else if (action.equalsIgnoreCase("reset")) {
				// Not implemented yet!
			}
			return c;
		} else {
			throw new NotAuthorizedException(Response.serverError());
		}
	}
}
