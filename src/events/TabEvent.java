package events;

import javafx.event.Event;
import javafx.event.EventType;
import ui.header.TypeTab;


public final class TabEvent extends Event
{
	static public final EventType<TabEvent> ANY = new EventType<TabEvent>(Event.ANY, "tabEvent");
	static public final EventType<TabEvent> SELECT = new EventType<TabEvent>(ANY, "select");
	static public final EventType<TabEvent> CLOSE = new EventType<TabEvent>(ANY, "close");
	static public final EventType<TabEvent> CLOSE_LEFT = new EventType<TabEvent>(ANY, "closeLeft");
	static public final EventType<TabEvent> CLOSE_RIGHT = new EventType<TabEvent>(ANY, "closeRight");
	static public final EventType<TabEvent> CLOSE_OTHERS = new EventType<TabEvent>(ANY, "closeOthers");

	public TypeTab[] tabs;

	public TabEvent(EventType<TabEvent> type) { super(type); }
}