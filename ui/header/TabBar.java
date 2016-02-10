package ui.header;

import java.util.ArrayList;
import org.jetbrains.annotations.NotNull;
import javafx.event.EventHandler;
import javafx.scene.layout.Region;
import javafx.scene.shape.Rectangle;
import data.Type;
import events.TabEvent;
import main.Strings;
import ui.Graphics;
import ui.Tab;


public final class TabBar extends Region
{
	static public final byte MAX_TABS = 12;

	static private final byte _MIN_OVERLAP = 20;
	static private final byte _MIN_VISIBLE_TAB_WIDTH = 30;
	static private final byte _MIN_TOP_MARGIN = 1;
	static private final byte _MAX_TOP_MARGIN = (byte) (_MIN_TOP_MARGIN + (TypeTab.MAX_HEIGHT - TypeTab.MIN_HEIGHT) * 0.5);

	static final byte MIN_HEIGHT = TypeTab.MIN_HEIGHT + _MIN_TOP_MARGIN;
	static final byte MAX_HEIGHT = TypeTab.MAX_HEIGHT + _MAX_TOP_MARGIN;

	static private final byte _HEIGHT_DELTA = MAX_HEIGHT - MIN_HEIGHT;
	static private final byte _MARGIN_DELTA = _MAX_TOP_MARGIN - _MIN_TOP_MARGIN;

	private ArrayList<TypeTab> _tabs;
	private TypeTab _current;
	private Rectangle _border;


	TabBar(@NotNull Type type)
	{
		super();
		super.setBackground(Graphics.COLOR_BACKGROUND(Graphics.TABS_BACKGROUND_COLOR));
		super.setMinHeight(MIN_HEIGHT);
		super.setMaxHeight(MAX_HEIGHT);

		_tabs = new ArrayList<TypeTab>();
		_border = new Rectangle(100, Tab.BORDER_THICKNESS, Tab.BORDER_COLOR);
		super.getChildren().add(_border);

		super.addEventHandler(TabEvent.ANY, new EventHandler<TabEvent>()
		{
			@Override
			public void handle(TabEvent event) { _onTabEvent(event); }
		});

		addTab(type);
	}


	public TypeTab addTab(@NotNull Type type)
	{
		if (_tabs.size() >= MAX_TABS) throw new IllegalArgumentException();

		TypeTab t = new TypeTab(type);
		_tabs.add(t);
		super.getChildren().add(0, t);
		setCurrent(t);
		_update();
		return t;
	}


	@NotNull
	public TypeTab getCurrent() { return _current; }


	public byte getIndex() { return (byte) _tabs.indexOf(_current); }


	public byte getSize() { return (byte) _tabs.size(); }


	@NotNull
	public TypeTab getTab(byte index)
	{
		return _tabs.get((byte) Math.max(0, Math.min(index, _tabs.size() - 1)));
	}


	public void setCurrent(@NotNull TypeTab tab)
	{
		if (!_tabs.contains(tab) || tab == _current) return;

		if (_current != null) // null durant l'initialisation ou après fermeture d'un onglet
		{
			_current.setClosable(false);
			_current.select(false);
			super.getChildren().remove(_current);
			super.getChildren().add(_tabs.size() - 1 - _tabs.indexOf(_current), _current);
		}

		tab.setClosable(true);
		tab.select(true);
		tab.toFront();
		_current = tab;
	}


	public void setCurrent(byte index)
	{
		setCurrent(_tabs.get(Math.max(0, Math.min(index, _tabs.size() - 1))));
	}


	@Override
	public String toString()
	{
		String s = _tabs.get(0).getType().getName();
		for (byte i = 1; i < _tabs.size(); ++i) s += Strings.SPACE + _tabs.get(i).getType().getName();
		return s;
	}


	@Override
	protected void layoutChildren()
	{
		_border.setLayoutY(super.getHeight() - TypeTab.OVERLAP - Tab.BORDER_THICKNESS);
		_border.setWidth(super.getWidth());

		short x = Graphics.PADDING;
		byte o = _MIN_OVERLAP;
		byte n = (byte) _tabs.size();
		short w = (short) Math.min((super.getWidth() - 2 * Graphics.PADDING + (n - 1) * o) / n, TypeTab.MAX_WIDTH);

		if (w < TypeTab.MIN_WIDTH)
		{
			w = TypeTab.MIN_WIDTH;
			o = (byte) Math.min((w * n - (super.getWidth() - 2 * Graphics.PADDING)) / (n - 1),
								TypeTab.MIN_WIDTH - _MIN_VISIBLE_TAB_WIDTH);
		}

		byte y = (byte) Math.round((super.getHeight() - MIN_HEIGHT) / _HEIGHT_DELTA * _MARGIN_DELTA);
		byte h = (byte) (super.getHeight() - y);

		for (TypeTab i : _tabs)
		{
			i.setLayoutX(x);
			i.setLayoutY(y);
			i.setPrefSize(w, h);
			x += w - o;
		}

		super.layoutChildren();
	}


	private void _onTabEvent(TabEvent e)
	{
		if (!(e.getTarget() instanceof TypeTab)) return;
		TypeTab t = (TypeTab) e.getTarget();

		if (e.getEventType() == TabEvent.SELECT)
		{
			if (t == _current)
			{
				e.consume();
				return;
			}

			setCurrent(t);
		}
		else if (_tabs.size() < 2)
		{
			e.consume();
		}
		else
		{
			byte i = (byte) _tabs.indexOf(t);

			if (i < 0)
			{
				e.consume();
				return;
			}

			if (e.getEventType() == TabEvent.CLOSE)
			{
				super.getChildren().remove(t);
				_tabs.remove(i);
				if (t == _current) _current = null;
				setCurrent(_tabs.get(Math.min(i, _tabs.size() - 1)));
			}
			else
			{
				byte n = (byte) _tabs.size();
				byte k = 0;

				if (e.getEventType() == TabEvent.CLOSE_OTHERS)
				{
					e.tabs = new TypeTab[n - 1];
					for (byte j = 0; j < n; ++j) if (j != i) e.tabs[k++] = _tabs.get(j);
					_removeFirsts();
					_removeLasts();
				}
				else if (e.getEventType() == TabEvent.CLOSE_LEFT)
				{
					if (i == 0)
					{
						e.consume();
						return;
					}

					e.tabs = new TypeTab[i];
					for (byte j = 0; j < i; ++j) e.tabs[j] = _tabs.get(j);
					_removeFirsts();
				}
				else if (e.getEventType() == TabEvent.CLOSE_RIGHT)
				{
					if (i == n - 1)
					{
						e.consume();
						return;
					}

					e.tabs = new TypeTab[n - 1 - i];
					for (byte j = (byte) (i + 1); j < n; ++j) e.tabs[k++] = _tabs.get(j);
					_removeLasts();
				}
			}

			_update();
		}
	}


	private void _removeFirsts()
	{
		byte i = (byte) (_tabs.indexOf(_current) - 1);

		while (i > -1)
		{
			super.getChildren().remove(_tabs.get(i));
			_tabs.remove(i--);
		}
	}


	private void _removeLasts()
	{
		byte i = (byte) (_tabs.indexOf(_current) + 1);
		while (i < _tabs.size()) super.getChildren().remove(_tabs.remove(i));
	}


	private void _update()
	{
		super.setMinWidth(2 * Graphics.PADDING + TypeTab.MIN_WIDTH + (_tabs.size() - 1) * _MIN_VISIBLE_TAB_WIDTH);
		if (_tabs.size() == 1) _tabs.get(0).setClosable(false);
	}
}