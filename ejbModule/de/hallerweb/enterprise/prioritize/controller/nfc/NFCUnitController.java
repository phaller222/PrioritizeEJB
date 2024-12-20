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

package de.hallerweb.enterprise.prioritize.controller.nfc;

import de.hallerweb.enterprise.prioritize.controller.resource.ResourceController;
import de.hallerweb.enterprise.prioritize.controller.security.SessionController;
import de.hallerweb.enterprise.prioritize.model.nfc.NFCCounter;
import de.hallerweb.enterprise.prioritize.model.nfc.NFCUnit;
import de.hallerweb.enterprise.prioritize.model.nfc.NFCUnit.NFCUnitType;
import de.hallerweb.enterprise.prioritize.model.resource.Resource;
import jakarta.ejb.EJB;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;

import java.io.Serializable;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@Stateless
public class NFCUnitController implements Serializable {

    @PersistenceContext
    transient EntityManager em;

    private static final String LITERAL_COUNTER = "Counter";

    @EJB
    transient ResourceController resourceController;
    @EJB
    transient SessionController sessionController;

    public NFCUnit findNFCUnitByUUID(String uuid) {
        Query query = em.createNamedQuery("findNFCUnitByUUID");
        query.setParameter("uuid", uuid);
        return (NFCUnit) query.getSingleResult();
    }

    public List<NFCUnit> findNFCUnitsByType(NFCUnitType type) {
        Query query = em.createNamedQuery("findNFCUnitsByType");
        query.setParameter("unitType", type);
        return query.getResultList();
    }

    public NFCUnit createNFCUnit(String name, String description, NFCUnitType unitType, Resource device, String payload, String uuid) {
        NFCUnit unit = createNFCUnit(name, description, unitType, device, payload);
        unit.setUuid(uuid);
        return unit;
    }

    public NFCUnit createNFCUnit(String name, String description, NFCUnitType unitType, Resource device, String payload) {
        NFCUnit unit = new NFCUnit();
        unit.setName(name);
        unit.setDescription(description);
        unit.setUnitType(unitType);
        unit.setSequenceNumber(0);
        unit.setUuid(UUID.randomUUID().toString());
        unit.setLastConnectedDevice(device);
        unit.setLastConnectedTime(new Date());
        unit.setPayload(payload);
        if (payload == null) {
            unit.setPayloadSize(0);
        } else {
            unit.setPayloadSize(payload.length());
        }
        em.persist(unit);
        return unit;
    }

    public NFCUnit updateNFCUnit(String uuid, NFCUnit unit, Resource device) {
        NFCUnit managedUnit = findNFCUnitByUUID(uuid);
        managedUnit.setName(unit.getName());
        managedUnit.setDescription(unit.getDescription());
        managedUnit.setLastConnectedDevice(device);
        managedUnit.setLastConnectedTime(new Date());
        managedUnit.setLatitude(unit.getLatitude());
        managedUnit.setLongitude(unit.getLongitude());
        managedUnit.setPayload(unit.getPayload());
        managedUnit.setPayloadSize(unit.getPayloadSize());
        managedUnit.setSequenceNumber(unit.getSequenceNumber() + 1);
        managedUnit.setUnitType(unit.getUnitType());
        return managedUnit;
    }

    public String readPayload(String uuid, long sequenceNumber) {
        NFCUnit unit = findNFCUnitByUUID(uuid);
        if (unit == null) {
            return "";
        } else {
            if (sequenceNumber == unit.getSequenceNumber()) {
                return unit.getPayload();
            } else {
                // If sequence number does not match (tampered with?) do NOT return payload data!
                return "";
            }
        }
    }

    public void writePayload(String uuid, String payload, Resource device) {
        NFCUnit unit = findNFCUnitByUUID(uuid);
        if (unit != null) {
            unit.setSequenceNumber(unit.getSequenceNumber() + 1);
            unit.setPayload(payload);
            unit.setPayloadSize(payload.length());
            unit.setLastConnectedTime(new Date());
            Resource managedResource = resourceController.getResource(device.getId(), sessionController.getUser());
            unit.setLastConnectedDevice(managedResource);
            unit.setLatitude(managedResource.getLatitude());
            unit.setLongitude(managedResource.getLongitude());
        }
    }

    public NFCCounter createNFCCounter() {
        NFCCounter counter = new NFCCounter();
        counter.setNfcUnit(createNFCUnit(LITERAL_COUNTER, "NFC Counter", NFCUnitType.COUNTER, null, ""));
        em.persist(counter);
        return counter;
    }

    public NFCCounter createNFCCounterWithUUID(String uuid) {
        NFCCounter counter = new NFCCounter();
        NFCUnit unit = createNFCUnit(LITERAL_COUNTER, "NFC counter", NFCUnitType.COUNTER, null, "0", uuid);
        counter.setNfcUnit(unit);
        em.persist(counter);
        return counter;
    }
}
