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

    private static final String TIMESPAN_ILLNESS = "ILLNESS";
    private static final String TIMESPAN_VACATION = "VACATION";
    private static final String TIMESPAN_RESOURCE_RESERVATION = "RESOURCE_RESERVATION";
    private static final String TIMESPAN_ALL = "ALL";


    /**
     * Returns {@link TimeSpan} objects with {@link ResourceReservation} objects for the given Resource. It must be narrowed down by
     * providing from and to parameters as Timestamp.
     *
     * @return JSON object with {@link TimeSpan} objects for that department.
     * @api {get} /calendar/resource/reservations getTimeSpansForResourceReservations
     * @apiName getTimeSpansForResourceReservations
     * @apiGroup /calendar
     * @apiDescription Searches for all resource reservations for the specific resource (devices).
     * Parameters "from" and "to" indicate the timespan to search. If from and to are omitted all entries for the resource are returned.
     * @apiParam {Long} resourceId The ID of the Resource to look for reservations.
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
     * ...
     * }
     * ]
     * @apiError NotAuthorized  APIKey incorrect.
     */
    @GET
    @Path("resource/reservations/{resourceID}")
    @Produces(MediaType.APPLICATION_JSON)
    public List<TimeSpan> getTimeSpansForResourceReservations(@QueryParam(value = "resourceId") int resourceId,
                                                              @QueryParam(value = "apiKey") String apiKey, @QueryParam(value = "from") String from,
                                                              @QueryParam(value = "to") String to) {
        User user = accessController.checkApiKey(apiKey);
        if (user != null) {
            List<ResourceReservation> reservations = resourceReservationController.getResourceReservationsForResource(resourceId);
            List<TimeSpan> entries = findIntersectingTimeSpansInReservations(from, to, user, reservations);

            if (entries.isEmpty()) {
                throw new NotFoundException(createNegativeResponse("No entries found for resource " + resourceId
                    + " and given timespan or no permission to read resource reservations form this department!"));
            } else {
                return entries;
            }

        } else {
            throw new NotAuthorizedException(Response.serverError());
        }
    }


    /**
     * /**
     *
     * @return JSON object with {@link TimeSpan} objects for this user.
     * @api {get} /calendar/user/self getTimeSpansForCurrentUser
     * @apiName getTimeSpansForCurrentUser
     * @apiGroup /calendar
     * @apiDescription Searches for all Timespan entries for the user with the given apiKey. This includes resource reservations
     * initiated by this user, illness and vacation entries.
     * @apiParam {String} apiKey The API-Key of the user accessing the service.
     * @apiParam {long} from Java timestamp to indicate the start date from which to search for entries.
     * @apiParam {long} to Java timestamp to indicate the end date to search for entries.
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
     * "type" : "...",
     * "department" : ...list of departments...
     * }
     * ]
     * @apiError NotAuthorized  DepartmentToken or APIKey incorrect.
     */
    @GET
    @Path("user/self/")
    @Produces(MediaType.APPLICATION_JSON)
    public List<TimeSpan> getTimeSpansForCurrentUser(@QueryParam(value = "apiKey") String apiKey, @QueryParam(value = "from") String from,
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


    /**
     * Returns all {@link TimeSpan} objects for the given {@link User} within the given date range. This search searches for
     * illness entries only. Search must be narrowed down by
     * providing from and to parameters as Timestamp.
     *
     * @return JSON object with {@link TimeSpan} objects for that user.
     * @api {get} /calendar/user/illness/{username} getIllnessEntriesForUser
     * @apiName getIllnessEntriesForUser
     * @apiGroup /calendar
     * @apiDescription Searches for all illness entries of the given user.
     * The department is given by the departmentToken parameter. Parameters "from" and "to" indicate
     * the timespan to search.
     * @apiParam {String} username     The user to use for the search.
     * @apiParam {String}   apiKey The API-Key of the user accessing the service.
     * @apiParam {long} from    Java timestamp to indicate the start date from which to search for illness entries.
     * @apiParam {long} to Java timestamp to indicate the end date to search for illness entries.
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
     * "type" : "ILLNESS",
     * "department" : ...list of departments...
     * }
     * ]
     * @apiError NotAuthorized  DepartmentToken or APIKey incorrect.
     */
    @GET
    @Path("user/illness/{username}")
    @Produces(MediaType.APPLICATION_JSON)
    public List<TimeSpan> getIllnessEntriesForUser(@PathParam(value = "username") String username,
                                                   @QueryParam(value = "apiKey") String apiKey, @QueryParam(value = "from") String from,
                                                   @QueryParam(value = "to") String to) {
        User user = accessController.checkApiKey(apiKey);
        if (user != null) {
            List<TimeSpan> entries = findTimeSpansForUser(TIMESPAN_ILLNESS, from, to, user, username);
            if (entries.isEmpty()) {
                throw new NotFoundException(createNegativeResponse("No illness entries found for user " + user.getName()
                    + " and given timespan or no permission to read user data."));
            } else {
                return entries;
            }
        } else {
            throw new NotAuthorizedException(Response.serverError());
        }
    }

    /**
     * Returns all {@link TimeSpan} objects for the given {@link User} within the given date range. This search searches for
     * vacation entries only. Search must be narrowed down by
     * providing from and to parameters as Timestamp.
     *
     * @return JSON object with {@link TimeSpan} objects for that user.
     * @api {get} /calendar/user/vacation/{username} getVacationEntriesForUser
     * @apiName getVacationEntriesForUser
     * @apiGroup /calendar
     * @apiDescription Searches for all vacation entries of the given user.
     * The department is given by the departmentToken parameter. Parameters "from" and "to" indicate
     * the timespan to search.
     * @apiParam {String} username The user to use for the search.
     * @apiParam {String} apiKey The API-Key of the user accessing the service.
     * @apiParam {long} from Java timestamp to indicate the start date from which to search for vacation entries.
     * @apiParam {long} to Java timestamp to indicate the end date to search for resevations.
     * @apiSuccess {TimeSpan} timespan JSON Objects with all timespans currently registered for vacationentries.
     * @apiSuccessExample Success-Response:
     * HTTP/1.1 200 OK
     * [
     * {
     * "id" : 76,
     * "title" : "aaaa",
     * "description" : "default:aaaa[admin]",
     * "dateFrom" : 1479164400000,
     * "dateUntil" : 1485817200000,
     * "type" : "VACATION",
     * "department" : ...list of departments...
     * }
     * ]
     * @apiError NotAuthorized  DepartmentToken or APIKey incorrect.
     */
    @GET
    @Path("user/vacation/{username}")
    @Produces(MediaType.APPLICATION_JSON)
    public List<TimeSpan> getVacationEntriesForUser(@PathParam(value = "username") String username,
                                                    @QueryParam(value = "apiKey") String apiKey, @QueryParam(value = "from") String from,
                                                    @QueryParam(value = "to") String to) {
        User user = accessController.checkApiKey(apiKey);
        if (user != null) {
            List<TimeSpan> entries = findTimeSpansForUser(TIMESPAN_VACATION, from, to, user, username);
            if (entries.isEmpty()) {
                throw new NotFoundException(createNegativeResponse("No vacation entries found for user " + user.getName()
                    + " and given timespan or no permission to read user data."));
            } else {
                return entries;
            }
        } else {
            throw new NotAuthorizedException(Response.serverError());
        }
    }

    /**
     * Returns all {@link TimeSpan} objects for the given {@link User} within the given date range. This search searches for
     * resource reservation entries only. Search must be narrowed down by
     * providing from and to parameters as Timestamp.
     *
     * @return JSON object with {@link TimeSpan} objects for that user.
     * @api {get} /calendar/user/reservations/{username} getResourceReservationsForUser
     * @apiName getResourceReservationsForUser
     * @apiGroup /calendar
     * @apiDescription Searches for all resource reservation entries of the given user.
     * Parameters "from" and "to" indicate the timespan to search.
     * @apiParam {String} username     The user to use for the search.
     * @apiParam {String}   apiKey The API-Key of the user accessing the service.
     * @apiParam {long} from    Java timestamp to indicate the start date from which to search for illness entries.
     * @apiParam {long} to Java timestamp to indicate the end date to search for illness entries.
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
     * ...
     * }
     * ]
     * @apiError NotAuthorized or APIKey incorrect.
     */
    @GET
    @Path("user/reservations/{username}")
    @Produces(MediaType.APPLICATION_JSON)
    public List<TimeSpan> getResourceReservationsForUser(@PathParam(value = "username") String username,
                                                         @QueryParam(value = "apiKey") String apiKey, @QueryParam(value = "from") String from,
                                                         @QueryParam(value = "to") String to) {
        User user = accessController.checkApiKey(apiKey);
        if (user != null) {
            List<TimeSpan> entries = findTimeSpansForUser(TIMESPAN_RESOURCE_RESERVATION, from, to, user, username);
            if (entries.isEmpty()) {
                throw new NotFoundException(createNegativeResponse("No resource reservation entries found for user " + user.getName()
                    + " and given timespan or no permission to read user data."));
            } else {
                return entries;
            }
        } else {
            throw new NotAuthorizedException(Response.serverError());
        }
    }


    private List<TimeSpan> findTimeSpansForUser(String type, String timeSpanFrom, String timeSpanTo, User sessionUser,
                                                String username) {
        List<TimeSpan> entries = new ArrayList<>();
        List<TimeSpan> userTimeSpans = new ArrayList<>();

        User user = userRoleController.findUserByUsername(username, sessionUser);
        if (authController.canRead(user, sessionUser)) {
            if (type.equalsIgnoreCase(TIMESPAN_ILLNESS) || type.equalsIgnoreCase(TIMESPAN_ALL)) {
                if (user.getIllness() != null) {
                    userTimeSpans.add(user.getIllness());
                }
            }

            if (type.equalsIgnoreCase(TIMESPAN_VACATION) || type.equalsIgnoreCase(TIMESPAN_ALL)) {
                if (!user.getVacations().isEmpty()) {
                    userTimeSpans.addAll(user.getVacations());
                }
            }

            if (type.equalsIgnoreCase(TIMESPAN_RESOURCE_RESERVATION) || type.equalsIgnoreCase(TIMESPAN_ALL)) {
                List<ResourceReservation> reservations = resourceReservationController.getResourceReservationsForUser(user);
                if (null != reservations && !reservations.isEmpty()) {
                    for (ResourceReservation res : reservations) {
                        userTimeSpans.add(res.getTimeSpan());
                    }

                }
            }

            // If parameters "from" and "to" are present limit the search
            if (null != timeSpanFrom && null != timeSpanTo) {
                TimeSpan searchSpan = new TimeSpan();
                try {
                    searchSpan.setDateFrom(new Date(Long.parseLong(timeSpanFrom)));
                    searchSpan.setDateUntil(new Date(Long.parseLong(timeSpanTo)));
                } catch (Exception ex) {
                    throw new NotFoundException(createNegativeResponse("Missing or invalid Date parameters 'from' or 'to'!"));
                }
                for (TimeSpan span : userTimeSpans) {
                    // Add to search result if TimeSpan objects intersect
                    if (span.intersects(searchSpan)) {
                        entries.add(span);
                    }
                }
            } else {
                entries.addAll(userTimeSpans);
            }
        }
        return entries;
    }


    private Response createNegativeResponse(String responseText) {
        return Response.status(404).entity("{\"response\" : \"" + responseText + "\"}").build();
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

    private List<TimeSpan> findAllTimeSpansForReservations(User sessionUser, List<ResourceReservation> reservations) {
        List<TimeSpan> entries = new ArrayList<>();
        for (ResourceReservation res : reservations) {
            if (authController.canRead(res.getResource(), sessionUser)) {
                TimeSpan reservationTimeSpan = res.getTimeSpan();
                entries.add(reservationTimeSpan);
            }
        }
        return entries;
    }

}
