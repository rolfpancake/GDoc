package events;

import javafx.event.Event;
import javafx.event.EventType;


public final class SelectionEvent extends Event
{
	static public final EventType<SelectionEvent> SELECTED = new EventType<SelectionEvent>("selected");
	static public final EventType<SelectionEvent> GROUP_SELECTED = new EventType<SelectionEvent>("groupSelected");


	public SelectionEvent(EventType<SelectionEvent> type)
	{
		super(type);
	}
}