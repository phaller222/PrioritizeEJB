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

package de.hallerweb.enterprise.prioritize.model.project.goal;

import de.hallerweb.enterprise.prioritize.model.document.DocumentInfo;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.OneToOne;

@Entity
public class ProjectGoalPropertyRecord {

    @Id
    @GeneratedValue
    int id;

    @OneToOne
    ProjectGoalProperty property;

    double value;

    @OneToOne
    DocumentInfo documentInfo;

    boolean documentPropertyRecord;
    boolean numericPropertyRecord;

    public boolean isDocumentPropertyRecord() {
        return documentPropertyRecord;
    }

    public void setDocumentPropertyRecord(boolean documentPropertyRecord) {
        this.documentPropertyRecord = documentPropertyRecord;
    }

    public boolean isNumericPropertyRecord() {
        return numericPropertyRecord;
    }

    public void setNumericPropertyRecord(boolean numericPropertyRecord) {
        this.numericPropertyRecord = numericPropertyRecord;
    }

    public DocumentInfo getDocumentInfo() {
        return documentInfo;
    }

    public void setDocumentInfo(DocumentInfo documentInfo) {
        this.documentInfo = documentInfo;
    }

    public ProjectGoalProperty getProperty() {
        return property;
    }

    public void setProperty(ProjectGoalProperty property) {
        this.property = property;
    }

    public double getValue() {
        return value;
    }

    public void setValue(double value) {
        this.value = value;
    }

    public int getId() {
        return id;
    }

}
