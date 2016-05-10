package de.hallerweb.enterprise.prioritize.model.event;

public abstract class PEventConsumerProducer {
	public abstract void raiseEvent(PObjectType type, int id, String name, String oldValue, String newValue, long lifetime);

	public abstract void consumeEvent(int id, Event evt);

}
