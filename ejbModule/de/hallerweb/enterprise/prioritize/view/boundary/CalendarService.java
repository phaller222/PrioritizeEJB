/**
 * 
 */

package de.hallerweb.enterprise.prioritize.view.boundary;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.ejb.EJB;
import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.NotAuthorizedException;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import de.hallerweb.enterprise.prioritize.controller.CompanyController;
import de.hallerweb.enterprise.prioritize.controller.calendar.CalendarController;
import de.hallerweb.enterprise.prioritize.controller.resource.ResourceController;
import de.hallerweb.enterprise.prioritize.controller.search.SearchController;
import de.hallerweb.enterprise.prioritize.controller.security.AuthorizationController;
import de.hallerweb.enterprise.prioritize.controller.security.RestAccessController;
import de.hallerweb.enterprise.prioritize.controller.security.SessionController;
import de.hallerweb.enterprise.prioritize.controller.security.UserRoleController;
import de.hallerweb.enterprise.prioritize.model.Department;
import de.hallerweb.enterprise.prioritize.model.calendar.TimeSpan;
import de.hallerweb.enterprise.prioritize.model.resource.ResourceReservation;
import de.hallerweb.enterprise.prioritize.model.security.User;
import jdk.internal.org.objectweb.asm.util.CheckAnnotationAdapter;

/**
 * 
 * <p>
 * Copyright: (c) 2015
 * </p>
 * <p>
 * Peter Haller
 * </p>
 * 
 * @author peter REST-Service to create, update and delete calendar related objects.
 */
@RequestScoped
@Path("calendar")
public class CalendarService {

	@EJB
	RestAccessController accessController;

	@EJB
	ResourceController resourceController;

	@EJB
	CompanyController companyController;

	@EJB
	CalendarController calendarController;

	@EJB
	UserRoleController userRoleController;

	@EJB
	SearchController searchController;

	@Inject
	SessionController sessionController;

	@EJB
	AuthorizationController authController;

	/**
	 * Returns {@link TimeSpan} objects with {@link ResourceReservation} objects for the given department. It must be narrowed down by
	 * providing from and to parameters as Timestamp.
	 *
	 * @param departmentToken - The department token.
	 * @return JSON object with {@link TimeSpan} objects for that department.
	 */
	@GET
	@Path("reservations/")
	@Produces(MediaType.APPLICATION_JSON)
	public List<TimeSpan> getTimeSpansForReservations(@QueryParam(value = "departmentToken") String departmentToken,
			@QueryParam(value = "apiKey") String apiKey, @QueryParam(value = "from") String from,
			@QueryParam(value = "to") String to) {
		User user = accessController.checkApiKey(apiKey);
		if (user != null) {
			Department dept = companyController.getDepartmentByToken(departmentToken);
			List<TimeSpan> entries = new ArrayList<TimeSpan>();
			if (dept != null) {
				List<ResourceReservation> reservations = resourceController
						.getResourceReservationsForDepartment(dept.getId());
				for (ResourceReservation res : reservations) {
					if (authController.canRead(res.getResource(), user)) {
						TimeSpan span = res.getTimeSpan();
						TimeSpan searchSpan = new TimeSpan();
						searchSpan.setDateFrom(new Date(Long.parseLong(from)));
						searchSpan.setDateUntil(new Date(Long.parseLong(to)));

						// Add to search result if TimeSpan objects intersect
						if (span.intersects(searchSpan)) {
							entries.add(res.getTimeSpan());
						}
					} else {
						break;
					}
				}

				if (!entries.isEmpty()) {
					return entries;
				} else {
					throw new NotFoundException(createNegativeResponse("No entries found for department "
							+ dept.getName()
							+ " and given timespan or no permission to read resource reservations form this department!"));
				}

			} else {
				throw new NotFoundException(
						createNegativeResponse("Department not found or department token invalid!"));
			}
		} else
			throw new NotAuthorizedException(Response.serverError());
	}

	/**
	 * Returns all {@link TimeSpan} objects for the user with the given apiKey. It must be narrowed down by providing from and to parameters
	 * as TimeStamp.
	 *
	 * @return JSON object with {@link TimeSpan} objects for that department.
	 */
	@GET
	@Path("self/")
	@Produces(MediaType.APPLICATION_JSON)
	public List<TimeSpan> getTimeSpansForUser(@QueryParam(value = "apiKey") String apiKey,
			@QueryParam(value = "from") String from, @QueryParam(value = "to") String to) {
		User user = accessController.checkApiKey(apiKey);
		if (user != null) {
			List<TimeSpan> entries = calendarController.getTimeSpansForUser(user);
			if (entries != null) {
				return entries;
			} else {
				throw new NotFoundException(createNegativeResponse("No entries found!"));
			}
		} else
			throw new NotAuthorizedException(Response.serverError());
	}

	private Response createPositiveResponse(String responseText) {
		return Response.status(200).entity("{\"response\" : \"" + responseText + "\"}").build();
	}

	private Response createNegativeResponse(String responseText) {
		return Response.status(405).entity("{\"response\" : \"" + responseText + "\"}").build();
	}
}
