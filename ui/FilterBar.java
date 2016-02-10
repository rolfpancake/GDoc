package ui;

import java.util.ArrayList;
import org.jetbrains.annotations.Nullable;
import javafx.event.EventHandler;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.Region;
import events.ChangeEvent;
import events.KeyboardManager;
import exceptions.NullArgumentException;


public final class FilterBar extends Region
{
	static private final CornerRadii _CORNERS = new CornerRadii(FilterButton.CORNER + Graphics.DOUBLE);
	static private Background _DEFAULT = new Background(new BackgroundFill(Graphics.GRAY_22, _CORNERS, Insets.EMPTY));
	static private Background _REVERSED = new Background(new BackgroundFill(Graphics.GRAY_16, _CORNERS, Insets.EMPTY));

	private ArrayList<FilterButton> _buttons;
	private boolean _reversable;
	private boolean _blankable;
	private boolean _reversed;
	private FilterButton _current;


	public FilterBar(boolean reversable, boolean blankable, String... names)
	{
		super();
		if (names == null) throw new NullArgumentException();
		_reversable = reversable;
		_blankable = reversable || blankable;
		_init((Object[]) names);
		if (!blankable) setCurrent((byte) 0);
	}


	public FilterBar(boolean reversable, boolean blankable, ImageView... icons)
	{
		if (icons == null) throw new NullArgumentException();
		_reversable = reversable;
		_blankable = reversable || blankable;
		_init((Object[]) icons);
		if (!blankable) setCurrent((byte) 0);
	}


	@Nullable
	public FilterButton getButton(String name)
	{
		String n;

		for (FilterButton i : _buttons)
		{
			n = i.getName();
			if (n != null && n.equals(name)) return i;
		}

		return null;
	}


	@Nullable
	public FilterButton getButton(byte index)
	{
		return index > -1 && index < _buttons.size() ? _buttons.get(index) : null;
	}


	@Nullable
	public FilterButton getCurrent() { return _current; }


	public byte indexOf(FilterButton button) { return (byte) _buttons.indexOf(button); }


	/**
	 * Récupére l'indice du filtre en cours.
	 *
	 * @return Un entier positif ou -1
	 */
	public byte indexof() { return (byte) _buttons.indexOf(_current); }


	public boolean isReversed() { return _reversed; }


	public void reset()
	{
		if (_current != null)
		{
			_current.toggle(false);
			_current = null;
		}

		if (_reversed)
		{
			_reversed = false;
			super.setBackground(_DEFAULT);
		}
	}


	public void setCurrent(FilterButton button)
	{
		if (!_buttons.contains(button) || button == _current) return;
		if (_current != null) _current.toggle(false);
		_current = button;
		_current.toggle(true);
	}


	public void setCurrent(byte index)
	{
		if (index < 0 || index >= _buttons.size()) return;
		FilterButton b = _buttons.get(index);
		if (b == _current) return;
		if (_current != null) _current.toggle(false);
		_current = b;
		_current.toggle(true);
	}


	@Override
	protected void layoutChildren()
	{
		short x = Graphics.DOUBLE;
		double w = (super.getWidth() - 2 * Graphics.DOUBLE) / _buttons.size();
		double h = super.getHeight() - 2 * Graphics.DOUBLE;
		FilterButton b;
		byte j = (byte) (_buttons.size() - 1);
		short r;

		for (int i = 0; i < j; ++i)
		{
			b = _buttons.get(i);
			b.setLayoutX(x);
			b.setLayoutY(Graphics.DOUBLE);
			r = (short) Math.round(Graphics.DOUBLE + (i + 1) * w);
			b.setPrefWidth(r - x);
			b.setPrefHeight(h);
			x = (short) (r - Graphics.THIN);
		}

		b = _buttons.get(j);
		b.setLayoutX(x);
		b.setLayoutY(Graphics.DOUBLE);
		b.setPrefWidth(super.getWidth() - Graphics.DOUBLE - x);
		b.setPrefHeight(h);

		super.layoutChildren();
	}


	private void _init(Object... l)
	{
		byte n = (byte) l.length;
		if (n == 0) throw new IllegalArgumentException();

		super.setMinHeight(24);
		super.setBackground(_DEFAULT);
		_buttons = new ArrayList<FilterButton>(n);

		boolean s = l[0] instanceof String;
		FilterButton b;
		HPos p;

		EventHandler<MouseEvent> mh = new EventHandler<MouseEvent>()
		{
			@Override
			public void handle(MouseEvent event) { _onClick(event); }
		};

		EventHandler<ChangeEvent> ch = new EventHandler<ChangeEvent>()
		{
			@Override
			public void handle(ChangeEvent event) { _onChange(event); }
		};

		for (int i = 0; i < n; ++i)
		{
			p = i == 0 ? HPos.LEFT : (i == n - 1 ? HPos.RIGHT : HPos.CENTER);
			b = s ? new FilterButton(p, (String) l[i]) : new FilterButton(p, (ImageView) l[i]);
			_buttons.add(b);
			super.getChildren().add(b);
			b.addEventHandler(MouseEvent.MOUSE_CLICKED, mh);
			b.addEventHandler(ChangeEvent.CHANGE, ch);
		}
	}


	private void _onChange(ChangeEvent e)
	{
		e.consume();
		FilterButton b = (FilterButton) e.getSource();

		if (b.isToggled())
		{
			if (_current != null && b != _current) _current.toggle(false);
			_current = b;
		}
		else
		{
			_current = null;
		}
	}


	private void _onClick(MouseEvent e)
	{
		e.consume();
		KeyboardManager m = KeyboardManager.GET_MANAGER(this);
		if (m == null || e.getButton() != MouseButton.PRIMARY) return;

		if (_reversable && e.isAltDown())
		{
			if (!m.isKey(KeyCode.ALT)) return;

			if (_current != null)
			{
				_current.toggle(false);
				_current = null;
			}

			_reversed = !_reversed;
			super.setBackground(_reversed ? _REVERSED : _DEFAULT);
		}
		else if (!m.isEmpty())
		{
			return;
		}
		else
		{
			if (_reversed)
			{
				_reversed = false;
				super.setBackground(_DEFAULT);
			}

			FilterButton b = (FilterButton) e.getTarget();

			if (!b.isToggled())
			{
				if (_current != null) _current.toggle(false); // _current != b
				_current = b;
			}
			else // _current == b
			{
				if (!_blankable) return;
				_current = null;
			}

			b.toggle();
		}

		super.fireEvent(new ChangeEvent());
	}
}