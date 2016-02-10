package ui.content;

import java.util.HashMap;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import javafx.event.EventHandler;
import javafx.scene.layout.Region;
import data.Documentation;
import data.GroupType;
import data.Trunk;
import data.Type;
import events.HistoricEvent;
import events.SelectionEvent;


public final class ClassStack extends Region
{
	static private final byte _CAPACITY = 6;

	static private class _CACHED
	{
		UIClass cls;
		byte use;
		StackFrame frame;
		DocPrototyped highlighted = null;
	}

	private HashMap<Type, _CACHED> _cache = new HashMap<Type, _CACHED>(_CAPACITY);
	private Historic _historic;
	private _CACHED _current;


	ClassStack(@NotNull Type type)
	{
		super();
		_historic = new Historic(type);

		_historic.addEventListener(HistoricEvent.CHANGE, new EventHandler<HistoricEvent>()
		{
			@Override
			public void handle(HistoricEvent event) { _onChange(); }
		});

		super.addEventHandler(SelectionEvent.SELECTED, new EventHandler<SelectionEvent>()
		{
			@Override
			public void handle(SelectionEvent event)
			{
				event.consume();
				_clearHighlight();
			}
		});

		_selectCurrent(type);
	}


	public void frame(@Nullable String member)
	{
		if (member == null) return;

		Trunk k = _current.cls.getTrunk();
		if (k == null) return;
		GroupType g = k.getGroupType(member);
		if (g == null) return;
		DocList l = _current.cls.getGroup(g);
		if (l == null) return;
		DocPrototyped d = l.getPrototyped(member);
		if (d == null) return;
		_clearHighlight();
		_current.highlighted = d;
		_current.cls.selectGroup(g);
		d.highlight(true);
		_current.frame.frame(d);
	}


	@NotNull
	public Historic getHistoric() { return _historic; }


	void add(@Nullable Type type)
	{
		if (type == null) return;

		if (type != _historic.getCurrent())
		{
			_selectCurrent(type);
			_historic.add(type);
		}
		else
		{
			_clearHighlight();
		}
	}


	@Override
	protected void layoutChildren()
	{
		_current.frame.setPrefSize(super.getWidth(), super.getHeight());
		super.layoutChildren();
	}


	private void _clearHighlight()
	{
		if (_current.highlighted != null)
		{
			_current.highlighted.highlight(false);
			_current.highlighted = null;
		}
	}


	private void _onChange() // changement de type courant dans l'historique
	{
		_selectCurrent(_historic.getCurrent());
	}


	private void _pop() // supprime du cache la classe non utilisée la moins demandée
	{
		Type t = null;
		byte m = 0;
		_CACHED d;
		byte u;

		for (Type i : _cache.keySet())
		{
			d = _cache.get(i);

			if (d.cls.getParent() != null) continue;
			u = d.use;

			if (u < m || m == 0)
			{
				m = u;
				t = i;
			}
		}

		if (t != null)
		{
			Documentation.INSTANCE.removeTrunk(t);
			_cache.remove(t);
		}
	}


	private void _selectCurrent(@NotNull Type t) // sélectionne le type en cours sans l'ajouter à l'historique
	{
		_CACHED d = _cache.get(t);

		if (d == null)
		{
			d = new _CACHED();
			d.cls = new UIClass(t, Documentation.INSTANCE.getTrunk(t));
			d.use = 1;
			d.frame = new StackFrame();
			d.frame.setContent(d.cls);
			_cache.put(t, d);
		}
		else if (d == _current)
		{
			return;
		}
		else
		{
			d.use = (byte) Math.max(d.use, d.use + 1);
		}

		if (_current != null) super.getChildren().remove(_current.frame);
		_current = d;
		super.getChildren().add(d.frame);
	}
}