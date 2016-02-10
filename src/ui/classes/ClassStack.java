package ui.classes;

import java.util.HashMap;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.EventHandler;
import javafx.scene.Parent;
import data.Documentation;
import data.GroupType;
import data.Trunk;
import data.Type;
import events.HistoricEvent;
import main.Pair;
import ui.header.TabBar;

//TODO une frame par classe pour éviter les resets de scrolling lors des changements d'historique

public final class ClassStack extends StackFrame
{
	static private final byte _CAPACITY = (byte) (TabBar.MAX_TABS * 1.5);
	static private final HashMap<Type, Pair<UIClass, Byte>> _CACHE = new HashMap<Type, Pair<UIClass, Byte>>(_CAPACITY);

	private Historic _historic;
	private UIClass _currentClass;
	private UIPrototype _highlighted;


	ClassStack(@NotNull Type type)
	{
		super();
		_historic = new Historic(type);

		_historic.addEventListener(HistoricEvent.CHANGE, new EventHandler<HistoricEvent>()
		{
			@Override
			public void handle(HistoricEvent event) { _onChange(); }
		});

		/* si la classe en cours est affichée dans une autre occurrence */
		super.parentProperty().addListener(new ChangeListener<Parent>()
		{
			@Override
			public void changed(ObservableValue<? extends Parent> observable, Parent oldValue, Parent newValue)
			{
				_onParenting(newValue);
			}
		});

		_selectCurrent(type);
	}


	static private UIClass _FETCH(@NotNull Type t) // mets une classe en cache et la récupére
	{
		Pair<UIClass, Byte> d = _CACHE.get(t);
		if (d != null) return _USE(d);

		if (_CACHE.containsKey(t)) return _CACHE.get(t).getKey();
		if (_CACHE.size() >= _CAPACITY) _POP();
		d = new Pair<UIClass, Byte>(new UIClass(t, Documentation.INSTANCE.getTrunk(t)), (byte) 1);
		_CACHE.put(t, d);
		return _USE(d);
	}


	static private void _POP() // supprime du cache la classe non utilisée la moins demandée
	{
		Type t = null;
		byte m = 0;
		byte v;
		Pair<UIClass, Byte> d;

		for (Type i : _CACHE.keySet())
		{
			d = _CACHE.get(i);
			if (d.getKey().getParent() != null) continue;
			v = d.getValue();

			if (v < m || m == 0)
			{
				m = v;
				t = i;
			}
		}

		if (t != null)
		{
			Documentation.INSTANCE.removeTrunk(t);
			_CACHE.remove(t);
		}
	}


	static private UIClass _USE(@NotNull Pair<UIClass, Byte> u) // incrémente le compteur d'utilisation d'un type
	{
		byte v = u.getValue();
		u.setValue((byte) Math.max(v, v + 1));
		return u.getKey();
	}


	@NotNull
	public Historic getHistoric() { return _historic; }


	void add(@Nullable Type type, @Nullable String member)
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

		if (member == null) return;
		Trunk k = _currentClass.getTrunk();
		if (k == null) return;
		GroupType g = k.getGroupType(member);
		if (g == null) return;
		DocList l = _currentClass.getGroup(g);
		if (l == null) return;
		DocPrototyped d = l.getPrototyped(member);
		if (d == null) return;
		_highlighted = d.getPrototype();
		_currentClass.selectGroup(g);
		_highlighted.highlight(true);
		super.frame(d);
	}


	private void _clearHighlight()
	{
		if (_highlighted != null)
		{
			_highlighted.highlight(false);
			_highlighted = null;
		}
	}


	private void _onParenting(Parent p)
	{
		if (p != null && _currentClass != null) super.setContent(_currentClass);
	}


	private void _onChange() // changement de type courant dans l'historique
	{
		_selectCurrent(_historic.getCurrent());
	}


	private void _selectCurrent(@NotNull Type t) // sélectionne le type en cours sans l'ajouter à l'historique
	{
		_clearHighlight();
		_currentClass = _FETCH(t);
		super.setContent(_currentClass);
	}
}