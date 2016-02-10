package ui.header;

import org.jetbrains.annotations.NotNull;
import javafx.event.EventHandler;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Region;
import javafx.scene.shape.Circle;
import javafx.scene.text.Text;
import data.Category;
import data.Documentation;
import data.Type;
import events.KeyboardManager;
import events.NavigationEvent;
import main.Strings;
import ui.Graphics;


public final class MenuBar extends Region
{
	static final byte MIN_HEIGHT = 28;
	static final byte MAX_HEIGHT = MIN_HEIGHT + Graphics.PADDING;

	static private final byte _MARGIN = 8;
	static private final byte _ICONS_ROW = 2;
	static private final String _SPLIT = "  |  ";
	static private final byte _SEPARATOR_SIZE = 3;
	static private final byte _MENUS = 4;
	static private final MenuType[] _TYPES = MenuType.values();
	static private final NavigationEvent[] _EVENTS = new NavigationEvent[_MENUS];

	private MenuButton[] _buttons;
	private String _version = Strings.EMPTY;
	private Text _category;
	private Circle _separator;
	private byte _current;


	MenuBar()
	{
		super();
		super.setMinHeight(MIN_HEIGHT);
		super.setMaxHeight(MAX_HEIGHT);

		_EVENTS[0] = new NavigationEvent(NavigationEvent.LIST);
		_EVENTS[1] = new NavigationEvent(NavigationEvent.SCRIPT);
		_EVENTS[2] = new NavigationEvent(NavigationEvent.GLOBAL);
		_EVENTS[3] = new NavigationEvent(NavigationEvent.CLASS);

		String v = Documentation.INSTANCE.getVersion();
		if (v != null && !v.isEmpty()) _version = v.replaceFirst("\\.custom_build$", Strings.EMPTY);
		_category = Graphics.CREATE_TEXT_FIELD(_version);

		_buttons = new MenuButton[_MENUS];
		_separator = new Circle((float) _SEPARATOR_SIZE / 2, Graphics.GRAY_8);
		_separator.setMouseTransparent(true);
		super.getChildren().addAll(_category, _separator);
		MenuButton b;

		EventHandler<MouseEvent> h = new EventHandler<MouseEvent>()
		{
			@Override
			public void handle(MouseEvent event) { _onClick(event); }
		};

		for (byte i = 0; i < _MENUS; ++i)
		{
			b = new MenuButton(Graphics.GET_ICON_24(i, _ICONS_ROW));
			_buttons[i] = b;
			super.getChildren().add(b);
			b.addEventHandler(MouseEvent.MOUSE_CLICKED, h);
		}

		_current = 0;
		setCurrent(MenuType.CLASS);
	}


	@NotNull
	public MenuType getCurrent() { return _TYPES[_current]; }


	public void setCurrent(@NotNull MenuType menuType)
	{
		for (byte i = _MENUS - 1; i > -1; --i)
		{
			if (_TYPES[i] == menuType)
			{
				_setCurrent(i);
				return;
			}
		}
	}


	public void setCategory(Category category)
	{
		_category.setText(category != null ? _version + _SPLIT + category.getName() : _version);
	}


	@Override
	protected void layoutChildren()
	{
		super.layoutChildren();
		double h = super.getHeight();
		double x = (super.getWidth() - (_MENUS * (Graphics.ICON_SIZE_24 + Graphics.PADDING) + _SEPARATOR_SIZE)) / 2;
		double y = Math.floor((h - Graphics.ICON_SIZE_24) / 2);

		_category.relocate(_MARGIN, Math.floor((h - _category.getBoundsInLocal().getHeight()) / 2));

		for (byte i = 0; i < _MENUS; ++i)
		{
			if (i == 3)
			{
				_separator.relocate(x, (h - _SEPARATOR_SIZE) / 2);
				x += _SEPARATOR_SIZE + Graphics.PADDING;
			}

			_buttons[i].relocate(x, y);
			x += Graphics.ICON_SIZE_24 + Graphics.PADDING;
		}
	}


	private void _onClick(MouseEvent e)
	{
		e.consume();
		if (e.getButton() != MouseButton.PRIMARY || !KeyboardManager.GET_MANAGER(this).isEmpty()) return;
		MenuButton b = (MenuButton) e.getSource();
		if (b.isSelected()) return;
		byte j = 0;

		for (MenuButton i : _buttons)
		{
			if (i == b)
			{
				_setCurrent(j);
				super.fireEvent(_EVENTS[j]);
			}

			j += 1;
		}
	}


	private void _setCurrent(byte i)
	{
		if (i == _current) return;
		_buttons[_current].select(false);
		_buttons[i].select(true);
		_current = i;
		if (i == 0) setCategory(null);
		else if (i == 1) setCategory(Type.GD_SCRIPT.getCategory());
		else if (i == 2) setCategory(Type.GLOBAL_SCOPE.getCategory());
	}
}