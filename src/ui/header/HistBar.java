package ui.header;

import java.util.ArrayList;
import org.jetbrains.annotations.NotNull;
import javafx.event.EventHandler;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Region;
import data.Type;
import events.HistoricEvent;
import main.Launcher;
import ui.content.Historic;


public final class HistBar extends Region
{
	static public final byte MIN_HEIGHT = 11;

	private Historic _historic;
	private ArrayList<HistButton> _buttons;

	private EventHandler<HistoricEvent> _addedHandler = new EventHandler<HistoricEvent>()
	{
		@Override
		public void handle(HistoricEvent event) { _onAdded(); }
	};


	HistBar(@NotNull final Historic historic)
	{
		super();
		super.setMinHeight(MIN_HEIGHT);

		_buttons = new ArrayList<HistButton>(Historic.CAPACITY);
		HistButton b;

		EventHandler<MouseEvent> h = new EventHandler<MouseEvent>()
		{
			@Override
			public void handle(MouseEvent event) { _onClick(event); }
		};

		for (byte i = 0; i < Historic.CAPACITY; ++i)
		{
			b = new HistButton(Type.OBJECT);
			b.addEventHandler(MouseEvent.MOUSE_CLICKED, h);
			_buttons.add(b);
		}

		setHistoric(historic);
	}


	public void setHistoric(@NotNull Historic historic)
	{
		if (historic == _historic) return;
		if (_historic != null) _historic.removeEventListener(HistoricEvent.ADDED, _addedHandler);
		_historic = historic;
		historic.addEventListener(HistoricEvent.ADDED, _addedHandler);
		_update();
	}


	@NotNull
	Historic getHistoric() { return _historic; }


	@Override
	protected void layoutChildren()
	{
		int n = super.getChildren().size();
		double w = super.getWidth() / n;
		double x = 0;
		double h = super.getHeight();
		HistButton b;

		for (int i = 0; i < n; ++i)
		{
			b = _buttons.get(i);
			b.setLayoutX(x);
			x = (i + 1) * w;
			b.setPrefSize(x - b.getLayoutX(), h);
		}

		super.layoutChildren();
	}


	private void _onAdded() // ajout d'un type à l'historique en cours
	{
		_update();
	}


	private void _onClick(MouseEvent e)
	{
		e.consume();
		HistButton b = (HistButton) e.getSource();
		if (b.isSelected() || !Launcher.KEYBOARD.isEmpty()) return;
		_buttons.get(_historic.getIndex()).select(false);
		b.select(true);
		_historic.setIndex((byte) _buttons.indexOf(b));
		super.fireEvent(new HistoricEvent(HistoricEvent.CHANGE));
	}


	private void _update()
	{
		Type[] l = _historic.getList();
		byte n1 = (byte) super.getChildren().size();
		byte n2 = (byte) l.length;
		byte j = _historic.getIndex();
		HistButton b;

		if (n1 > n2) super.getChildren().remove(n2, n1);
		else if (n1 < n2) super.getChildren().addAll(_buttons.subList(n1, n2));

		for (byte i = 0; i < n2; ++i)
		{
			b = _buttons.get(i);
			b.setType(l[i]);
			b.select(i == j);
		}
	}
}