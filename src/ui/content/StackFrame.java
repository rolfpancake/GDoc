package ui.content;

import javafx.event.EventHandler;
import javafx.geometry.Insets;
import events.ChangeEvent;
import ui.Frame;
import static main.Debug.trace;


class StackFrame extends Frame
{
	static protected final Insets INSETS = new Insets(15, 7, 55, 7);
	static protected final short MIN_WIDTH = 350;


	StackFrame()
	{
		super(INSETS);
		super.setMinWidth(MIN_WIDTH);

		super.addEventHandler(ChangeEvent.CHANGE, new EventHandler<ChangeEvent>()
		{
			@Override
			public void handle(ChangeEvent event) { _onSelected(event); }
		});
	}


	private void _onSelected(ChangeEvent e)
	{
		e.consume();
		super.requestLayout();
		super.resetScrolling(true, false);
	}
}