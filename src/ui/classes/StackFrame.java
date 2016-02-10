package ui.classes;

import javafx.event.EventHandler;
import javafx.geometry.Insets;
import events.SelectionEvent;
import ui.Frame;


abstract class StackFrame extends Frame
{
	static protected final Insets INSETS = new Insets(15, 7, 55, 7);
	static protected final short MIN_WIDTH = 350;


	protected StackFrame()
	{
		super(INSETS);
		super.setMinWidth(MIN_WIDTH);

		super.addEventHandler(SelectionEvent.GROUP_SELECTED, new EventHandler<SelectionEvent>()
		{
			@Override
			public void handle(SelectionEvent event) { _onSelected(event); }
		});
	}


	private void _onSelected(SelectionEvent e)
	{
		e.consume();
		super.requestLayout();
		super.resetScrolling(true, false);
	}
}