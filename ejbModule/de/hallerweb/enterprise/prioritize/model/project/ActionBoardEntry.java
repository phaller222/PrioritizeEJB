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

package de.hallerweb.enterprise.prioritize.model.project;

import de.hallerweb.enterprise.prioritize.model.document.Document;
import de.hallerweb.enterprise.prioritize.model.resource.Resource;
import jakarta.persistence.*;

import java.util.List;

/**
 * ActionBoardEntry.java - Stores inforamtion about an ActionBoardEntry and keeps
 * references to attached Documents and Resources. An Event is used as the source
 * of this ActionBoardEntry so all kinds of events can be posted.
 *
 * @author peter
 */
@NamedQuery(name = "findActionBoardEntryById", query = "select abe FROM ActionBoardEntry abe WHERE abe.id = :id")
@Entity
public class ActionBoardEntry {

    @Id
    @GeneratedValue
    private int id;

    private String title;

    private String message;

    @OneToOne
    private ActionBoard actionBoard;

    @OneToMany
    private List<Document> attachedDocuments;

    @OneToMany
    private List<Resource> attachedResources;

    public ActionBoard getActionBoard() {
        return actionBoard;
    }

    public void setActionBoard(ActionBoard actionBoard) {
        this.actionBoard = actionBoard;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public List<Document> getAttachedDocuments() {
        return attachedDocuments;
    }

    public void setAttachedDocuments(List<Document> attachedDocuments) {
        this.attachedDocuments = attachedDocuments;
    }

    public void addAttachedDocument(Document doc) {
        this.attachedDocuments.add(doc);
    }

    public List<Resource> getAttachedResources() {
        return attachedResources;
    }

    public void setAttachedResources(List<Resource> attachedResources) {
        this.attachedResources = attachedResources;
    }

    public void addAttachedResource(Resource res) {
        this.attachedResources.add(res);
    }

    public int getId() {
        return id;
    }
}
