package ui.filters;

import java.util.ArrayList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import javafx.event.EventHandler;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.scene.effect.ColorAdjust;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.Region;
import events.ChangeEvent;
import events.KeyboardManager;
import events.UIEvent;
import main.Launcher;
import ui.Graphics;


/**
 * Un groupe de filtres.
 * Un événement ChangeEvent est distribué uniquement lorsqu'un un clic modifie le bouton en cours.
 */
public final class FilterGroup extends Region
{
	static public final byte MIN_HEIGHT = 24;

	static private final byte _CORNER = FilterButton.CORNER + Graphics.DOUBLE;
	static private final ColorAdjust _DISABLED_EFFECT = new ColorAdjust(0, 0, 0.4, -0.5);

	private ArrayList<FilterButton> _buttons;
	private HPos _position;
	private boolean _reversable;
	private boolean _blankable;
	private boolean _reversed;
	private FilterButton _current;
	private Background _background;

	private EventHandler<MouseEvent> _clickHandler = new EventHandler<MouseEvent>()
	{
		@Override
		public void handle(MouseEvent event) { _onClick(event); }
	};

	private EventHandler<UIEvent> _quantifiedHandler = new EventHandler<UIEvent>()
	{
		@Override
		public void handle(UIEvent event) { _onQuantified(event); }
	};

	private EventHandler<ChangeEvent> _changeHandler = new EventHandler<ChangeEvent>()
	{
			@Override
			public void handle(ChangeEvent event) { _onChange(event); }
	};


	public FilterGroup(boolean reversable, boolean blankable)
	{
		_position = null;
		_reversable = reversable;
		_blankable = reversable || blankable;
		_buttons = new ArrayList<FilterButton>(2);
		super.setMinHeight(MIN_HEIGHT);
	}


	public void addButton(@NotNull FilterButton... button)
	{
		if (button.length == 0) return;

		for (FilterButton i : button)
		{
			if (i == null || _buttons.contains(i)) continue;
			_buttons.add(i);
			i.addEventHandler(UIEvent.QUANTIFIED, _quantifiedHandler);
			super.getChildren().add(i);

			if (i.getQuantity() == 0)
			{
				i.setMouseTransparent(true);
				i.toBack();
			}
			else
			{
				i.setMouseTransparent(false);
				i.addEventHandler(MouseEvent.MOUSE_CLICKED, _clickHandler);
			}
		}

		_buttons.trimToSize();
		_updatePositions();
	}


	@Nullable
	public final FilterButton getButton(char letter)
	{
		char c;

		for (FilterButton i : _buttons)
		{
			c = i.getLetter();
			if (letter == c) return i;
		}

		return null;
	}


	@Nullable
	public final FilterButton getButton(byte index)
	{
		return index > -1 && index < _buttons.size() ? _buttons.get(index) : null;
	}


	@Nullable
	public final FilterButton getCurrent() { return _current; }


	public final byte indexOf(FilterButton button) { return (byte) _buttons.indexOf(button); }


	/**
	 * Récupére l'indice du filtre en cours.
	 *
	 * @return Un entier positif ou -1
	 */
	public final byte indexOf() { return (byte) _buttons.indexOf(_current); }


	public final boolean isReversed() { return _reversed; }


	public final void reset()
	{
		if (_reversed)
		{
			super.setBackground(null);
			_reversed = false;
		}

		if (_blankable && _current != null)
		{
			_current.toggle(false);
			_current = null;
		}
	}


	public final void setCurrent(@Nullable FilterButton button)
	{
		if (button == null)
		{
			if (_blankable && _current != null)
			{
				_current.toggle(false);
				_current = null;
			}
		}
		else if (_buttons.contains(button) && button != _current)
		{
			if (_current != null) _current.toggle(false);
			_current = button;
			_current.toggle(true);
		}
	}


	public final void setCurrent(byte index)
	{
		if (index < 0 || index >= _buttons.size()) return;
		FilterButton b = _buttons.get(index);
		if (b == _current) return;
		if (_current != null) _current.toggle(false);
		_current = b;
		_current.toggle(true);
	}


	public final void setCurrent()
	{
		for (FilterButton i : _buttons)
		{
			if (!i.isMouseTransparent())
			{
				setCurrent(i);
				return;
			}
		}
	}


	void setPosition(@Nullable HPos position)
	{
		if (position == _position && _position != null) return;

		if (_reversable)
		{
			CornerRadii c = CornerRadii.EMPTY;

			if (position == null) c = new CornerRadii(_CORNER);
			else if (position == HPos.LEFT) c = new CornerRadii(_CORNER, 0, 0, _CORNER, false);
			else if (position == HPos.RIGHT) c = new CornerRadii(0, _CORNER, _CORNER, 0, false);
			_background = new Background(new BackgroundFill(Graphics.GRAY_16, c, Insets.EMPTY));
			if (_reversed) super.setBackground(_background);
		}

		_position = position;
		_updatePositions();
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


	private void _onChange(ChangeEvent e)
	{
		e.consume();
		FilterButton b = (FilterButton) e.getSource();

		if (b.isMouseTransparent())
		{
			b.toggle();
			return;
		}

		if (_current != null)
		{
			if (b == _current && !_blankable) b.toggle();
		}
		else if (b.isToggled())
		{
			_current = b;
		}
	}


	private void _onClick(MouseEvent e)
	{
		e.consume();
		if (e.getButton() != MouseButton.PRIMARY) return;
		KeyboardManager m = Launcher.KEYBOARD;

		if (_reversable && e.isAltDown())
		{
			if (!m.isKey(KeyCode.ALT)) return;

			if (_current != null)
			{
				_current.toggle(false);
				_current = null;
			}

			_reversed = !_reversed;
			super.setBackground(_reversed ? _background : null);
		}
		else if (!m.isEmpty())
		{
			return;
		}
		else
		{
			if (_reversed)
			{
				super.setBackground(null);
				_reversed = false;
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


	private void _onQuantified(UIEvent e)
	{
		FilterButton b = (FilterButton) e.getSource();

		if (b.getQuantity() == 0)
		{
			b.removeEventHandler(MouseEvent.MOUSE_CLICKED, _clickHandler);
			b.setMouseTransparent(true);
			b.setEffect(_DISABLED_EFFECT);
			b.toBack();
		}
		else
		{
			b.setEffect(null);
			b.toFront();
			b.setMouseTransparent(false);
			b.addEventHandler(MouseEvent.MOUSE_CLICKED, _clickHandler);
		}
	}


	private void _updatePositions()
	{
		int n = _buttons.size();

		if (n == 1)
		{
			_buttons.get(0).setPosition(_position);
		}
		else
		{
			FilterButton b;

			for (int i = 0; i < n; ++i)
			{
				b = _buttons.get(i);

				if (i == 0)
					b.setPosition(_position == null || _position == HPos.LEFT ? HPos.LEFT : HPos.CENTER);
				else if (i == n - 1)
					b.setPosition(_position == null || _position == HPos.RIGHT ? HPos.RIGHT : HPos.CENTER);
				else
					b.setPosition(HPos.CENTER);
			}
		}

		if (_current == null && !_blankable) setCurrent((byte) 0);
	}
}