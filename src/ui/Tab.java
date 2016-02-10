package ui;

import javafx.scene.effect.ColorAdjust;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;
import events.SelectionEvent;


abstract public class Tab extends Region
{
	static public final byte BORDER_THICKNESS = Graphics.THIN;
	static public final Color BORDER_COLOR = Graphics.GRAY_5;

	static protected final byte MARGIN = 2;
	static protected final byte FOOT_ARC_MARGIN = 4;
	static protected final byte FOOT_ARC_THICKNESS = 6;
	static protected final byte FOOT_ARC_LENGTH = 8;
	static protected final byte HEAD_ARC_THICKNESS = 6;
	static protected final byte HEAD_ARC_LENGTH = 8;

	static protected final ColorAdjust UNSELECTED_EFFECT = new ColorAdjust(0, 0, 0.5, -0.3);

	private boolean _selected;


	protected Tab()
	{
		super();
		super.setPickOnBounds(false);
	}


	public final boolean isSelected() { return _selected; }


	public void select(boolean select)
	{
		if (select == _selected) return;
		_selected = select;
		super.setEffect(select ? null : UNSELECTED_EFFECT);
		if (select) super.fireEvent(new SelectionEvent(SelectionEvent.SELECTED));
	}
}