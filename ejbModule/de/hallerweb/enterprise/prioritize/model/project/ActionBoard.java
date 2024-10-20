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

import de.hallerweb.enterprise.prioritize.model.PObject;
import jakarta.persistence.*;

import java.util.List;

/**
 * ActionBoard.java - Holds ActionBoardEntry's. It is assigned either to a department, a user or a project
 * and other objects can "subscribe" themselves to receive changes.
 *
 * @author peter
 */
@Entity
@NamedQuery(name = "findActionBoardById", query = "select ab FROM ActionBoard ab WHERE ab.id = :actionBoardId")
@NamedQuery(name = "findActionBoardByName", query = "select ab FROM ActionBoard ab WHERE ab.name = :actionBoardName")
@NamedQuery(name = "findActionBoardByOwner", query = "select ab FROM ActionBoard ab WHERE ab.owner.id = :ownerId")
public class ActionBoard extends PObject {

    private String name;
    private String description;

    @OneToOne
    private PObject owner;

    @OneToMany(fetch = FetchType.LAZY)
    private List<ActionBoardEntry> entries;

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public PObject getOwner() {
        return owner;
    }

    public void setOwner(PObject owner) {
        this.owner = owner;
    }

    public List<ActionBoardEntry> getEntries() {
        return entries;
    }

    public void setEntries(List<ActionBoardEntry> entries) {
        this.entries = entries;
    }

    public void addEntry(ActionBoardEntry entry) {
        entries.add(entry);
    }

    public void removeEntry(ActionBoardEntry entry) {
        entries.remove(entry);
    }

    public String getName() {
        return name;
    }

    public void setDescriprion(String desc) {
        this.description = desc;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public int getId() {
        return id;
    }
}
