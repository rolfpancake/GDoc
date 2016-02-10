package ui.content;


import java.util.HashMap;
import org.jetbrains.annotations.NotNull;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.EventHandler;
import javafx.scene.Node;
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
import main.Launcher;
import main.Strings;
import settings.Property;
import settings.ShortProperty;
import ui.UIType;
import ui.header.Header;
import ui.header.HistButton;
import ui.header.MenuType;
import ui.header.TabBar;
import ui.header.TypeTab;
import ui.types.TypePane;


public final class DocPane extends Region
{
	static private final byte _HEADER_HEIGHT = 83;
	static private final Property _OPENED_TABS = Launcher.SETTINGS.ui().openedTabs();
	static private final ShortProperty _CURRENT_TAB = Launcher.SETTINGS.ui().currentTab();

	private Header _header;
	private HashMap<TypeTab, ClassStack> _stacks;
	private GlobalStack _globalStack;
	private ClassStack _classStack;
	private Node _current;
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


	public DocPane(@NotNull TypePane _typePane)
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
		String[] o; // onglets ouverts
		Type t;
		ClassStack s;
		Documentation d = Documentation.INSTANCE;

		if (!_OPENED_TABS.getValue().isEmpty())
		{
			o = _OPENED_TABS.getValue().split(Strings.SPACE);
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
			if (t == null || t == Type.GD_SCRIPT || t == Type.GLOBAL_SCOPE) continue;
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

		if (_classStack == null)
		{
			s = new ClassStack(Type.ARRAY);
			_classStack = s;
			_header = new Header(s.getHistoric());
			_stacks.put(_header.getTabBar().getCurrent(), s);
		}

		_header.getTabBar().setCurrent((byte) _CURRENT_TAB.toShort());
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
		if (e.getButton() != MouseButton.PRIMARY) return;

		KeyboardManager k = Launcher.KEYBOARD;
		String m;
		Type t;

		if (e.getTarget() instanceof UIType) // lien
		{
			UIType u = (UIType) e.getTarget();
			t = u.getType();

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
			}
			else if (k.isKey(KeyCode.CONTROL)) // nouvel onglet
			{
				e.consume();
				if (_header.getTabBar().getSize() < TabBar.MAX_TABS) _openNewTab(t, u.getMember());
				else _openType(t, u.getMember());
			}
		}
		else if (e.getTarget() instanceof HistButton && k.isKey(KeyCode.CONTROL) &&
				 _header.getTabBar().getSize() < TabBar.MAX_TABS) // bloc d'historique
		{
			e.consume();
			_openNewTab(((HistButton) e.getTarget()).getType(), null);
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

		_updateCurrentTab();
	}


	private void _openNewTab(Type t, String m)
	{
		_unregister();
		_classStack = new ClassStack(t);
		_stacks.put(_header.getTabBar().addTab(_classStack.getHistoric().getCurrent()), _classStack);
		_register();
		_showClass();
		_classStack.frame(m);
		_updateOpenedTabs();
		_updateCurrentTab();
	}


	private void _openType(Type t, String m)
	{
		_classStack.add(t);
		_showClass();
		_classStack.frame(m);
		_updateTab();
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

		if (_current == _classStack)
		{
			_updateCategory();
			return;
		}

		if (super.getChildren().size() > 1) super.getChildren().remove(0);
		_current = _classStack;
		super.getChildren().add(0, _classStack);
		_header.getMenuBar().setCurrent(MenuType.CLASS);
		_updateCategory();
	}


	private void _showGlobals(@NotNull MenuType m)
	{
		_layout = true;
		_globalStack.setCurrent(m);
		_header.getMenuBar().setCurrent(m);

		if (_current != _globalStack)
		{
			if (super.getChildren().size() > 1) super.getChildren().remove(0);
			_current = _globalStack;
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
		if (_current == _classStack) c = _classStack.getHistoric().getCurrent().getCategory();
		else if (_header.getMenuBar().getCurrent() == MenuType.SCRIPT) c = Type.GD_SCRIPT.getCategory();
		else if (_header.getMenuBar().getCurrent() == MenuType.GLOBAL) c = Type.GLOBAL_SCOPE.getCategory();
		_header.getMenuBar().setCategory(c);
	}


	private void _updateCurrentTab()
	{
		_CURRENT_TAB.setValue(_header.getTabBar().getIndex());
	}


	private void _updateOpenedTabs()
	{
		_OPENED_TABS.setValue(_header.getTabBar().toString());
	}


	private void _updateTab()
	{
		_header.getTabBar().getCurrent().setType(_classStack.getHistoric().getCurrent());
		_updateOpenedTabs();
	}
}