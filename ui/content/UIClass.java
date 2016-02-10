package ui.content;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.EventHandler;
import javafx.scene.layout.Region;
import data.Group;
import data.GroupType;
import data.Trunk;
import data.Type;
import events.ChangeEvent;
import events.UIEvent;
import main.Launcher;
import settings.BooleanProperty;
import settings.Property;
import settings.ShortProperty;
import ui.Graphics;
import ui.descriptions.ClassDescription;
import ui.descriptions.EmptyDescription;
import ui.descriptions.TextArea;
import ui.filters.FilterBar;
import ui.filters.FilterButton;
import ui.filters.FilterGroup;

//TODO quand tous les filtres sont désactivés le premier ne devrait pas être basculé

final class UIClass extends Region
{
	static private final byte _TOP_PADDING = 20;
	static private final byte _PADDING = 30;
	static private final BooleanProperty _FOLD_DESCRIPTION = Launcher.SETTINGS.ui().foldDescription();
	static private final boolean _GLOBAL_MEMBER_FOLDING = Launcher.SETTINGS.ui().globalMemberFolding().toBoolean();
	static private final ShortProperty _FOLD_MEMBERS = Launcher.SETTINGS.ui().foldMembers();
	static private final byte _GROUP_TITLE_OFFSET = -1;
	static private final byte _FILTER_BAR_HEIGHT = 28;
	static private final byte _DESCRIPTION_GUTTER = 2;

	private Trunk _trunk;
	private DocList[] _groups = {null, null, null, null, null};
	private ClassHeader _header;
	private FilterBar _filterBar;
	private FilterGroup _groupBar;
	private FilterGroup _foldingBar;
	private TextArea _description;
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

	private EventHandler<UIEvent> _onFolding = new EventHandler<UIEvent>()
	{
		@Override
		public void handle(UIEvent event)
		{
			_layout = true;
			layoutChildren(); // sinon le layout parent se fait avant
		}
	};


	UIClass(@NotNull Type type, @Nullable Trunk trunk)
	{
		super();
		if (trunk != null && trunk.getType() != type) throw new IllegalArgumentException("type mismatch");
		_trunk = trunk;

		_header = new ClassHeader(type);
		_groupBar = new FilterGroup(false, false);
		_groupBar.addButton(new FilterButton(GroupType.values()[0].toString(), _GROUP_TITLE_OFFSET),
							new FilterButton(GroupType.values()[1].toString(), _GROUP_TITLE_OFFSET),
							new FilterButton(GroupType.values()[2].toString(), _GROUP_TITLE_OFFSET),
							new FilterButton(GroupType.values()[3].toString(), _GROUP_TITLE_OFFSET),
							new FilterButton(GroupType.values()[4].toString(), _GROUP_TITLE_OFFSET));

		_foldingBar = new FilterGroup(false, true);
		_foldingBar.addButton(new FilterButton(Graphics.GET_ICON_16((byte) 2, (byte) 0)),
							  new FilterButton(Graphics.GET_ICON_16((byte) 3, (byte) 0)));
		_foldingBar.setCurrent((byte) (_FOLD_MEMBERS.toShort() - 1));
		_foldingBar.setPrefWidth(50);

		_filterBar = new FilterBar(null, _groupBar, _foldingBar);
		_filterBar.setPrefHeight(_FILTER_BAR_HEIGHT);

		super.getChildren().addAll(_header, _filterBar);
		super.widthProperty().addListener(_changeListener);
		super.heightProperty().addListener(_changeListener);

		if (trunk == null)
		{
			_description = new EmptyDescription();
			_description.setLayoutX(_DESCRIPTION_GUTTER);
			super.getChildren().add(_description);
			return;
		}

		if (trunk.description != null)
		{
			_description = new ClassDescription(type, trunk.description);

			if (_description.isEmpty())
			{
				_description = new EmptyDescription();
			}
			else
			{
				if (Launcher.SETTINGS.ui().globalDescriptionFolding().toBoolean())
				{
					((ClassDescription) _description).fold(_FOLD_DESCRIPTION.toBoolean());

					_FOLD_DESCRIPTION.addEventListener(ChangeEvent.CHANGE, new EventHandler<ChangeEvent>()
					{
						@Override
						public void handle(ChangeEvent event) { _onSettingChange(_FOLD_DESCRIPTION); }
					});

				}

				_description.addEventHandler(UIEvent.FOLDING, _onFolding);
			}
		}
		else
		{
			_description = new EmptyDescription();
		}

		_description.setLayoutX(_DESCRIPTION_GUTTER);
		super.getChildren().add(_description);
		boolean f = true;
		FilterButton b;
		Group g;

		for (GroupType i : GroupType.values())
		{
			if (trunk.hasGroup(i))
			{
				if (f)
				{
					_current = _getList(i);
					if (_current == null) continue;
					_groupBar.setCurrent((byte) i.ordinal());
					super.getChildren().add(_current);
					f = false;
				}
				else
				{
					b = _groupBar.getButton((byte) i.ordinal());
					if (b != null) b.quantify(FilterButton.UNQUANTIFIED);
				}
			}
			else
			{
				b = _groupBar.getButton((byte) i.ordinal());
				if (b != null) b.quantify((short) 0);
			}
		}

		_groupBar.setCurrent();

		_groupBar.addEventHandler(ChangeEvent.CHANGE, new EventHandler<ChangeEvent>()
		{
			@Override
			public void handle(ChangeEvent event) { _onGroupChanged(); }
		});

		_foldingBar.addEventHandler(ChangeEvent.CHANGE, new EventHandler<ChangeEvent>()
		{
			@Override
			public void handle(ChangeEvent event) { _onFoldingChange(); }
		});

		if (Launcher.SETTINGS.ui().globalMemberFolding().toBoolean())
		{
			_FOLD_MEMBERS.addEventListener(ChangeEvent.CHANGE, new EventHandler<ChangeEvent>()
			{
				@Override
				public void handle(ChangeEvent event) { _onSettingChange(_FOLD_MEMBERS); }
			});
		}

		_description.addEventHandler(UIEvent.FOLDING, new EventHandler<UIEvent>()
		{
			@Override
			public void handle(UIEvent event) { _onFolding(); }
		});
	}


	@Override
	public String toString()
	{
		return super.getClass().getSimpleName() + " " + _trunk.getType();
	}


	@Nullable
	DocList getGroup(@NotNull GroupType type) { return _getList(type); }


	@Nullable
	Trunk getTrunk() { return _trunk; }


	void selectGroup(@NotNull GroupType type)
	{
		DocList l = _groups[type.ordinal()];
		if (l == _current) return;
		super.getChildren().remove(_current);
		_current = l;
		super.getChildren().add(l);
		_groupBar.setCurrent((byte) type.ordinal());

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
		_header.setWidth(w);
		_filterBar.setPrefWidth(w);
		super.layoutChildren();

		if (_description != null) _description.setWidth((short) (w - 2 * _DESCRIPTION_GUTTER));
		if (_current != null) _current.setWidth(w);

		if (_description != null)
		{
			_description.setLayoutY(_header.getHeight() + _TOP_PADDING);
			_filterBar.setLayoutY(_description.getLayoutY() + _description.getHeight() + _PADDING);
		}
		else
		{
			_filterBar.setLayoutY(_header.getLayoutY() + _header.getHeight() + _PADDING);
		}

		_layoutGroup();
	}


	private DocList _getList(@NotNull GroupType t)
	{
		byte j = (byte) t.ordinal();
		DocList dl = _groups[j];

		if (dl != null)
		{
			dl.setDisplayMode(DisplayMode.values()[_FOLD_MEMBERS.toShort()]);
			return dl;
		}

		FilterButton b = _groupBar.getButton((byte) (t.ordinal()));

		if (!_trunk.hasGroup(t))
		{
			if (b != null) b.quantify((byte) 0);
			return null;
		}

		Group g = _trunk.getGroup(t);

		if (g == null) // fichier inaccessible
		{
			if (b != null) b.quantify(FilterButton.UNQUANTIFIED);
			return null;
		}

		dl = new DocList(g, DisplayMode.values()[_FOLD_MEMBERS.toShort()]);
		dl.layout();
		_groups[j] = dl;
		if (b != null) b.quantify(g.getSize());
		return dl;
	}


	private void _layoutGroup()
	{
		if (_current != null)
		{
			_current.setLayoutY(_filterBar.getBoundsInParent().getMaxY() + _PADDING);
			_current.setWidth((short) super.getWidth());
		}
	}


	private void _layoutLater()
	{
		_layout = true;
		super.requestLayout();
	}


	private void _onFolding() // pliage de la description par un clic sur son plieur
	{
		_layout = true;
		layoutChildren();
	}


	private void _onFoldingChange() // clic dans la barre de pliage
	{
		short i = _foldingBar.indexOf();
		if (_GLOBAL_MEMBER_FOLDING) _FOLD_MEMBERS.setValue((short) (i + 1));

		if (_current != null)
		{
			if (i == 0) _current.setDisplayMode(DisplayMode.MONOLINE);
			else if (i == 1) _current.setDisplayMode(DisplayMode.MULTILINE);
			else _current.setDisplayMode(DisplayMode.EMPTY);
		}
	}


	private void _onGroupChanged()
	{
		byte i = _groupBar.indexOf();
		if (i < 0) return;
		DocList g = _getList(GroupType.values()[i]);
		if (g == _current) return;
		super.getChildren().remove(_current);
		_current = g;
		if (g == null) return;
		_layoutGroup();
		super.getChildren().add(g);
	}


	private void _onSettingChange(Property p) // modification d'un paramètre global
	{
		if (p == _FOLD_DESCRIPTION) // pliage des descriptions
		{
			if (super.getScene() != null || _description.isEmpty())	return;
			((ClassDescription) _description).fold(_FOLD_DESCRIPTION.toBoolean());
			_layout = true;
			super.requestLayout();
		}
		else if (p == _FOLD_MEMBERS) // pliage des membres
		{
			short s = _FOLD_MEMBERS.toShort();

			if (s == 1)
			{
				_foldingBar.setCurrent((byte) 0);
				if (_current != null) _current.setDisplayMode(DisplayMode.MONOLINE);
			}
			else if (s == 2)
			{
				_foldingBar.setCurrent((byte) 1);
				if (_current != null) _current.setDisplayMode(DisplayMode.MULTILINE);
			}
			else
			{
				_foldingBar.setCurrent(null);
				if (_current != null) _current.setDisplayMode(DisplayMode.EMPTY);
			}

			_layout = true;
			super.requestLayout();
		}
	}
}