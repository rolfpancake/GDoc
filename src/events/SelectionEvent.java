package events;

import javafx.event.Event;
import javafx.event.EventType;


public final class SelectionEvent extends Event
{
	static public final EventType<SelectionEvent> SELECTED = new EventType<SelectionEvent>("selected");


	public SelectionEvent(EventType<SelectionEvent> type)
	{
		super(type);
	}
}