package ui.classes;


import java.util.HashMap;
import org.jetbrains.annotations.NotNull;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.EventHandler;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Region;
import data.Category;
import data.Documentation;
import data.Type;
import events.HistoricEvent;
import events.KeyboardManager;
import events.NavigationEvent;
import events.TabEvent;
import main.Strings;
import settings.Property;
import settings.Section;
import settings.Settings;
import ui.Frame;
import ui.UIType;
import ui.header.Header;
import ui.header.MenuType;
import ui.header.TabBar;
import ui.header.TypeTab;
import ui.types.TypePane;
import static main.Debug.trace;


public final class ClassPane extends Region
{
	static private final byte _HEADER_HEIGHT = 90;
	static private final String _OPENED_TABS = "openedTabs";
	static private final String _CURRENT_TAB = "currentTab";

	private Header _header;
	private HashMap<TypeTab, ClassStack> _stacks;
	private GlobalStack _globalStack;
	private ClassStack _classStack;
	private Frame _currentFrame;
	private boolean _layout;

	private EventHandler<MouseEvent> _preClickHandler = new EventHandler<MouseEvent>()
	{
		@Override
		public void handle(MouseEvent event) { _onPreClick(event); }
	};

	private EventHandler<HistoricEvent> _histChangeHandler = new EventHandler<HistoricEvent>()
	{
		@Override
		public void handle(HistoricEvent event) { _onHistChange(); }
	};

	private ChangeListener<Number> _sizeChangeHandler = new ChangeListener<Number>()
	{
		@Override
		public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue)
		{
			_layout = true;
		}
	};


	public ClassPane(@NotNull TypePane _typePane)
	{
		super();

		_stacks = new HashMap<TypeTab, ClassStack>();
		_globalStack = new GlobalStack();
		_initTabs();
		_layout = false;
		super.getChildren().add(_header);

		super.widthProperty().addListener(_sizeChangeHandler);
		super.heightProperty().addListener(_sizeChangeHandler);

		_header.getMenuBar().addEventHandler(NavigationEvent.ANY, new EventHandler<NavigationEvent>()
		{
			@Override
			public void handle(NavigationEvent event) { _onNavigation(event); }
		});

		super.addEventHandler(TabEvent.ANY, new EventHandler<TabEvent>()
		{
			@Override
			public void handle(TabEvent event) { _onTabEvent(event); }
		});

		super.addEventFilter(MouseEvent.MOUSE_CLICKED, _preClickHandler);
		_typePane.addEventFilter(MouseEvent.MOUSE_CLICKED, _preClickHandler);
		_register();
		_showClass();
		_updateSettings();
	}


	public void setType(Type type)
	{
		if (type == null) return;

		if (type == Type.GD_SCRIPT) _showGlobals(MenuType.SCRIPT);
		else if (type == Type.GLOBAL_SCOPE) _showGlobals(MenuType.GLOBAL);
		else _showClass(); // pas d'événement en cas d'égalité
	}


	@Override
	protected void layoutChildren()
	{
		if (!_layout) return;
		_layout = false;

		_header.setPrefWidth(super.getWidth());
		_header.setPrefHeight(_HEADER_HEIGHT);

		_classStack.setPrefWidth(super.getWidth());
		_classStack.setLayoutY(_HEADER_HEIGHT);
		_classStack.setPrefHeight(super.getHeight() - _classStack.getLayoutY());

		_globalStack.setPrefWidth(super.getWidth());
		_globalStack.setLayoutY(_HEADER_HEIGHT);
		_globalStack.setPrefHeight(super.getHeight() - _globalStack.getLayoutY());

		super.layoutChildren();
	}

	private void _initTabs()
	{
		Section e = Settings.INSTANCE.getSection(Strings.UI);
		Property p = e.getProperty(_OPENED_TABS);
		String[] o; // onglets ouverts
		Type t;
		ClassStack s;
		Documentation d = Documentation.INSTANCE;

		if (!p.getValue().isEmpty())
		{
			o = p.getValue().split(Strings.SPACE);
		}
		else
		{
			t = d.getFirstType();
			if (t == null) t = Type.ARRAY;
			o = new String[1];
			o[0] = t.getName();
		}

		for (String i : o)
		{
			t = d.getType(i.trim());
			if (t == null) t = d.getWideType(i);
			if (t == null) continue;
			s = new ClassStack(t);

			if (_classStack == null)
			{
				_classStack = s;
				_header = new Header(s.getHistoric());
				_stacks.put(_header.getTabBar().getCurrent(), s);
			}
			else
			{
				_stacks.put(_header.getTabBar().addTab(t), s);
			}
		}

		p = e.getProperty(_CURRENT_TAB);
		p.setDefault(0, true);
		_header.getTabBar().setCurrent(Byte.valueOf(p.getValue()));
		_classStack = _stacks.get(_header.getTabBar().getCurrent());

		if (_stacks.size() == 0)
		{
			t = d.getFirstType();
			if (t == null) t = Type.ARRAY;
			_classStack = new ClassStack(t);
			_header = new Header(_classStack.getHistoric());
			_stacks.put(_header.getTabBar().addTab(_classStack.getHistoric().getCurrent()), _classStack);
		}
	}

	private void _onNavigation(NavigationEvent e)
	{
		if (e.getEventType() == NavigationEvent.CLASS) _showClass();
		else if (e.getEventType() == NavigationEvent.SCRIPT) _showGlobals(MenuType.SCRIPT);
		else if (e.getEventType() == NavigationEvent.GLOBAL) _showGlobals(MenuType.GLOBAL);
		else if (e.getEventType() == NavigationEvent.LIST) _showGlobals(MenuType.LIST);
	}


	private void _onHistChange()
	{
		_updateTab();
		_showClass();
	}


	private void _onPreClick(MouseEvent e)
	{
		if (!(e.getTarget() instanceof UIType) || e.getButton() != MouseButton.PRIMARY) return;

		KeyboardManager k = KeyboardManager.GET_MANAGER(this);
		UIType u = (UIType) e.getTarget();
		Type t = u.getType();

		if (t == Type.GD_SCRIPT)
		{
			if (!k.isEmpty()) return;
			e.consume();
			_showGlobals(MenuType.SCRIPT);
		}
		else if (t == Type.GLOBAL_SCOPE)
		{
			if (!k.isEmpty()) return;
			e.consume();
			_showGlobals(MenuType.GLOBAL);
		}
		else if (k.isEmpty()) // onglet en cours
		{
			e.consume();
			_openType(t, u.getMember());
			_updateSettings();
		}
		else if (k.isKey(KeyCode.CONTROL)) // nouvel onglet
		{
			e.consume();

			if (_header.getTabBar().getSize() < TabBar.MAX_TABS)
			{
				_unregister();
				_classStack = new ClassStack(t);
				_stacks.put(_header.getTabBar().addTab(_classStack.getHistoric().getCurrent()), _classStack);
				_register();
				_showClass();
			}
			else
			{
				_openType(t, u.getMember());
			}

			_updateSettings();
		}
	}


	private void _onTabEvent(TabEvent e)
	{
		e.consume();
		if (!(e.getTarget() instanceof TypeTab)) return;
		TypeTab b = (TypeTab) e.getTarget();

		if (e.getEventType() != TabEvent.SELECT)
		{
			if (e.getEventType() == TabEvent.CLOSE) _stacks.remove(b);
			else for (TypeTab i : e.tabs) _stacks.remove(i);
			b = _header.getTabBar().getCurrent();
		}

		if (_stacks.containsKey(b))
		{
			_unregister();
			_classStack = _stacks.get(b);
			_register();
			_updateTab();
			_showClass();
		}

		_updateSettings();
	}


	private void _openType(Type t, String m)
	{
		_classStack.add(t, m);
		_updateTab();
		_showClass();
	}


	private void _register()
	{
		Historic h = _classStack.getHistoric();
		h.addEventListener(HistoricEvent.CHANGE, _histChangeHandler);
		_header.getHistBar().setHistoric(h);
		_updateTab();
	}


	private void _showClass()
	{
		_layout = true;

		if (_currentFrame == _classStack)
		{
			_updateCategory();
			return;
		}

		if (super.getChildren().size() > 1) super.getChildren().remove(0);
		_currentFrame = _classStack;
		super.getChildren().add(0, _classStack);
		_header.getMenuBar().setCurrent(MenuType.CLASS);
		_updateCategory();
	}


	private void _showGlobals(@NotNull MenuType m)
	{
		_layout = true;
		_globalStack.setCurrent(m);
		_header.getMenuBar().setCurrent(m);

		if (_currentFrame != _globalStack)
		{
			if (super.getChildren().size() > 1) super.getChildren().remove(0);
			_currentFrame = _globalStack;
			super.getChildren().add(0, _globalStack);
		}

		_updateCategory();
	}


	private void _unregister()
	{
		_classStack.getHistoric().removeEventListener(HistoricEvent.CHANGE, _histChangeHandler);
	}


	private void _updateCategory()
	{
		Category c = null;
		if (_currentFrame == _classStack) c = _classStack.getHistoric().getCurrent().getCategory();
		else if (_header.getMenuBar().getCurrent() == MenuType.SCRIPT) c = Type.GD_SCRIPT.getCategory();
		else if (_header.getMenuBar().getCurrent() == MenuType.GLOBAL) c = Type.GLOBAL_SCOPE.getCategory();
		_header.getMenuBar().setCategory(c);
	}


	private void _updateSettings()
	{
		Section s = Settings.INSTANCE.getSection(Strings.UI);
		s.getProperty(_OPENED_TABS).setValue(_header.getTabBar().toString());
		s.getProperty(_CURRENT_TAB).setValue(_header.getTabBar().getIndex());
	}


	private void _updateTab()
	{
		_header.getTabBar().getCurrent().setType(_classStack.getHistoric().getCurrent());
	}
}