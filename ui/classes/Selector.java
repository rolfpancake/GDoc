package ui.classes;

import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.layout.HBox;
import data.GroupType;
import events.SelectionEvent;
import ui.Graphics;


final class Selector extends HBox implements EventHandler<SelectionEvent>
{
	static GroupType[] TYPES = GroupType.class.getEnumConstants();

	static private byte _SIZE = (byte) TYPES.length;

	private SelectorButton[] _buttons;
	private GroupType _current = null;


	Selector()
	{
		super(4 * Graphics.PADDING);
		super.setMinWidth(350);
		super.setAlignment(Pos.CENTER_LEFT);
		_buttons = new SelectorButton[_SIZE];
		SelectorButton b;

		for (byte i = 0; i < _SIZE; ++i)
		{
			b = new SelectorButton(TYPES[i]);
			_buttons[i] = b;
			super.getChildren().add(b);
			b.addEventHandler(SelectionEvent.GROUP_SELECTED, this);
		}
	}


	@Override
	public void handle(SelectionEvent event)
	{
		SelectorButton b = (SelectorButton) event.getSource();
		if (b.getType() == _current) return;
		if (_current != null) _buttons[_current.ordinal()].unselect();
		b.select();
		_current = b.getType();
	}


	SelectorButton getButton(GroupType type)
	{
		return type == null ? null : _buttons[type.ordinal()];
	}


	GroupType getCurrent() { return _current; }


	void setCurrent(GroupType value)
	{
		if (value == _current) return;
		if (_current != null) _buttons[_current.ordinal()].unselect();

		if (value == null)
			for (SelectorButton i : _buttons) i.unselect();

		else
			_buttons[value.ordinal()].select();

		_current = value;
	}


	void selectFirst()
	{
		byte i;
		for (i = 0; i < _SIZE; ++i) if (_buttons[i].getQuantity() > 0) break;
		setCurrent(i < _SIZE ? TYPES[i] : null);
	}
}
