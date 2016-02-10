package ui.types;

import java.util.ArrayList;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import events.SelectionEvent;
import ui.Graphics;
import ui.Tab;


public final class ListTabBar extends Region implements EventHandler<Event>
{
	private ArrayList<ListTab> _tabs;
	private Rectangle _leftBorder;
	private Rectangle _rightBorder;
	private ListTab _current;
	private boolean _layout;


	ListTabBar()
	{
		super.setBackground(Graphics.COLOR_BACKGROUND(Graphics.TABS_BACKGROUND_COLOR));

		_leftBorder = new Rectangle(Tab.BORDER_THICKNESS, 100);
		_leftBorder.setFill(Color.gray(50d / 255));
		_rightBorder = new Rectangle(ListTab.RIGHT_MARGIN, 100);
		_rightBorder.setFill(Graphics.BACKGROUND_COLOR);
		_leftBorder.setLayoutX(ListTab.LEFT_MARGIN);
		_rightBorder.setLayoutX(ListTab.LEFT_MARGIN + Tab.BORDER_THICKNESS);
		super.getChildren().addAll(_leftBorder, _rightBorder);

		_tabs = new ArrayList<ListTab>(ListType.values().length);
		ListTab t;
		short y = 20;

		for (ListType i : ListType.values())
		{
			t = new ListTab(i);
			_tabs.add(t);
			super.getChildren().add(0, t);
			t.setLayoutY(y);
			y += ListTab.TAB_HEIGHT / 2;
			t.addEventHandler(MouseEvent.MOUSE_CLICKED, this);
		}

		ChangeListener<Number> h = new ChangeListener<Number>()
		{
			@Override
			public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue)
			{
				_layout = true;
			}
		};

		super.widthProperty().addListener(h);
		super.heightProperty().addListener(h);
		super.addEventHandler(SelectionEvent.SELECTED, this);
		//super.addEventHandler(MouseEvent.MOUSE_CLICKED, this);
		setCurrent(ListType.ALL);
	}


	@Override
	public void handle(Event event)
	{
		if (event.getEventType() == MouseEvent.MOUSE_CLICKED)
		{
			event.consume();
			if (event.getSource() instanceof ListTab) ((ListTab) event.getSource()).select(true); // event
		}
		else if (event.getEventType() == SelectionEvent.SELECTED)
		{
			setCurrent(((ListTab) event.getTarget()).getType());
		}
	}


	public ListType getCurrent() { return _current.getType(); }


	public void setCurrent(ListType type)
	{
		if (type == null) return;
		ListTab t = getTab(type);

		if (_current != null)
		{
			if (type == _current.getType()) return;
			int i = _tabs.indexOf(_current);
			int j = _tabs.indexOf(t);
			super.getChildren().remove(_current);
			super.getChildren().add(_tabs.size() - 1 - _tabs.indexOf(_current), _current);
			_current.select(false);
		}

		_current = t;
		_current.toFront();
		_current.select(true);
	}


	ListTab getTab(ListType type)
	{
		if (type == null) return null;
		for (ListTab i : _tabs) if (type == i.getType()) return i;
		return null;
	}


	@Override
	protected void layoutChildren()
	{
		if (!_layout) return;
		_layout = false;
		_leftBorder.setHeight(super.getHeight());
		_rightBorder.setHeight(super.getHeight());
		super.layoutChildren();
	}
}