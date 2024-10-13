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

package de.hallerweb.enterprise.prioritize.model.resource;

import com.fasterxml.jackson.annotation.JsonBackReference;
import de.hallerweb.enterprise.prioritize.model.calendar.ITimeSpan;
import de.hallerweb.enterprise.prioritize.model.calendar.TimeSpan;
import de.hallerweb.enterprise.prioritize.model.security.User;
import jakarta.persistence.*;

/**
 * JPA entity to represent a {@link ResourceReservation}. Users can reserve Resources for a given timeframe. This entity holds information
 * about the User who reserved a Resource, the TimeFrame (from, until) and the Resource itself.
 *
 * <p>
 * Copyright: (c) 2014
 * </p>
 * <p>
 * Peter Haller
 * </p>
 *
 * @author peter
 */
@Entity
@NamedQuery(name = "findPastResoureReservations", query = "select rr FROM ResourceReservation rr WHERE rr.timespan.dateUntil < :now")
@NamedQuery(name = "findAllResourceReservations", query = "select rr FROM ResourceReservation rr")
@NamedQuery(name = "findResourceReservationsForResourceGroup",
    query = "select rr FROM ResourceReservation rr WHERE rr.resource.resourceGroup.id = :resourceGroupId")
@NamedQuery(name = "findResourceReservationsForDepartment", query = "select rr FROM ResourceReservation rr WHERE rr.resource.department.id = :departmentId")
@NamedQuery(name = "findResourceReservationsForUser", query = "select rr FROM ResourceReservation rr WHERE rr.reservedBy.id = :userId")
public class ResourceReservation implements ITimeSpan {

    @Id
    @GeneratedValue
    private int id;

    @OneToOne
    @JsonBackReference(value = "resourcesBackRef")
    private Resource resource;

    @OneToOne
    private User reservedBy;

    @OneToOne(cascade = CascadeType.ALL)
    private TimeSpan timespan; // TimeSpan indication when the resource has been reserved (from/until).

    /**
     * The number of the slot which can be used for commands.
     * A default resource without a defined bnumber of slots always has one slot and
     * one slot available. So in that case always slot nr. 1 is assigned.
     */
    int slotNumber;

    public int getSlotNumber() {
        return slotNumber;
    }

    public void setSlotNumber(int slotNumber) {
        this.slotNumber = slotNumber;
    }

    public int getId() {
        return id;
    }

    public Resource getResource() {
        return resource;
    }

    public void setResource(Resource resource) {
        this.resource = resource;
    }

    public User getReservedBy() {
        return reservedBy;
    }

    public void setReservedBy(User reservedBy) {
        this.reservedBy = reservedBy;
    }

    public TimeSpan getTimeSpan() {
        return timespan;
    }

    public void setTimeSpan(TimeSpan timespan) {
        this.timespan = timespan;
    }
}
