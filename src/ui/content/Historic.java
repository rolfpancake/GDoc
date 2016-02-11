package ui.content;

import java.util.ArrayList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import data.Type;
import events.EventDispatcher;
import events.HistoricEvent;


public final class Historic extends EventDispatcher<HistoricEvent>
{
	static public final byte CAPACITY = 6;

	private ArrayList<Type> _types;
	private byte _index;
	private Type _current;


	Historic(@NotNull Type type)
	{
		super();
		_types = new ArrayList<Type>(CAPACITY);
		_types.add(type);
		_current = type;
		_index = 0;
	}


	@NotNull
	public Type getCurrent() { return _current; }


	public byte getIndex() { return _index; }


	public Type[] getList() { return _types.toArray(new Type[_types.size()]); }


	public byte getSize() { return (byte) _types.size(); }


	/**
	 * Définit l'indice courant. S'il est égal à l'indice en cours rien ne se passe.
	 * Sinon un événement HistoricEvent::CHANGE est distribué.
	 */
	public void setIndex(byte index)
	{
		index = (byte) Math.max(0, Math.min(index, _types.size() - 1));
		if (index == _index) return;
		_index = index;
		_current = _types.get(index);
		super.dispatchEvent(new HistoricEvent(HistoricEvent.CHANGE));
	}


	/**
	 * Ajoute un type à la suite et le sélectionne. S'il est égal au type en cours rien ne se passe. Si le type en
	 * cours n'est pas le dernier tous les types suivants sont remplacés par le type ajouté. Si le nombre de types
	 * a atteint le maximum le premier type est supprimé.
	 * Si le type est ajouté un événement HistoricEvent::ADDED est distribué.
	 */
	void add(@Nullable Type type)
	{
		if (type == null || _types.get(_index) == type) return;
		int n = _types.size();

		if (_index < n - 1)
		{
			for (int i = n - 1; i > _index; --i) _types.remove(i);
			n = _index + 1;
		}

		if (n >= CAPACITY) // _index = CAPACITY - 1
		{
			_types.remove(0);
			_index -= 1;
		}

		_types.add(type);
		_index += 1;
		_current = type;

		super.dispatchEvent(new HistoricEvent(this, HistoricEvent.ADDED));
	}
}