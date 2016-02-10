package events;

import javafx.event.Event;
import javafx.event.EventType;


public final class ChangeEvent extends Event
{
	static public final EventType<ChangeEvent> CHANGE = new EventType<ChangeEvent>("change");

	public ChangeEvent() { super(CHANGE); }
}