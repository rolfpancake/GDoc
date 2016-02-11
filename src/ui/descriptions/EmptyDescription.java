package ui.descriptions;

import javafx.scene.text.Text;
import fonts.FontManager;
import ui.Graphics;


public final class EmptyDescription extends TextArea
{
	static private final String _EMPTY_TEXT = "no description";
	static private final byte _EMPTY_OFFSET = -2;

	private Text _empty;


	public EmptyDescription() { super(null); }


	@Override
	public boolean isEmpty() { return true; }


	@Override
	protected short computeHeight()
	{
		return (short) _empty.getBoundsInLocal().getHeight();
	}


	@Override
	protected void initDisplay()
	{
		super.initDisplay();
		_empty = Graphics.CREATE_TEXT_FIELD(_EMPTY_TEXT, FontManager.INSTANCE.getFont(true, _EMPTY_OFFSET));
		super.getChildren().add(_empty);
	}
}