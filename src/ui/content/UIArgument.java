package ui.content;

import org.jetbrains.annotations.NotNull;
import javafx.scene.Parent;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import data.Argument;
import data.Type;
import fonts.FontManager;
import fonts.FontWeight;
import main.Strings;
import ui.Graphics;
import ui.UIType;


final class UIArgument extends Parent
{
	static private final byte _NAME_PADDING = 5;

	private UIType _type;
	private Text _name;
	private Text _equal;
	private Text _value;
	private boolean _initialized = false;


	UIArgument(@NotNull Argument argument)
	{
		Type t = argument.getType();
		String n;

		if (t != null)
		{
			_type = new UIType(t);
		}
		else
		{
			n = argument.getAlias();
			if (n == null) throw new IllegalArgumentException();
			if (n.equals(Type.UNTYPED_TOKEN)) _type.setTextSizeOffset(Graphics.DOUBLE);
			_type = new UIType(n);
		}

		n = argument.getName();
		String v = argument.getValue();
		Font f;

		if (n != null && n.equals(Type.REST_NAME))
		{
			f = FontManager.INSTANCE.getFont(FontWeight.Bold, false, Graphics.DOUBLE);
			_name = Graphics.CREATE_TEXT_FIELD(Type.REST_NAME, f);
		}
		else
		{
			f = FontManager.INSTANCE.getFont(true);
			_name = Graphics.CREATE_TEXT_FIELD(n, f);
		}

		super.getChildren().addAll(_type, _name);
		if (v == null) return;

		_equal = Graphics.CREATE_TEXT_FIELD(Strings.EQUAL);
		_value = Graphics.CREATE_TEXT_FIELD(v);

		if (argument.isPseudo())
		{
			_value.setFill(Graphics.PSEUDO_VALUE_COLOR);
			_value.setText(Strings.LESS_THAN + v + Strings.GREATER_THAN);
		}

		super.getChildren().addAll(_equal, _value);
	}


	@Override
	public String toString()
	{
		return _name.getText();
	}


	@Override
	protected void layoutChildren()
	{
		if (_initialized) return;

		_initialized = true;
		super.layoutChildren();
		_name.setLayoutX(_type.getBoundsInLocal().getWidth() + _NAME_PADDING);
		_name.setLayoutY(_type.getBaselineOffset() - _name.getBaselineOffset());

		if (_equal != null)
		{
			_equal.setLayoutX(_name.getBoundsInParent().getMaxX() + Graphics.PADDING);
			_equal.setLayoutY(_name.getLayoutY());
			_value.setLayoutX(_equal.getBoundsInParent().getMaxX() + Graphics.PADDING);
			_value.setLayoutY(_name.getLayoutY());
		}
	}
}