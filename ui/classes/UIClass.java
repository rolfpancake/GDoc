package ui.classes;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.layout.Region;
import data.Group;
import data.GroupType;
import data.Trunk;
import data.Type;
import events.SelectionEvent;
import ui.descriptions.UIDescription;


final class UIClass extends Region
{
	static private final byte _TOP_PADDING = 20;
	static private final byte _PADDING = 30;
	static private final byte _SELECTOR_LEFT_MARGIN = 10;
	static private final Insets _INSETS = new Insets(15, 6, 35, 6);

	private Trunk _trunk;
	private DocList[] _groups = {null, null, null, null, null};
	private ClassHeader _header;
	private Selector _selector;
	private UIDescription _description;
	private DocList _current;
	private boolean _layout;

	private ChangeListener<Number> _changeListener = new ChangeListener<Number>()
	{
		@Override
		public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue)
		{
			_layout = true;
		}
	};


	UIClass(@NotNull Type type, @Nullable Trunk trunk)
	{
		super();
		if (trunk != null && trunk.getType() != type) throw new IllegalArgumentException("type mismatch");
		_trunk = trunk;

		_header = new ClassHeader(type);
		_selector = new Selector();
		_selector.setLayoutX(_SELECTOR_LEFT_MARGIN);
		super.getChildren().addAll(_header, _selector);
		super.widthProperty().addListener(_changeListener);
		super.heightProperty().addListener(_changeListener);

		if (trunk == null) return;

		if (trunk.description != null)
		{
			_description = new UIDescription(type, trunk.description, false);
			if (!_description.isEmpty()) super.getChildren().add(_description);
			else _description = null;
		}

		boolean f = true;
		Group g;

		for (GroupType i : Selector.TYPES)
		{
			if (trunk.hasGroup(i))
			{
				if (f)
				{
					_current = _getList(i);
					if (_current == null) continue;
					_selector.getButton(i).select();
					super.getChildren().add(_current);
					f = false;
				}
				else
				{
					_selector.getButton(i).quantify(SelectorButton.UNDEFINED_QUANTITY);
				}
			}
			else
			{
				_selector.getButton(i).quantify((short) 0);
			}
		}

		_selector.selectFirst();

		_selector.addEventHandler(SelectionEvent.GROUP_SELECTED, new EventHandler<SelectionEvent>()
		{
			@Override
			public void handle(SelectionEvent event) { _onGroupChanged(event); }
		});
	}


	@Override
	public String toString()
	{
		return super.getClass().getSimpleName() + " " + _trunk.getType();
	}


	DocList getGroup(GroupType type) { return type != null ? _getList(type) : null; }


	@Nullable
	Trunk getTrunk() { return _trunk; }


	void selectGroup(GroupType type)
	{
		if (type == null) return;
		DocList l = _groups[type.ordinal()];
		if (l == _current) return;
		super.getChildren().remove(_current);
		_current = l;
		super.getChildren().add(l);
		_selector.setCurrent(type);

		Platform.runLater(new Runnable()
		{
			@Override
			public void run() { _layoutLater(); }
		});
	}


	@Override
	protected void layoutChildren()
	{
		if (!_layout) return;
		_layout = false;

		short w = (short) super.getWidth();
		_header.setHSize(w);
		_selector.setPrefWidth(w);
		super.layoutChildren();

		if(_description != null) _description.setHSize(w);
		if (_current != null) _current.setHSize(w);

		if(_description != null)
		{
			_description.setLayoutY(_header.getVSize() + _TOP_PADDING);
			_selector.setLayoutY(_description.getLayoutY() + _description.getVSize() + _PADDING);
		}
		else
		{
			_selector.setLayoutY(_header.getBoundsInParent().getMaxY() + _PADDING);
		}

		_layoutGroup();
	}


	private DocList _getList(GroupType t)
	{
		byte j = (byte) t.ordinal();
		DocList dl = _groups[j];
		if (dl != null) return dl;
		SelectorButton b = _selector.getButton(t);

		if (!_trunk.hasGroup(t))
		{
			b.quantify((byte) 0);
			return null;
		}

		Group g = _trunk.getGroup(t);

		if (g == null) // fichier inaccessible
		{
			b.quantify((short) -1);
			return null;
		}

		dl = new DocList(g);
		dl.layout();
		_groups[j] = dl;
		b.quantify(g.getSize());
		return dl;
	}


	private void _layoutGroup()
	{
		if (_current != null)
		{
			_current.setLayoutY(_selector.getLayoutY() + _selector.getHeight() + _PADDING);
			_current.setHSize((short) super.getWidth());
		}
	}

	private void _layoutLater()
	{
		_layout = true;
		super.requestLayout();
	}

	private void _onGroupChanged(SelectionEvent e)
	{
		DocList g = _getList(_selector.getCurrent());
		if (g == _current) return;

		super.getChildren().remove(_current);
		_current = g;
		if (g == null) return;

		_layoutGroup();
		super.getChildren().add(g);
	}
}