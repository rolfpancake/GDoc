package ui.classes;

import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.geometry.VPos;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import data.GroupType;
import events.SelectionEvent;
import exceptions.NullArgumentException;
import fonts.FontManager;
import fonts.FontWeight;
import main.Strings;
import ui.Graphics;


final class SelectorButton extends HBox implements EventHandler<MouseEvent>
{
	static final byte MIN_WIDTH = 45;
	static final byte MIN_HEIGHT = 19;
	static final byte UNDEFINED_QUANTITY = -1;

	static private final byte _OFFSET = 1;
	static private final FontWeight _SELECTED_WEIGHT = FontWeight.Bold;
	static private final Color _DISABLED_COLOR = Color.gray(180d / 255);
	static private Font _SELECTED_FONT;
	static private Font _UNSELECTED_FONT;

	private GroupType _type;
	private Text _prefix;
	private Text _suffix;
	private short _quantity = 0;
	private boolean _selected;


	SelectorButton(GroupType type)
	{
		super(Graphics.PADDING);
		if (type == null) throw new NullArgumentException();
		_type = type;

		super.setMinWidth(MIN_WIDTH);
		super.setMinHeight(MIN_HEIGHT);

		String n = Strings.CAPITALIZE(type.toString().replace(Strings.UNDERLINE, Strings.SPACE));
		_prefix = new Text(n);
		_prefix.setTextOrigin(VPos.TOP);
		_UNSELECTED_FONT = FontManager.INSTANCE.getFont(_OFFSET);
		_SELECTED_FONT = FontManager.INSTANCE.getFont(_SELECTED_WEIGHT, _OFFSET);

		_prefix.setFont(_UNSELECTED_FONT);
		_prefix.setFill(Graphics.TEXT_COLOR);

		_suffix = new Text();
		_suffix.setTextOrigin(VPos.TOP);
		_suffix.setFill(Graphics.TEXT_COLOR);
		_suffix.setFont(FontManager.DIGIT_FONT);
		_suffix.setMouseTransparent(true);

		super.setAlignment(Pos.CENTER);
		HBox.setHgrow(_prefix, Priority.NEVER);
		HBox.setHgrow(_suffix, Priority.NEVER);
		super.getChildren().addAll(_prefix, _suffix);

		_prefix.addEventHandler(MouseEvent.MOUSE_ENTERED, this);
		_prefix.addEventHandler(MouseEvent.MOUSE_CLICKED, this);
		_prefix.addEventHandler(MouseEvent.MOUSE_EXITED, this);

		quantify((byte) 0);
	}


	@Override
	public void handle(MouseEvent event)
	{
		event.consume();

		if (event.getEventType() == MouseEvent.MOUSE_CLICKED)
		{
			if (event.getButton() == MouseButton.PRIMARY)
				super.fireEvent(new SelectionEvent(SelectionEvent.GROUP_SELECTED));
		}
		else
			_prefix.setUnderline(!_selected && event.getEventType() == MouseEvent.MOUSE_ENTERED);
	}


	short getQuantity() { return _quantity; }


	GroupType getType() { return _type; }


	void quantify(short quantity)
	{
		if (quantity == 0)
		{
			_suffix.setVisible(false);
			_prefix.setMouseTransparent(true);
			_prefix.setUnderline(false);
			_prefix.setFill(_DISABLED_COLOR);
			unselect();
			_quantity = 0;
		}
		else
		{
			if (quantity > 0) // <0 = indéterminée mais > 0
			{
				_suffix.setText(Strings.LEFT_PARENTHESIS + String.valueOf(quantity) + Strings.RIGHT_PARENTHESIS);
				_suffix.setVisible(true);
				_quantity = quantity;
			}
			else
			{
				_quantity = UNDEFINED_QUANTITY;
				_suffix.setVisible(false);
			}

			_prefix.setFill(Graphics.TEXT_COLOR);
			_prefix.setMouseTransparent(false);
		}
	}


	void select()
	{
		_prefix.setUnderline(false);
		_prefix.setFont(_SELECTED_FONT);
		_selected = true;
	}


	void unselect()
	{
		_prefix.setFont(_UNSELECTED_FONT);
		_selected = false;
	}
}