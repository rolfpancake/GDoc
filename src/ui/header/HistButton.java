package ui.header;

import org.jetbrains.annotations.NotNull;
import javafx.scene.layout.Background;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import data.Type;
import fonts.FontManager;
import main.Strings;
import ui.Graphics;


public final class HistButton extends Region
{
	static private final byte _OFFSET = -2;
	static private final byte _MARGINS = Graphics.PADDING;
	static private final Background _UNSELECTED_BACKGROUND = Graphics.COLOR_BACKGROUND(Graphics.GRAY_22);
	static private final Background _SELECTED_BACKGROUND = Graphics.COLOR_BACKGROUND(Color.gray(205d / 255));

	private Type _type;
	private Text _title;


	HistButton(@NotNull Type type)
	{
		super();
		_type = type;
		super.setBackground(_UNSELECTED_BACKGROUND);
		_title = Graphics.CREATE_TEXT_FIELD(type.getName(), FontManager.INSTANCE.getFont(_OFFSET));
		_title.setFill(Graphics.GRAY_8);
		select(true);
		super.getChildren().add(_title);
	}


	@NotNull
	public Type getType() { return _type; }


	boolean isSelected() { return super.getBackground() == _SELECTED_BACKGROUND; }


	void select(boolean select) { super.setBackground(select ? _SELECTED_BACKGROUND : _UNSELECTED_BACKGROUND); }


	void setType(@NotNull Type type)
	{
		if (type == _type) return;
		_type = type;
		super.requestLayout();
	}


	@Override
	protected void layoutChildren()
	{
		if (_type.getName() == null) return;
		_title.setText(Strings.ELLIPSE(_type.getName(), _title.getFont(), super.getWidth() - _MARGINS));
		super.layoutChildren();
		_title.setLayoutX((super.getWidth() - _title.getBoundsInLocal().getWidth()) / 2);
		_title.setLayoutY((super.getHeight() - _title.getBoundsInLocal().getHeight()) / 2);
	}
}