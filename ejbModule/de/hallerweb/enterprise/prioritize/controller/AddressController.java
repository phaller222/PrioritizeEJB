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

package de.hallerweb.enterprise.prioritize.controller;

import de.hallerweb.enterprise.prioritize.controller.LoggingController.Action;
import de.hallerweb.enterprise.prioritize.controller.security.AuthorizationController;
import de.hallerweb.enterprise.prioritize.controller.security.SessionController;
import de.hallerweb.enterprise.prioritize.controller.security.UserRoleController;
import de.hallerweb.enterprise.prioritize.model.Address;
import de.hallerweb.enterprise.prioritize.model.Department;
import jakarta.ejb.EJB;
import jakarta.ejb.EJBException;
import jakarta.ejb.Stateless;
import jakarta.enterprise.context.ContextNotActiveException;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;

import java.util.List;

/**
 * AddressController.java - Controls the creation, modification and deletion of {@link Address} objects.
 */
@Stateless
public class AddressController {

    @PersistenceContext
    EntityManager em;
    @EJB
    AuthorizationController authController;
    @EJB
    UserRoleController userRoleController;
    @EJB
    LoggingController logger;
    @EJB
    InitializationController initController;
    @Inject
    SessionController sessionController;


    public static final String LITERAL_ADDRESS = "Address";
    public static final String LITERAL_SYSTEM = "SYSTEM";
    public static final String LITERAL_CREATED = "\" created.";


    public Address createAddress(String country, String street, String housenumber, String zipCode, String city, String phone, String fax, String mobile) {
        Address adr = new Address();
        adr.setCountry(country);
        adr.setStreet(street);
        adr.setHousenumber(housenumber);
        adr.setZipCode(zipCode);
        adr.setCity(city);
        adr.setPhone(phone);
        adr.setFax(fax);
        adr.setMobile(mobile);

        em.persist(adr);
        em.flush();
        try {
            if (sessionController.getUser() != null) {
                logger.log(sessionController.getUser().getUsername(), LITERAL_ADDRESS, Action.CREATE, adr.getId(),
                    " New Address \"" + adr.getId() + LITERAL_CREATED);
            }
        } catch (ContextNotActiveException ex) {
            logger.log(LITERAL_SYSTEM, LITERAL_ADDRESS, Action.CREATE, adr.getId(), " New Address \"" + adr.getId() + LITERAL_CREATED);
        }
        return adr;
    }

    /**
     * Returns a {@link List} of all adresses.
     *
     * @return List of Companies adresses.
     * @throws EJBException thrown exception if any errors
     */
    @SuppressWarnings("unchecked")
    public List<Address> getAllAddresses() {
        Query query = em.createNamedQuery("findAllAddresses");
        return query.getResultList();
    }

    /**
     * Deletes the {@link Address} with the given ID.
     *
     * @param id - The primary key (int) of the {@link Department} to be deleted.
     */
    public void deleteAddress(int id) {
        Address managedAddress = findAddressById(id);
        em.remove(managedAddress);
        logger.log(sessionController.getUser().getUsername(), LITERAL_ADDRESS, Action.DELETE, managedAddress.getId(),
            " Address \"" + managedAddress.getId() + "\" deleted.");
    }


    public Address findAddressById(int id) {
        return em.find(Address.class, id);
    }

}
