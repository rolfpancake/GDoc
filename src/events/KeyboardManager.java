package events;


import java.util.ArrayList;
import java.util.HashMap;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.stage.Window;
import javafx.stage.WindowEvent;
import main.Strings;


public final class KeyboardManager
{
	static private HashMap<Window, KeyboardManager> _managers = new HashMap<Window, KeyboardManager>(2);

	private ArrayList<KeyCode> _keys = new ArrayList<KeyCode>(3);

	private EventHandler<KeyEvent> _onPress = new EventHandler<KeyEvent>()
	{
		@Override
		public void handle(KeyEvent event) // appelé en continu
		{
			if (!_keys.contains(event.getCode())) _keys.add(event.getCode());
		}
	};

	private EventHandler<KeyEvent> _onRelease = new EventHandler<KeyEvent>()
	{
		@Override
		public void handle(KeyEvent event)
		{
			_keys.remove(event.getCode());
		}
	};

	private ChangeListener<Boolean> _onFocusChanged = new ChangeListener<Boolean>()
	{
		@Override
		public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue)
		{
			/* hors focus les touches relâchées ne sont pas détectées, donc soit on supprime les touches
			   enregistrées, soit il faut utiliser une API externe */
			if (!newValue) _keys.clear();
		}
	};

	private EventHandler<WindowEvent> _onHidden = new EventHandler<WindowEvent>()
	{
		@Override
		public void handle(WindowEvent event)
		{
			Window w = (Window) event.getSource();
			w.removeEventHandler(KeyEvent.KEY_PRESSED, _onPress);
			w.removeEventHandler(KeyEvent.KEY_RELEASED, _onRelease);
			w.removeEventHandler(WindowEvent.WINDOW_HIDDEN, this);
			w.focusedProperty().removeListener(_onFocusChanged);
			_managers.remove(w);
		}
	};


	public KeyboardManager() { super(); }


	public KeyboardManager(@NotNull Window window) { _init(window); }


	@Nullable
	static public KeyboardManager GET_MANAGER(@Nullable Window window)
	{
		return _managers.get(window);
	}


	@Nullable
	static public KeyboardManager GET_MANAGER(Node node)
	{
		if (node == null) return null;
		Scene s = node.getScene();
		if (s == null) return null;
		Window w = s.getWindow();
		return _managers.get(w);
	}


	public KeyCode[] getCombo() { return _keys.toArray(new KeyCode[_keys.size()]); }


	public boolean isCombo(boolean ordered, KeyCode... keys)
	{
		if (keys == null || keys.length == 0) return _keys.size() == 0;
		int n = _keys.size();
		if (n != keys.length) return false;
		KeyCode k;

		if (ordered)
		{
			for (byte i = 0; i < n; ++i)
			{
				k = keys[i];
				if (k == null || k != _keys.get(i)) return false;
			}
		}
		else
		{
			for (KeyCode i : keys) if (i == null || !_keys.contains(i)) return false;
		}

		return true;
	}


	public boolean isCombo(KeyCode... keys)
	{
		if (keys == null) return _keys.size() == 0;
		if (keys.length != _keys.size()) return false;
		for (KeyCode i : keys) if (i == null || !_keys.contains(i)) return false;
		return true;
	}


	public boolean isEmpty() { return _keys.isEmpty(); }


	public boolean isKey(KeyCode key)
	{
		return key != null ? _keys.size() == 1 && key == _keys.get(0) : _keys.isEmpty();
	}


	public void setWindow(@NotNull Window window)
	{
		if (_managers.containsKey(window)) return;
		_managers.put(window, this);
		_init(window);
	}


	@Override
	public String toString()
	{
		String s = Strings.EMPTY;
		String c = Strings.COMMA + Strings.SPACE;
		for (KeyCode i : _keys) s += (s.isEmpty() ? Strings.EMPTY : c) + i.getName();
		return s;
	}


	private void _init(Window w)
	{
		w.addEventHandler(KeyEvent.KEY_PRESSED, _onPress);
		w.addEventHandler(KeyEvent.KEY_RELEASED, _onRelease);
		w.focusedProperty().addListener(_onFocusChanged);
		w.addEventHandler(WindowEvent.WINDOW_HIDDEN, _onHidden);
	}
}