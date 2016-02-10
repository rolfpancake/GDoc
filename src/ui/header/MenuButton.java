package ui.header;

import javafx.scene.Parent;
import javafx.scene.image.ImageView;
import javafx.scene.shape.Rectangle;
import exceptions.NullArgumentException;
import ui.Graphics;


class MenuButton extends Parent
{
	private ImageView _icon;
	private Rectangle _border;


	MenuButton(ImageView icon)
	{
		super();
		if (icon == null) throw new NullArgumentException();

		_icon = icon;
		_border = new Rectangle(Graphics.ICON_SIZE_24, Graphics.ICON_SIZE_24);
		_border.setArcWidth(2 * Graphics.CORNER);
		_border.setArcHeight(2 * Graphics.CORNER);
		_border.setFill(Graphics.GRAY_8);
		_border.setMouseTransparent(true);
		_border.setVisible(false);

		super.getChildren().addAll(_border, icon);
		super.setPickOnBounds(true);
	}


	boolean isSelected() { return _border.isVisible(); }


	void select(boolean select)
	{
		if (select == _border.isVisible()) return;
		_border.setVisible(select);
		_icon.setEffect(select ? Graphics.FULL_LIGHT_EFFECT : null);
	}
}