package events;

import java.util.ArrayList;
import java.util.HashMap;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import javafx.event.Event;
import javafx.event.EventDispatchChain;
import javafx.event.EventHandler;
import javafx.event.EventTarget;
import javafx.event.EventType;


public class EventDispatcher<T extends Event> implements EventTarget
{
	/* les références faibles ne permettent pas les écouteurs anonymes, ceux-ci sont supprimés */
	private HashMap<EventType<T>, ArrayList<EventHandler<T>>> _listeners;


	public EventDispatcher()
	{
		_listeners = new HashMap<EventType<T>, ArrayList<EventHandler<T>>>(1, 1);
	}


	public void addEventListener(@NotNull EventType<T> type, @NotNull EventHandler<T> listener)
	{
		ArrayList<EventHandler<T>> l = _listeners.get(type);

		if (l == null)
		{
			l = new ArrayList<EventHandler<T>>(1);
			_listeners.put(type, l);
		}

		if (!l.contains(listener)) l.add(listener);
	}


	@Override
	public EventDispatchChain buildEventDispatchChain(EventDispatchChain tail)
	{
		return null;
	}


	public void clearEventListeners() { _listeners.clear(); }


	@SuppressWarnings("unchecked")
	public void dispatchEvent(@NotNull T event)
	{
		EventType<T> t = (EventType<T>) event.getEventType();
		ArrayList<EventHandler<T>> l = _listeners.get(t);
		if (l == null) return;
		for (EventHandler<T> i : l) i.handle(event);
	}


	public void removeEventListener(@Nullable EventType<T> type, @Nullable EventHandler<T> listener)
	{
		ArrayList<EventHandler<T>> l = _listeners.get(type);
		if (l == null) return;
		l.remove(listener);
		if (l.size() == 0) _listeners.remove(type);
	}
}