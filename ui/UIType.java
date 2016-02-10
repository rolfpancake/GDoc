package ui;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import javafx.event.EventHandler;
import javafx.geometry.VPos;
import javafx.scene.Cursor;
import javafx.scene.Group;
import javafx.scene.input.MouseEvent;
import javafx.scene.text.Text;
import data.Documentation;
import data.Trunk;
import data.Type;
import exceptions.EmptyArgumentException;
import fonts.FontManager;
import fonts.FontWeight;
import main.Strings;

//TODO alignement du namespace
//TODO créer une super classe UIReference

/**
 * Un texte cliquable qui représente un type ou un membre.
 */
public final class UIType extends Group
{
	static private final byte _OFFSET = 1;
	static private final byte _H_PADDING = 1;

	private Type _type;
	private Text _label;
	private Text _member;
	private Text _namespace;
	private Text _parenthesis;


	public UIType(@NotNull Type type)
	{
		super();

		if (type == Type.UNDEFINED)
		{
			_initUndefined(type.getName());
			return;
		}

		_type = type;
		_init(false);
	}


	public UIType(@NotNull Type type, @Nullable String member, boolean hideType)
	{
		super();

		if (type == Type.UNDEFINED)
		{
			_initUndefined(type.getName());
			return;
		}

		_type = type;
		member = Strings.CLEAN(member);

		if (member != null)
		{
			if (!hideType)
			{
				_namespace = Graphics.CREATE_TEXT_FIELD(Strings.NAMESPACE);
				_namespace.setFont(FontManager.INSTANCE.getFont(FontWeight.ExtraBold, (byte) (_OFFSET + 1)));
				_namespace.setFill(Graphics.BLUE);
				super.getChildren().add(_namespace);
			}

			Trunk t = Documentation.INSTANCE.getTrunk(type);

			if (t != null && t.isMethod(member))
			{
				_parenthesis = Graphics.CREATE_TEXT_FIELD(Strings.LEFT_PARENTHESIS + Strings.RIGHT_PARENTHESIS);
				_parenthesis.setFill(Graphics.BLUE);
				super.getChildren().add(_parenthesis);
			}

			_member = Graphics.CREATE_TEXT_FIELD(member);
			_member.setFont(FontManager.INSTANCE.getFont(FontWeight.SemiBold, _OFFSET));
			_member.setFill(Graphics.BLUE);
			super.getChildren().add(_member);
		}

		_init(hideType);
	}


	public UIType(@NotNull String name)
	{
		super();
		if (name.isEmpty()) throw new EmptyArgumentException();
		_initUndefined(name);
	}


	@Override
	public String toString() { return "{" + super.getClass().getSimpleName() + ":" + _label.getText() + "}"; }


	public String getMember() { return _member != null ? _member.getText() : null; }


	public Type getType() { return _type; }


	public void setTextSizeOffset(byte offset)
	{
		System.out.println(offset);
		if (_label != null) _label.setFont(FontManager.INSTANCE.getFont(FontWeight.SemiBold, offset));
		if (_member != null) _member.setFont(FontManager.INSTANCE.getFont(FontWeight.SemiBold, offset));
	}


	@Override
	protected void layoutChildren()
	{
		super.layoutChildren();
		double x = 0;

		if (_label != null) x += _label.getBoundsInLocal().getWidth() + _H_PADDING;

		if (_namespace != null)
		{
			_namespace.setLayoutX(x);
			x += _namespace.getBoundsInLocal().getWidth() + _H_PADDING;
		}

		if (_member != null)
		{
			_member.setLayoutX(x);
			x += _member.getBoundsInLocal().getWidth() + _H_PADDING;
			if (_parenthesis != null) _parenthesis.setLayoutX(x);
		}
	}

	private void _onMouse(MouseEvent e)
	{
		e.consume();

		if (e.getEventType() == MouseEvent.MOUSE_ENTERED)
		{
			super.getScene().setCursor(Cursor.HAND);
			if (_label != null) _label.setUnderline(true);
			if (_member != null) _member.setUnderline(true);
		}
		else
		{
			if (_label != null) _label.setUnderline(false);
			if (_member != null) _member.setUnderline(false);
			super.getScene().setCursor(Cursor.DEFAULT);
		}
	}


	private void _init(boolean h)
	{
		if (!h)
		{
			_label = Graphics.CREATE_TEXT_FIELD(_type.getName());
			_label.setFont(FontManager.INSTANCE.getFont(FontWeight.SemiBold, _OFFSET));
			_label.setFill(Graphics.BLUE);
			_label.setMouseTransparent(true);
			super.getChildren().add(_label);
		}

		super.setPickOnBounds(true);

		EventHandler<MouseEvent> f = new EventHandler<MouseEvent>()
		{
			@Override
			public void handle(MouseEvent event) { _onMouse(event); }
		};

		super.addEventHandler(MouseEvent.MOUSE_ENTERED, f);
		super.addEventHandler(MouseEvent.MOUSE_EXITED, f);

		layoutChildren();
	}


	private void _initUndefined(String n)
	{
		_label = new Text();
		_label.setTextOrigin(VPos.TOP);
		_label.setFont(FontManager.INSTANCE.getFont(FontWeight.SemiBold));

		if (n.equals(Type.UNTYPED_NAME))
		{
			_label.setText(Type.UNTYPED_TOKEN);
			_label.setFill(Graphics.TEXT_COLOR);
		}
		else
		{
			_label.setText(n);
			_label.setFill(Graphics.INVALID_ID);
		}

		super.getChildren().add(_label);
		super.setMouseTransparent(true);
	}
}
