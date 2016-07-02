package de.hallerweb.enterprise.prioritize.model.event;

import de.hallerweb.enterprise.prioritize.model.PObject;

public abstract class PEventConsumerProducer {
	public abstract void raiseEvent(PObject source, String name, String oldValue, String newValue, long lifetime);

	public abstract void consumeEvent(PObject destination, Event evt);

}
