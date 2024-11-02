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

package de.hallerweb.enterprise.prioritize.model.document;

import de.hallerweb.enterprise.prioritize.model.PObject;
import de.hallerweb.enterprise.prioritize.model.security.User;
import jakarta.persistence.*;

import java.util.Date;

/**
 * JPA entity to represent a document.
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
@NamedQuery(name = "findDocumentById", query = "select d FROM Document d WHERE d.id = :docId")
@NamedQuery(name = "findDocumentByTag", query = "select d FROM Document d WHERE d.tag = :docTag")
@NamedQuery(name = "findDocumentsByMimeType", query = "select d FROM Document d WHERE d.mimeType = :docMimeType")
public class Document extends PObject implements Comparable<Object> {

    public static final String PROPERTY_NAME = "name";
    public static final String PROPERTY_MIMETYPE = "mimeType";
    public static final String PROPERTY_TAG = "tag";
    public static final String PROPERTY_ENCRYPTED = "encrypted";
    public static final String PROPERTY_CHANGES = "changes";

    private String name; // Name of the document.
    private int version; // Version of the document
    private String mimeType; // mimeType
    private String tag; // Tag for this document (if it has been tagged)
    private Date lastModified; // Date of last modification
    private boolean encrypted; // Is the document encrpted?
    private String changes; // Description of the last changes made.

    @ManyToOne
    private User lastModifiedBy; // Who last modified this document
    @OneToOne
    private User encryptedBy; // Who encrypted this document
    @Lob
    @Basic(fetch = FetchType.LAZY)
    private byte[] data; // the data of the document (e.G. binary MS-word data).

    public String getChanges() {
        return changes;
    }

    public void setChanges(String changes) {
        this.changes = changes;
    }

    public byte[] getData() {
        return data;
    }

    public void setData(byte[] data) {
        this.data = data;
    }

    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }

    public Date getLastModified() {
        return lastModified;
    }

    public void setLastModified(Date lastModified) {
        this.lastModified = lastModified;
    }

    public User getLastModifiedBy() {
        return lastModifiedBy;
    }

    public void setLastModifiedBy(User lastModifiedBy) {
        this.lastModifiedBy = lastModifiedBy;
    }

    public String getMimeType() {
        return mimeType;
    }

    public void setMimeType(String mimeType) {
        this.mimeType = mimeType;
    }

    public boolean isEncrypted() {
        return encrypted;
    }

    public void setEncrypted(boolean encrypted) {
        this.encrypted = encrypted;
    }

    public User getEncryptedBy() {
        return encryptedBy;
    }

    public void setEncryptedBy(User encryptedBy) {
        this.encryptedBy = encryptedBy;
    }

    @Override
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    @Override
    public int compareTo(Object obj) {
        Document doc = (Document) obj;
        return Integer.compare(version, doc.getVersion());
    }

    @Override
    public boolean equals(Object obj) {
        return super.equals(obj);
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }
}
