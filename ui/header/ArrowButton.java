package ui.header;

import javafx.scene.Parent;
import javafx.scene.image.ImageView;
import javafx.scene.text.Text;
import fonts.FontManager;
import main.Strings;
import ui.Graphics;


final class ArrowButton extends Parent
{
	static private final byte _MAX_VALUE = 9;
	static private final byte _OFFSET = 2;

	private Text _digit;
	private byte _offset;


	ArrowButton(boolean left)
	{
		super();
		_offset = left ? _OFFSET : -_OFFSET;
		ImageView i = Graphics.GET_ICON_24((byte) 2, (byte) 2);
		if (!left) i.setScaleX(-1);
		_digit = Graphics.CREATE_TEXT_FIELD(Strings.EMPTY, FontManager.DIGIT_FONT);
		super.getChildren().addAll(i, _digit);
		super.setPickOnBounds(true);
	}


	void setValue(byte value)
	{
		value = (byte) Math.max(0, Math.min(value, _MAX_VALUE));
		_digit.setText(value > 0 ? String.valueOf(value) : Strings.EMPTY);
	}


	@Override
	protected void layoutChildren()
	{
		super.layoutChildren();
		_digit.setLayoutX((Graphics.ICON_SIZE_24 - _digit.getBoundsInLocal().getWidth()) / 2 + _offset);
		_digit.setLayoutY((Graphics.ICON_SIZE_24 - _digit.getBoundsInLocal().getHeight()) / 2);
	}
}