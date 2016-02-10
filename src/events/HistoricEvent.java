package events;

import javafx.event.Event;
import javafx.event.EventTarget;
import javafx.event.EventType;


public final class HistoricEvent extends Event
{
	static public final EventType<HistoricEvent> ANY = new EventType<HistoricEvent>(Event.ANY, "historicEvent");
	static public final EventType<HistoricEvent> CHANGE = new EventType<HistoricEvent>(ANY, "change");
	static public final EventType<HistoricEvent> ADDED = new EventType<HistoricEvent>(ANY, "added");

	
	public HistoricEvent(EventType<HistoricEvent> type) { super(type); }

	public HistoricEvent(EventTarget target, EventType<HistoricEvent> type) { super(target, target, type); }
}