package ui.descriptions;

import javafx.scene.Parent;
import javafx.scene.image.ImageView;
import javafx.scene.transform.Scale;
import ui.Graphics;


final class Folder extends Parent
{
	static private final byte _COLUMN = 1;
	static private final byte _ROW = 0;
	static private final byte _BASELINE_OFFSET = 11;
	static private final Scale _TOGGLED_TRANSFORM = new Scale(1, -1);

	private ImageView _icon;


	Folder()
	{
		super();
		_icon = Graphics.GET_ICON_16(_COLUMN, _ROW);
		_icon.setPickOnBounds(true);
		_icon.setSmooth(true);
		super.getChildren().add(_icon);
	}


	@Override
	public double getBaselineOffset() { return _BASELINE_OFFSET; }


	void toggle(boolean toggle)
	{
		if (!toggle) _icon.getTransforms().clear();
		else if (_icon.getTransforms().size() == 0) _icon.getTransforms().add(_TOGGLED_TRANSFORM);
	}


	/**
	 * Bascule et renvoie la nouvelle valeur.
	 */
	boolean toggle()
	{
		if (_icon.getTransforms().size() == 0)
		{
			_icon.getTransforms().add(_TOGGLED_TRANSFORM);
			return true;
		}

		_icon.getTransforms().clear();
		return false;
	}


	boolean isToggled() { return _icon.getTransforms().size() > 0; }
}