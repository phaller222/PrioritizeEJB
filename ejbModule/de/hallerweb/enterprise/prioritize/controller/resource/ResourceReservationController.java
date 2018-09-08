package de.hallerweb.enterprise.prioritize.controller.resource;

import java.util.Date;
import java.util.List;
import java.util.Set;

import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.persistence.TemporalType;

import org.jboss.resteasy.logging.Logger;

import de.hallerweb.enterprise.prioritize.controller.CompanyController;
import de.hallerweb.enterprise.prioritize.controller.InitializationController;
import de.hallerweb.enterprise.prioritize.controller.LoggingController;
import de.hallerweb.enterprise.prioritize.controller.LoggingController.Action;
import de.hallerweb.enterprise.prioritize.controller.event.EventRegistry;
import de.hallerweb.enterprise.prioritize.controller.security.AuthorizationController;
import de.hallerweb.enterprise.prioritize.controller.security.SessionController;
import de.hallerweb.enterprise.prioritize.controller.security.UserRoleController;
import de.hallerweb.enterprise.prioritize.model.PObject;
import de.hallerweb.enterprise.prioritize.model.calendar.TimeSpan;
import de.hallerweb.enterprise.prioritize.model.calendar.TimeSpan.TimeSpanType;
import de.hallerweb.enterprise.prioritize.model.event.Event;
import de.hallerweb.enterprise.prioritize.model.event.PEventConsumerProducer;
import de.hallerweb.enterprise.prioritize.model.resource.Resource;
import de.hallerweb.enterprise.prioritize.model.resource.ResourceReservation;
import de.hallerweb.enterprise.prioritize.model.security.User;
import de.hallerweb.enterprise.prioritize.service.mqtt.MQTTService;

/**
 * ResourceReservationController.java - Controls the creation, modification and deletion of
 * {@link ResourceReservation} objects.  * 
 */
@Stateless
public class ResourceReservationController extends PEventConsumerProducer {
	@PersistenceContext(unitName = "MySqlDS")
	EntityManager em;

	@EJB
	UserRoleController userRoleController;
	@EJB
	CompanyController companyController;
	@Inject
	SessionController sessionController;
	@EJB
	AuthorizationController authController;
	@EJB
	LoggingController logger;
	@Inject
	MQTTService mqttService;
	@Inject
	EventRegistry eventRegistry;
	
	public void raiseEvent(PObject source, String name, String oldValue, String newValue, long lifetime) {
		if (InitializationController.getAsBoolean(InitializationController.FIRE_RESOURCE_EVENTS)) {
			Event evt = eventRegistry.getEventBuilder().newEvent().setSource(source).setOldValue(oldValue).setNewValue(newValue)
					.setPropertyName(name).setLifetime(lifetime).getEvent();
			eventRegistry.addEvent(evt);
		}
	}

	@Override
	public void consumeEvent(PObject destination, Event evt) {
		Logger.getLogger(this.getClass()).info("Object " + evt.getSource() + " raised event: " + evt.getPropertyName() + " with new Value: "
				+ evt.getNewValue() + "--- Resource listening: " + ((Resource) destination).getId());
	}
	
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
	 * @return
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
	 * @param resource
	 * @return
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
	 * @param resourceGroupId
	 * @return
	 */
	public List<ResourceReservation> getResourceReservationsForResourceGroup(int resourceGroupId) {

		Query query = em.createNamedQuery("findResourceReservationsForResourceGroup");
		query.setParameter("resourceGroupId", resourceGroupId);

		@SuppressWarnings("unchecked")
		List<ResourceReservation> reservations = (List<ResourceReservation>) query.getResultList();
		return reservations;
	}

	public List<ResourceReservation> getResourceReservationsForDepartment(int departmentId) {
		Query query = em.createNamedQuery("findResourceReservationsForDepartment");
		query.setParameter("departmentId", departmentId);

		@SuppressWarnings("unchecked")
		List<ResourceReservation> reservations = (List<ResourceReservation>) query.getResultList();
		return reservations;
	}

	public List<ResourceReservation> getAllResourceReservations() {
		Query query = em.createNamedQuery("findAllResourceReservations");
		@SuppressWarnings("unchecked")
		List<ResourceReservation> reservations = (List<ResourceReservation>) query.getResultList();
		return reservations;
	}

	public List<ResourceReservation> getResourceReservationsForUser(User user) {
		Query query = em.createNamedQuery("findResourceReservationsForUser");
		query.setParameter("UserId", user.getId());
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

			logger.log(user.getUsername(), "ResourceReservation", Action.CREATE, reservation.getId(), " ResourceReservation created.");
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
		logger.log(sessionController.getUser().getUsername(), "ResourceReservation", Action.DELETE, reservation.getId(),
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
				logger.log("SYSTEM", "ResourceReservation", Action.DELETE, reservation.getId(),
						"ResourceReservation no longer valid, so i deleted it.");
			}
		}
	}
	
	
	private boolean isWithinTimeframe(Date requestedFrom, Date requestedTo, ResourceReservation res) {
		

		if (requestedFrom.after(res.getTimeSpan().getDateUntil())) {
			return false;
		} else if (requestedTo.before(res.getTimeSpan().getDateFrom())){
			return false;
		}
		return true;
	}
	
	
}
	
	