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

import de.hallerweb.enterprise.prioritize.controller.CompanyController;
import de.hallerweb.enterprise.prioritize.controller.calendar.CalendarController;
import de.hallerweb.enterprise.prioritize.controller.resource.ResourceReservationController;
import de.hallerweb.enterprise.prioritize.controller.search.SearchController;
import de.hallerweb.enterprise.prioritize.controller.security.AuthorizationController;
import de.hallerweb.enterprise.prioritize.controller.security.RestAccessController;
import de.hallerweb.enterprise.prioritize.controller.security.SessionController;
import de.hallerweb.enterprise.prioritize.controller.security.UserRoleController;
import de.hallerweb.enterprise.prioritize.model.Department;
import de.hallerweb.enterprise.prioritize.model.calendar.TimeSpan;
import de.hallerweb.enterprise.prioritize.model.resource.ResourceReservation;
import de.hallerweb.enterprise.prioritize.model.security.User;

import jakarta.ejb.EJB;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
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
@Path("v1/calendar")
public class CalendarService {

    @EJB
    RestAccessController accessController;

    @EJB
    ResourceReservationController resourceReservationController;

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
     * @return JSON object with {@link TimeSpan} objects for that department.
     * @api {get} /calendar/reservations getTimeSpansForReservations
     * @apiName getTimeSpansForReservations
     * @apiGroup /calendar
     * @apiDescription Searches for all resource reservations to resources (devices) within a department.
     * The department is given by the departmentToken parameter. Parameters "from" and "to" indicate the
     * the timespan to search.
     * @apiParam {String} departmentToken Department token to use.
     * @apiParam {String} apiKey The API-Key of the user accessing the service.
     * @apiParam {long} from Java timestamp to indicate the start date from which to search for resevations.
     * @apiParam {long} to Java timestamp to indicate the end date to search for resevations.
     * @apiSuccess {TimeSpan} timespan JSON Objects with all timespans currently registered for reservations.
     * @apiSuccessExample Success-Response:
     * HTTP/1.1 200 OK
     * [
     * {
     * "id" : 76,
     * "title" : "aaaa",
     * "description" : "default:aaaa[admin]",
     * "dateFrom" : 1479164400000,
     * "dateUntil" : 1485817200000,
     * "type" : "RESOURCE_RESERVATION",
     * "department" : ...list of departments...
     * }
     * ]
     * @apiError NotAuthorized  DepartmentToken or APIKey incorrect.
     */
    @GET
    @Path("reservations/")
    @Produces(MediaType.APPLICATION_JSON)
    public List<TimeSpan> getTimeSpansForReservations(@QueryParam(value = "departmentToken") String departmentToken,
                                                      @QueryParam(value = "apiKey") String apiKey, @QueryParam(value = "from") String from, @QueryParam(value = "to") String to) {
        User user = accessController.checkApiKey(apiKey);
        if (user != null) {
            Department dept = companyController.getDepartmentByToken(departmentToken, user);
            if (dept != null) {
                List<ResourceReservation> reservations = resourceReservationController.getResourceReservationsForDepartment(dept.getId());
                List<TimeSpan> entries = findIntersectingTimeSpansInReservations(from, to, user, reservations);

                if (entries.isEmpty()) {
                    throw new NotFoundException(createNegativeResponse("No entries found for department " + dept.getName()
                            + " and given timespan or no permission to read resource reservations form this department!"));
                } else {
                    return entries;
                }
            } else {
                throw new NotFoundException(createNegativeResponse("Department not found or department token invalid!"));
            }
        } else {
            throw new NotAuthorizedException(Response.serverError());
        }
    }

    private List<TimeSpan> findIntersectingTimeSpansInReservations(String timeSpanFrom, String timeSpanTo, User sessionUser,
                                                                   List<ResourceReservation> reservations) {
        List<TimeSpan> entries = new ArrayList<>();
        for (ResourceReservation res : reservations) {
            if (authController.canRead(res.getResource(), sessionUser)) {
                TimeSpan reservationTimeSpan = res.getTimeSpan();
                TimeSpan searchSpan = new TimeSpan();
                try {
                    searchSpan.setDateFrom(new Date(Long.parseLong(timeSpanFrom)));
                    searchSpan.setDateUntil(new Date(Long.parseLong(timeSpanTo)));
                } catch (Exception ex) {
                    throw new NotFoundException(createNegativeResponse("Missing or invalid Date parameters 'from' or 'to'!"));
                }

                // Add to search result if TimeSpan objects intersect
                if (reservationTimeSpan.intersects(searchSpan)) {
                    entries.add(reservationTimeSpan);
                }
            } else {
                break;
            }
        }
        return entries;
    }

    /**
     * @return JSON object with {@link TimeSpan} objects for this user.
     * @api {get} /calendar/self getTimeSpansForUser
     * @apiName getTimeSpansForUser
     * @apiGroup /calendar
     * @apiDescription Searches for all Timespan entries for the user with the given apiKey. This includes resource reservations
     * initiated by this user, illness and vacation entries.
     * @apiParam {String} apiKey The API-Key of the user accessing the service.
     * @apiSuccess {TimeSpan} timespan JSON Objects with all timespans currently registered for the user.
     * @apiSuccessExample Success-Response:
     * HTTP/1.1 200 OK
     * [
     * {
     * "id" : 76,
     * "title" : "aaaa",
     * "description" : "default:aaaa[admin]",
     * "dateFrom" : 1479164400000,
     * "dateUntil" : 1485817200000,
     * "type" : "RESOURCE_RESERVATION",
     * "department" : ...list of departments...
     * }
     * ]
     * @apiError NotAuthorized  DepartmentToken or APIKey incorrect.
     */
    @GET
    @Path("self/")
    @Produces(MediaType.APPLICATION_JSON)
    public List<TimeSpan> getTimeSpansForUser(@QueryParam(value = "apiKey") String apiKey, @QueryParam(value = "from") String from,
                                              @QueryParam(value = "to") String to) {
        User user = accessController.checkApiKey(apiKey);
        if (user == null) {
            throw new NotAuthorizedException(Response.serverError());
        } else {
            List<TimeSpan> entries = calendarController.getTimeSpansForUser(user);
            if (entries != null) {
                return entries;
            } else {
                throw new NotFoundException(createNegativeResponse("No entries found!"));
            }
        }
    }

    private Response createNegativeResponse(String responseText) {
        return Response.status(404).entity("{\"response\" : \"" + responseText + "\"}").build();
    }
}
