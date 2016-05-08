package de.hallerweb.enterprise.prioritize.model.event;

public interface PEventProducer {
	public void raiseEvent(String name, Object oldValue, Object newValue);
	public int getId();
	public PObjectType getObjectType();
}
