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
package de.hallerweb.enterprise.prioritize.model.nfc;

import de.hallerweb.enterprise.prioritize.model.resource.Resource;
import jakarta.persistence.*;

import java.util.Date;

@Entity
@NamedQuery(name = "findNFCUnitByUUID", query = "select nfc FROM NFCUnit nfc WHERE nfc.uuid = :uuid")
@NamedQuery(name = "findNFCUnitsByType", query = "select nfc FROM NFCUnit nfc WHERE nfc.unitType = :unitType")
public class NFCUnit {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    int id;

    private String uuid;                            // UUID of NFC-Unit
    private String name;                            // Name of this unit
    private String description;                        // Description of this unit.
    private String payload;                            // Payload of NFC-Unit
    private int payloadSize;                        // Size of the Payload
    private Date lastConnectedTime;                    // Last time a device connected to the NFC-Unit
    private String latitude;                        // Last known latitude of NFC-Unit
    private String longitude;                        // Last known longitude NFC-Unit
    private long sequenceNumber;                    // Sequence Number which increases on each write
    private NFCUnitType unitType;                    // Type of this NFCUnit

    @OneToOne
    Resource lastConnectedDevice;                    // Device which recently connected to the chip.

    public enum NFCUnitType {
        COUNTER, CHECKPOINT, TIMETRACKER, INFOPOINT
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public long getSequenceNumber() {
        return sequenceNumber;
    }

    public void setSequenceNumber(long sequenceNumber) {
        this.sequenceNumber = sequenceNumber;
    }

    public String getPayload() {
        return payload;
    }

    public void setPayload(String payload) {
        this.payload = payload;
    }

    public int getPayloadSize() {
        return payloadSize;
    }

    public void setPayloadSize(int payloadSize) {
        this.payloadSize = payloadSize;
    }

    public Date getLastConnectedTime() {
        return lastConnectedTime;
    }

    public void setLastConnectedTime(Date lastConnectedTime) {
        this.lastConnectedTime = lastConnectedTime;
    }

    public String getLatitude() {
        return latitude;
    }

    public void setLatitude(String latitude) {
        this.latitude = latitude;
    }

    public String getLongitude() {
        return longitude;
    }

    public void setLongitude(String longitude) {
        this.longitude = longitude;
    }

    public Resource getLastConnectedDevice() {
        return lastConnectedDevice;
    }

    public void setLastConnectedDevice(Resource lastConnectedDevice) {
        this.lastConnectedDevice = lastConnectedDevice;
    }

    public int getId() {
        return id;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public NFCUnitType getUnitType() {
        return unitType;
    }

    public void setUnitType(NFCUnitType unitType) {
        this.unitType = unitType;
    }

    public Resource getWrappedResource() {
        return lastConnectedDevice;
    }

    public void setWrappedResource(Resource wrappedResource) {
        this.lastConnectedDevice = wrappedResource;
    }

}
