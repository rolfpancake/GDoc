package events;

import javafx.event.Event;
import javafx.event.EventType;


public final class UIEvent extends Event
{
	static public final EventType<UIEvent> FOLDING = new EventType<UIEvent>("folding");
	static public final EventType<UIEvent> QUANTIFIED = new EventType<UIEvent>("quantified");

	
	public UIEvent(EventType<UIEvent> type)
	{
		super(type);
	}
}