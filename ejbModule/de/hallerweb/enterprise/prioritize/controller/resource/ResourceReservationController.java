/*
 * Copyright 2015-2024 Peter Michael Haller and contributors
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

package de.hallerweb.enterprise.prioritize.controller.resource;

import de.hallerweb.enterprise.prioritize.controller.InitializationController;
import de.hallerweb.enterprise.prioritize.controller.LoggingController;
import de.hallerweb.enterprise.prioritize.controller.LoggingController.Action;
import de.hallerweb.enterprise.prioritize.controller.security.SessionController;
import de.hallerweb.enterprise.prioritize.model.calendar.TimeSpan;
import de.hallerweb.enterprise.prioritize.model.calendar.TimeSpan.TimeSpanType;
import de.hallerweb.enterprise.prioritize.model.resource.Resource;
import de.hallerweb.enterprise.prioritize.model.resource.ResourceReservation;
import de.hallerweb.enterprise.prioritize.model.security.User;
import jakarta.ejb.EJB;
import jakarta.ejb.Stateless;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import jakarta.persistence.TemporalType;

import java.util.Date;
import java.util.List;
import java.util.Set;

/**
 * ResourceReservationController.java - Controls the creation, modification and deletion of
 * {@link ResourceReservation} objects.  *
 */
@Stateless
public class ResourceReservationController {
    @PersistenceContext
    EntityManager em;
    @Inject
    SessionController sessionController;
    @EJB
    LoggingController logger;
    @EJB
    InitializationController initController;

    private static final String LITERAL_RESOURCE_RESERVATION = "ResourceReservation";

    public boolean isResourceActiveForUser(User user, Set<ResourceReservation> reservations) {
        Date now = new Date();
        Date now2 = new Date(System.currentTimeMillis() + 10000);
        for (ResourceReservation reservation : reservations) {
            if (reservation.getReservedBy().getId() == user.getId() && isWithinTimeframe(now, now2, reservation)) {
                return true;
            }
        }
        return false;
    }

    public int getActiveSlotForUser(User user, Set<ResourceReservation> reservations) {
        Date now = new Date();
        Date now2 = new Date(System.currentTimeMillis() + 10000);
        for (ResourceReservation reservation : reservations) {
            if (reservation.getReservedBy().getId() == user.getId() && isWithinTimeframe(now, now2, reservation)) {
                return reservation.getSlotNumber();
            }
        }
        return -1;
    }

    /**
     * Returns all ResourceReservation objects which reservation timespans are
     * in the past. Should be used to remove obsolete reservation entries.
     *
     * @return List with ResourceReservation objects.
     */
    @SuppressWarnings("unchecked")
    public List<ResourceReservation> getPastResourceReservations() {
        Query query = em.createNamedQuery("findPastResoureReservations");
        query.setParameter("now", new Date(), TemporalType.TIMESTAMP);
        return query.getResultList();
    }

    /**
     * Finds out how many slots (=available items or whatever) of a given
     * resource are available at the moment.
     *
     * @param resource The resource
     * @return number of slots of this resource with are currently in use.
     */
    public int getSlotsInUse(Resource resource) {
        int slotsInUse = 0;
        Resource res = em.find(Resource.class, resource.getId());
        if (res != null) {
            Set<ResourceReservation> reservations = res.getReservations();
            if (reservations != null) {
                for (ResourceReservation reservation : reservations) {
                    Date now = new Date(System.currentTimeMillis());
                    if (reservation.getTimeSpan().getDateFrom().before(now) && reservation.getTimeSpan().getDateUntil().after(now)) {
                        slotsInUse++;
                    }
                }
            }
        }
        return slotsInUse;
    }


    /**
     * Return all ResourceReservation entries for resources assigned into the
     * given ResourceGroup.
     *
     * @param resourceGroupId The ID of the resource group
     * @return A List with ResourceReservations for the ResourceGroup.
     */
    public List<ResourceReservation> getResourceReservationsForResourceGroup(int resourceGroupId) {

        Query query = em.createNamedQuery("findResourceReservationsForResourceGroup");
        query.setParameter("resourceGroupId", resourceGroupId);

        @SuppressWarnings("unchecked")
        List<ResourceReservation> reservations = query.getResultList();
        return reservations;
    }


    /**
     * Return all ResourceReservation entries for the given resource.
     *
     * @param resourceId The ID of the resource
     * @return A List with ResourceReservations for the Resource.
     */
    public List<ResourceReservation> getResourceReservationsForResource(int resourceId) {

        Query query = em.createNamedQuery("findResourceReservationsForResource");
        query.setParameter("resourceId", resourceId);

        @SuppressWarnings("unchecked")
        List<ResourceReservation> reservations = query.getResultList();
        return reservations;
    }

    public List<ResourceReservation> getResourceReservationsForDepartment(int departmentId) {
        Query query = em.createNamedQuery("findResourceReservationsForDepartment");
        query.setParameter("departmentId", departmentId);

        @SuppressWarnings("unchecked")
        List<ResourceReservation> reservations = (query.getResultList());
        return reservations;
    }

    public List<ResourceReservation> getAllResourceReservations() {
        Query query = em.createNamedQuery("findAllResourceReservations");
        @SuppressWarnings("unchecked")
        List<ResourceReservation> reservations = query.getResultList();
        return reservations;
    }

    public List<ResourceReservation> getResourceReservationsForUser(User user) {
        Query query = em.createNamedQuery("findResourceReservationsForUser");
        query.setParameter("userId", user.getId());
        return query.getResultList();
    }

    public ResourceReservation createResourceReservation(Resource resource, Date from, Date until, User user) {
        Resource res = em.find(Resource.class, resource.getId());
        User managedUser = em.find(User.class, user.getId());
        Set<ResourceReservation> reservations = res.getReservations();
        int slotsAvailable = calcFreeSlots(reservations, res.getMaxSlots(), from, until);
        if (slotsAvailable <= 0) {
            return null;
        } else {
            // Create resource reservation for resource
            ResourceReservation reservation = new ResourceReservation();
            reservation.setReservedBy(user);

            // Generate TimeSpan and persist
            TimeSpan ts = new TimeSpan();
            ts.setTitle(res.getName());
            ts.setDescription(res.getResourceGroup().getName() + ":" + res.getName() + "[" + user.getName() + "]");
            ts.setDateFrom(from);
            ts.setDateUntil(until);
            ts.addInvolvedResource(res);
            ts.addInvolvedUser(managedUser);
            ts.setType(TimeSpanType.RESOURCE_RESERVATION);

            em.persist(ts);
            em.flush();

            reservation.setTimeSpan(ts);
            reservation.setResource(res);

            if (res.getMaxSlots() == 1) {
                reservation.setSlotNumber(1);
            } else {
                reservation.setSlotNumber(getNextAvailableSlot(res, reservations, from, until));
            }

            em.persist(reservation);
            res.addReservation(reservation);
            em.flush();

            logger.log(user.getUsername(), LITERAL_RESOURCE_RESERVATION, Action.CREATE, reservation.getId(),
                " ResourceReservation created.");
            return reservation;
        }

    }

    private int getNextAvailableSlot(Resource resource, Set<ResourceReservation> reservations, Date from, Date to) {
        boolean[] bookedSlots = new boolean[resource.getMaxSlots()];
        for (ResourceReservation reservation : reservations) {
            if (isWithinTimeframe(from, to, reservation)) {
                bookedSlots[reservation.getSlotNumber()] = true;
            }
        }
        for (int i = 0; i < bookedSlots.length; i++) {
            if (!bookedSlots[i]) {
                return i;
            }
        }
        return -1;

    }

    public ResourceReservation removeResourceReservation(int reservationId) {
        ResourceReservation reservation = em.find(ResourceReservation.class, reservationId);
        reservation.getResource().getReservations().remove(reservation);
        em.remove(reservation);
        em.flush();
        logger.log(sessionController.getUser().getUsername(), LITERAL_RESOURCE_RESERVATION, Action.DELETE, reservation.getId(),
            " ResourceReservation deleted.");

        return reservation;
    }

    private int calcFreeSlots(Set<ResourceReservation> reservations, int maxSlots, Date from, Date to) {
        int slots = maxSlots;
        if (reservations == null) {
            return slots;
        }
        for (ResourceReservation reservation : reservations) {
            if (isWithinTimeframe(from, to, reservation)) {
                if (slots > 1) {
                    slots--;
                } else {
                    return 0;
                }
            }
        }
        return slots;
    }


    public void cleanupReservations() {
        // clean up all Resource Reservations which have past
        List<ResourceReservation> reservations = getPastResourceReservations();
        if (reservations != null) {
            for (ResourceReservation reservation : reservations) {
                reservation.getResource().getReservations().remove(reservation);
                em.remove(reservation);
                logger.log("SYSTEM", LITERAL_RESOURCE_RESERVATION, Action.DELETE, reservation.getId(),
                    "ResourceReservation no longer valid, so i deleted it.");
            }
        }
    }


    private boolean isWithinTimeframe(Date requestedFrom, Date requestedTo, ResourceReservation res) {


        if (requestedFrom.after(res.getTimeSpan().getDateUntil())) {
            return false;
        } else {
            return !requestedTo.before(res.getTimeSpan().getDateFrom());
        }
    }
}

