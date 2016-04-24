package de.hallerweb.enterprise.prioritize.model.inbox;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToOne;
import javax.persistence.Version;

import com.fasterxml.jackson.annotation.JsonIgnore;

import de.hallerweb.enterprise.prioritize.model.security.User;

/**
 * JPA entity to represent a {@link Message}. Simply a Message (text or text with references) to other Users.
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
@NamedQueries({
		@NamedQuery(name = "findReceivedMessagesForUser", query = "SELECT msg FROM Message msg WHERE msg.to.id = :userId ORDER BY msg.dateReceived"),
		@NamedQuery(name = "findUnreadMessagesForUser", query = "SELECT msg FROM Message msg WHERE msg.to.id = :userId AND msg.messageRead = false ORDER BY msg.dateReceived"),
		@NamedQuery(name = "findSentMessagesForUser", query = "SELECT msg FROM Message msg WHERE msg.from.id = :userId ORDER BY msg.dateReceived") })
public class Message {

	@Id
	@GeneratedValue
	int id;

	Date dateReceived;
	Date dateRead;
	boolean messageRead;
	String subject;

	@OneToOne
	User from;

	@OneToOne
	User to;

	@JsonIgnore
	@Column(length = 65535)
	String message;

	@Version
	private int entityVersion; // For optimistic locks

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public Date getDateReceived() {
		return dateReceived;
	}

	public void setDateReceived(Date dateReceived) {
		this.dateReceived = dateReceived;
	}

	public Date getDateRead() {
		return dateRead;
	}

	public void setDateRead(Date dateRead) {
		this.dateRead = dateRead;
	}

	public boolean isMessageRead() {
		return messageRead;
	}

	public void setMessageRead(boolean read) {
		this.messageRead = read;
	}

	public User getFrom() {
		return from;
	}

	public void setFrom(User from) {
		this.from = from;
	}

	public User getTo() {
		return to;
	}

	public void setTo(User to) {
		this.to = to;
	}

	public String getSubject() {
		return subject;
	}

	public void setSubject(String subject) {
		this.subject = subject;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}
}
