package de.hallerweb.enterprise.prioritize.model.event;

public interface PEventConsumer {
	public void consumeEvent(Event evt);
	public int getId();
	public PObjectType getObjectType();
}
