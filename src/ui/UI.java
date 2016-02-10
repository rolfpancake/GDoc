package ui;

import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.Cursor;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;
import events.SelectionEvent;
import main.Launcher;
import ui.classes.ClassPane;
import ui.types.TypePane;


public final class UI extends Region implements EventHandler<Event>, ChangeListener<Number>, Runnable
{
	static private final short _LIST_MIN_WIDTH = 40;
	static private final short _LIST_MAX_WIDTH = 350;
	static private final short _SEPARATOR_MARGIN = 4;

	private TypePane _typePane;
	private Region _separator;
	private ClassPane _classPane;
	private boolean _resizing = true;
	private boolean _autoWidth = true;


	public UI()
	{
		super();
		super.setBackground(new Background(new BackgroundFill(Graphics.BACKGROUND_COLOR, null, null)));
		super.setPickOnBounds(false);

		_typePane = new TypePane();
		_classPane = new ClassPane(_typePane);

		_separator = new Region();
		BackgroundFill b1 = new BackgroundFill(Color.TRANSPARENT, null, null);
		BackgroundFill b2 = new BackgroundFill(Color.BLACK, null, new Insets(0, _SEPARATOR_MARGIN, 0, _SEPARATOR_MARGIN));
		_separator.setBackground(new Background(b1, b2));
		_separator.setMinWidth(9);
		_separator.setMaxWidth(9);
		super.getChildren().addAll(_classPane, _separator, _typePane);

		_typePane.addEventHandler(SelectionEvent.SELECTED, this);
		_separator.addEventHandler(MouseEvent.MOUSE_ENTERED, this);
		_separator.addEventHandler(MouseEvent.MOUSE_EXITED, this);
		_separator.addEventHandler(MouseEvent.MOUSE_DRAGGED, this);
		_separator.addEventHandler(MouseEvent.MOUSE_PRESSED, this);

		super.widthProperty().addListener(this);
		super.heightProperty().addListener(this);
	}


	@Override
	public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue)
	{
		if (_resizing) return;
		_resizing = true;
		Platform.runLater(this);
	}


	@Override
	public void handle(Event event)
	{
		event.consume();

		if (event instanceof MouseEvent)
		{
			MouseEvent e = (MouseEvent) event;

			if (e.getEventType() == MouseEvent.MOUSE_DRAGGED)
			{
				_typePane.setPrefWidth(Math.max(_LIST_MIN_WIDTH, Math.min(e.getSceneX(), _LIST_MAX_WIDTH)));
				_resizing = true;
			}
			else if (e.getEventType() == MouseEvent.MOUSE_ENTERED)
			{
				super.getScene().setCursor(Cursor.E_RESIZE);
			}
			else if (e.getEventType() == MouseEvent.MOUSE_EXITED)
			{
				if (!e.isPrimaryButtonDown()) super.getScene().setCursor(Cursor.DEFAULT);
			}
			else if (e.getEventType() == MouseEvent.MOUSE_PRESSED)
			{
				_autoWidth = false;
				_separator.removeEventHandler(MouseEvent.MOUSE_PRESSED, this);
			}
		}
		else if (event instanceof SelectionEvent)
		{
			if (event.getTarget() instanceof UIType)
				_classPane.setType(((UIType) event.getTarget()).getType());

			Launcher.INSTANCE.updateMemoryUsage();
		}
	}


	@Override
	public void run()
	{
		if (_resizing) super.requestLayout();
	}


	@Override
	protected void layoutChildren()
	{
		if (!_resizing) return;
		_resizing = false;
		if (_autoWidth) _typePane.fitWidth();
		_typePane.setPrefHeight(super.getHeight());
		_separator.setPrefHeight(super.getHeight());
		_classPane.setPrefHeight(super.getHeight());
		super.layoutChildren();
		_separator.setLayoutX(_typePane.getWidth() - _SEPARATOR_MARGIN);
		_classPane.setLayoutX(_separator.getLayoutX() + _SEPARATOR_MARGIN + 1);
		_classPane.setPrefWidth(super.getWidth() - _classPane.getLayoutX());
		super.layoutChildren();
	}
}