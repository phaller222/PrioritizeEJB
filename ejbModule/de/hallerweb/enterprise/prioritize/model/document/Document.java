package de.hallerweb.enterprise.prioritize.model.document;

import java.util.Arrays;
import java.util.Date;

import javax.persistence.Basic;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToOne;
import javax.persistence.Version;

import com.fasterxml.jackson.annotation.JsonIgnore;

import de.hallerweb.enterprise.prioritize.model.security.User;

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
@NamedQueries({ @NamedQuery(name = "findDocumentById", query = "select d FROM Document d WHERE d.id = :docId"),
		@NamedQuery(name = "findDocumentByTag", query = "select d FROM Document d WHERE d.tag = :docTag") })
public class Document implements Comparable {

	static final public String PROPERTY_NAME="name";
	static final public String PROPERTY_MIMETYPE="mimeType";
	static final public String PROPERTY_TAG="tag";
	static final public String PROPERTY_ENCRYPTED="encrypted";
	static final public String PROPERTY_CHANGES="changes";
	
	@Id
	@GeneratedValue
	@JsonIgnore
	int id;

	private String name; // Name of the document.
	private int version; // Version of the document
	private String mimeType; // mimeType
	private String tag; // Tag for this document (if it has been tagged)
	private Date lastModified; // Date of last modification
	private boolean encrypted; // Is the document encrpted?
	private String changes; // Description of the last changes made.

	@OneToOne
	private User lastModifiedBy; // Who last modified this document
	@OneToOne
	private User encryptedBy; // Who encrypted this document
	@Lob
	@JsonIgnore
	@Basic(fetch = FetchType.LAZY)
	private byte[] data; // the data of the document (e.G. binary MS-word data).

	@Version
	private int entityVersion;

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
	public int compareTo(Object o) {
		Document doc = (Document) o;
		if (doc.getVersion() == version) {
			return 0;
		} else if (doc.getVersion() > version) {
			return -1;
		} else
			return 1;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((changes == null) ? 0 : changes.hashCode());
		result = prime * result + Arrays.hashCode(data);
		result = prime * result + (encrypted ? 1231 : 1237);
		result = prime * result + ((encryptedBy == null) ? 0 : encryptedBy.hashCode());
		result = prime * result + entityVersion;
		result = prime * result + id;
		result = prime * result + ((lastModified == null) ? 0 : lastModified.hashCode());
		result = prime * result + ((lastModifiedBy == null) ? 0 : lastModifiedBy.hashCode());
		result = prime * result + ((mimeType == null) ? 0 : mimeType.hashCode());
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		result = prime * result + ((tag == null) ? 0 : tag.hashCode());
		result = prime * result + version;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Document other = (Document) obj;
		if (changes == null) {
			if (other.changes != null)
				return false;
		} else if (!changes.equals(other.changes))
			return false;
		if (!Arrays.equals(data, other.data))
			return false;
		if (encrypted != other.encrypted)
			return false;
		if (encryptedBy == null) {
			if (other.encryptedBy != null)
				return false;
		} else if (!encryptedBy.equals(other.encryptedBy))
			return false;
		if (entityVersion != other.entityVersion)
			return false;
		if (id != other.id)
			return false;
		if (lastModified == null) {
			if (other.lastModified != null)
				return false;
		} else if (!lastModified.equals(other.lastModified))
			return false;
		if (lastModifiedBy == null) {
			if (other.lastModifiedBy != null)
				return false;
		} else if (!lastModifiedBy.equals(other.lastModifiedBy))
			return false;
		if (mimeType == null) {
			if (other.mimeType != null)
				return false;
		} else if (!mimeType.equals(other.mimeType))
			return false;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		if (tag == null) {
			if (other.tag != null)
				return false;
		} else if (!tag.equals(other.tag))
			return false;
		if (version != other.version)
			return false;
		return true;
	}

}
