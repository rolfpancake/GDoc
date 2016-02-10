package ui.content;

import java.util.ArrayList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import javafx.event.EventHandler;
import javafx.scene.input.MouseEvent;
import data.Constant;
import data.Group;
import data.GroupType;
import data.Identifier;
import data.Member;
import data.Method;
import data.Signal;
import data.ThemeItem;
import data.Typed;
import events.SelectionEvent;
import ui.VContainer;


final class DocList extends VContainer
{
	static private final byte _H_GAP = 10;
	static private final byte _V_GAP = 8;
	static private final byte _EMPTY_V_GAP = 3;

	private ArrayList<DocPrototyped> _docables;
	private GroupType _type;
	private DisplayMode _displayMode;
	private DocPrototyped _current;

	private EventHandler<MouseEvent> _clickHandler = new EventHandler<MouseEvent>()
	{
		@Override
		public void handle(MouseEvent event) { _onClick(event); }
	};


	DocList(@NotNull Group group, @NotNull DisplayMode displayMode)
	{
		super();
		_type = group.getType();
		_displayMode = displayMode;
		if (group.getSize() == 0) return;
		Identifier[] l = group.getIdentifiers();
		if (l == null) return;

		_docables = new ArrayList<DocPrototyped>(l.length);

		if (_type == GroupType.methods) _fromMethods(l);
		else if (_type == GroupType.signals) _fromSignals(l);
		else if (_type == GroupType.members) _fromTyped(Member.class, l);
		else if (_type == GroupType.constants) _fromTyped(Constant.class, l);
		else _fromTyped(ThemeItem.class, l);

		super.init();
	}


	public void setDisplayMode(@NotNull DisplayMode displayMode)
	{
		if (_docables == null || displayMode == _displayMode) return;
		_displayMode = displayMode;

		if (displayMode == DisplayMode.MULTILINE && _current != null)
		{
			_current.open(false);
			_current = null;
		}

		for (DocPrototyped i : _docables) i.setDisplayMode(displayMode);
		super.requestHeightComputing();
	}


	@Override
	public void setWidth(short width)
	{
		if (_docables == null) return;
		width = (short) Math.max(0, width);
		if (width == super.getWidth()) return;
		for (DocPrototyped i : _docables) i.setWidth(width);
		super.setWidth(width);
	}


	@Override
	public String toString()
	{
		return super.getClass().getSimpleName() + ":" + _type.toString();
	}


	@Nullable
	DocPrototyped getPrototyped(String name)
	{
		String s;

		for (DocPrototyped i : _docables)
		{
			s = i.getData().getName();
			if (s != null && s.equals(name)) return i;
		}

		return null;
	}


	@Override
	protected short computeHeight()
	{
		if (_docables == null) return 0;

		short y = 0;
		byte g = _displayMode == DisplayMode.EMPTY ? _EMPTY_V_GAP : _V_GAP;

		for (DocPrototyped i : _docables)
		{
			i.setLayoutY(y);
			y += i.getHeight() + g;
		}

		return y;
	}


	@Override
	protected boolean isInitialized() { return _type != null; }


	@Override
	protected void initDisplay()
	{
		super.initDisplay();
		if (_docables != null) super.getChildren().addAll(_docables);
	}


	@Override
	protected void initLayout()
	{
		super.initLayout();
		if (_docables != null) for (DocPrototyped i : _docables) i.setDisplayMode(_displayMode);
	}


	private void _fromMethods(Identifier[] l)
	{
		DocMethod o;
		String n, s;
		short k, w;
		short x = 0;

		for (Identifier i : l)
		{
			o = new DocMethod<Method>((Method) i);
			n = o.getData().getName();
			if (n == null) continue;
			k = 0;

			for (DocPrototyped j : _docables) // tri alphabétique
			{
				s = j.getData().getName();
				if (s != null && n.compareTo(s) < 0) break;
				k += 1;
			}

			_docables.add(k, o);
			w = o.getMinLeftOffset();
			if (w > x) x = w;
			o.addEventHandler(MouseEvent.MOUSE_CLICKED, _clickHandler);
		}

		x += _H_GAP;
		for (DocPrototyped i : _docables) i.setLeftOffset(x);
	}


	private void _fromSignals(Identifier[] l)
	{
		DocParameterized o;
		String n, s;
		short k;

		for (Identifier i : l)
		{
			o = new DocParameterized<Signal>((Signal) i);
			n = o.getData().getName();
			if (n == null) continue;
			k = 0;

			for (DocPrototyped j : _docables) // tri alphabétique
			{
				s = j.getData().getName();
				if (s != null && n.compareTo(s) < 0) break;
				k += 1;
			}

			_docables.add(k, o);
			o.addEventHandler(MouseEvent.MOUSE_CLICKED, _clickHandler);
		}
	}


	@SuppressWarnings("unchecked")
	private <T extends Typed> void _fromTyped(Class<T> t, Identifier[] l)
	{
		DocTyped<T> o;
		String n, s;
		short k, w;
		short x = 0;

		for (Identifier i : l)
		{
			o = new DocTyped<T>((T) i);
			n = o.getData().getName();
			if (n == null) continue;
			k = 0;

			for (DocPrototyped j : _docables) // tri alphabétique
			{
				s = j.getData().getName();
				if (s != null && n.compareTo(s) < 0) break;
				k += 1;
			}

			_docables.add(k, o);
			w = o.getMinLeftOffset();
			if (w > x) x = w;
			o.addEventHandler(MouseEvent.MOUSE_CLICKED, _clickHandler);
		}

		x += _H_GAP;
		for (DocPrototyped j : _docables) j.setLeftOffset(x);
	}


	private void _onClick(MouseEvent e)
	{
		DocPrototyped o = (DocPrototyped) e.getSource();

		if (o == _current)
		{
			o.open(false);
			_current = null;
		}
		else
		{
			if (_current != null) _current.open(false);
			_current = o;
			o.open(true);
		}

		if (_displayMode == DisplayMode.MULTILINE) return;
		super.requestHeightComputing();
		super.fireEvent(new SelectionEvent(SelectionEvent.SELECTED));
	}
}