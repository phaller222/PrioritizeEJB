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
