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

package de.hallerweb.enterprise.prioritize.model.nfc;

import jakarta.persistence.Entity;
import jakarta.persistence.OneToOne;

import java.util.logging.Level;
import java.util.logging.Logger;

@Entity
public class NFCCounter extends PCounter {

    @OneToOne
    NFCUnit nfcUnit;

    String uuid;

    public NFCUnit getNfcUnit() {
        return nfcUnit;
    }

    public void setNfcUnit(NFCUnit nfcUnit) {
        this.nfcUnit = nfcUnit;
    }

    @Override
    public int getId() {
        return id;
    }

    @Override
    public long getValue() {
        String payload = nfcUnit.getPayload();
        if (isNumeric(payload)) {
            return Long.parseLong(payload);
        } else {
            return -1;
        }
    }

    @Override
    public void setValue(long value) {
        nfcUnit.setPayload(String.valueOf(value));
    }

    @Override
    public void incCounter() {
        String payload = nfcUnit.getPayload();
        if (isNumeric(payload)) {
            long newValue = Long.parseLong(payload) + 1;
            Logger.getLogger(getClass().getName()).log(Level.INFO, "SET: " + newValue);
            nfcUnit.setPayload(String.valueOf(newValue));
        }
    }

    @Override
    public void decCounter() {
        String payload = nfcUnit.getPayload();
        if (isNumeric(payload)) {
            long newValue = Long.parseLong(payload) - 1;
            nfcUnit.setPayload(String.valueOf(newValue));
        }
    }

    @Override
    public String getUuid() {
        return nfcUnit.getUuid();
    }

    private boolean isNumeric(String s) {
        try {
            Long.valueOf(s);
            return true;
        } catch (NumberFormatException ex) {
            return false;
        }
    }

}
