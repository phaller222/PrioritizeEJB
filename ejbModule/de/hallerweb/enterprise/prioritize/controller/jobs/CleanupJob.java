package de.hallerweb.enterprise.prioritize.controller.jobs;

import javax.ejb.EJB;
import javax.ejb.LocalBean;
import javax.ejb.Schedule;
import javax.ejb.Singleton;

import de.hallerweb.enterprise.prioritize.controller.resource.ResourceReservationController;

/**
 * Session Bean implementation class CleanupJob. This Singleton bean is called every minute to perform different kinds of cleanup jobs e.G.
 * delete reservations for a resource from database after the time of the reservation has passed.
 */
@Singleton
@LocalBean
public class CleanupJob {

	@EJB
	ResourceReservationController resourceReservationController;

	/**
	 * Default constructor.
	 */
	public CleanupJob() {
		// Auto-generated constructor stub
	}

	@Schedule(minute = "*/5", hour = "*", persistent = false)
	public void cleanup() {
		resourceReservationController.cleanupReservations();
	}
}
