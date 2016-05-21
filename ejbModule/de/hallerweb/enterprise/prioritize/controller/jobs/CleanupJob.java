package de.hallerweb.enterprise.prioritize.controller.jobs;

import javax.ejb.EJB;
import javax.ejb.LocalBean;
import javax.ejb.Schedule;
import javax.ejb.Singleton;

import de.hallerweb.enterprise.prioritize.controller.CompanyController;
import de.hallerweb.enterprise.prioritize.controller.resource.ResourceController;
import de.hallerweb.enterprise.prioritize.controller.security.SessionController;
import de.hallerweb.enterprise.prioritize.controller.security.UserRoleController;

/**
 * Session Bean implementation class CleanupJob. This Singleton bean is called every minute to perform different kinds of cleanup jobs e.G.
 * delete reservations for a resource from database after the time of the reservation has passed.
 */
@Singleton
@LocalBean
public class CleanupJob {

	@EJB
	ResourceController resourceController;

	/**
	 * Default constructor.
	 */
	public CleanupJob() {
		// TODO Auto-generated constructor stub
	}

	@Schedule(minute = "*/1", hour = "*", persistent = false)
	public void cleanup() {
		System.out.println("cleaning up...");
		resourceController.cleanupReservations();

		// TODO: cleanup Vacation and illness entries from the past...? Does this make sense or
		// shall we keep all entries because the data amount isn't THAT Big.
	}
}
