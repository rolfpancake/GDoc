package events;

import javafx.event.Event;
import javafx.event.EventType;


public final class NavigationEvent extends Event
{
	static public final EventType<NavigationEvent> ANY = new EventType<NavigationEvent>(Event.ANY, "navigationEvent");
	static public final EventType<NavigationEvent> CLASS = new EventType<NavigationEvent>(ANY, "class");
	static public final EventType<NavigationEvent> GLOBAL = new EventType<NavigationEvent>(ANY, "global");
	static public final EventType<NavigationEvent> LIST = new EventType<NavigationEvent>(ANY, "list");
	static public final EventType<NavigationEvent> SCRIPT = new EventType<NavigationEvent>(ANY, "script");


	public NavigationEvent(EventType<NavigationEvent> type)
	{
		super(type);
	}
}