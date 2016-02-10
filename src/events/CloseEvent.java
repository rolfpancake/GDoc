package events;

import javafx.event.Event;
import javafx.event.EventType;


public final class CloseEvent extends Event
{
	static public final EventType<CloseEvent> CLOSE = new EventType<CloseEvent>("close");
	
	
	public CloseEvent()
	{
		super(CLOSE);
	}
}