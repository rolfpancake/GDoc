package ui.classes;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.layout.Region;
import javafx.scene.text.Text;
import data.Documentation;
import data.Type;
import events.ChangeEvent;
import fonts.FontManager;
import main.Launcher;
import main.Strings;
import ui.FilterBar;
import ui.FilterButton;
import ui.Graphics;
import ui.UIType;
import ui.descriptions.UIDescription;


final class TypeBriefs extends Region
{
	static private final byte _H_PADDING = 15;
	static private final byte _TOP_PADDING = 25;
	static private final byte _HEADER_PADDING = 30;
	static private final byte _V_PADDING = 15;
	static private final byte _LETTERS = 27;
	static private final byte _CAPACITY = 10;

	private FilteredList[] _lists; // un tableau fixe pour un accès par code caractère
	private ArrayList<FilteredList> _historic;
	private Type[] _types; // pour l'ordre inverse
	private LinkedHashMap<Type, String> _all; // vidée
	private Text _type;
	private Text _counter;
	private Text _description;
	private FilterBar _filters;
	private FilteredList _current;


	private class FilteredList
	{
		LinkedHashMap<Type, String> data; // données de pré-initialisation
		LinkedHashMap<UIType, UIDescription> ui;
		ArrayList<UIDescription> descriptions; // pour ajouter une collection d'enfants tous non nuls
	}


	TypeBriefs()
	{
		super();

		_all = Documentation.INSTANCE.getClassBriefs();
		_lists = new FilteredList[_LETTERS];
		_historic = new ArrayList<FilteredList>(_LETTERS);
		_types = _all.keySet().toArray(new Type[_all.size()]);

		String[] n = new String[_LETTERS];
		n[0] = Strings.AT;
		for (char i = 65; i < 91; ++i) n[i - 64] = String.valueOf(i);
		_filters = new FilterBar(false, false, n);
		super.getChildren().add(_filters);

		_filters.addEventHandler(ChangeEvent.CHANGE, new EventHandler<ChangeEvent>()
		{
			@Override
			public void handle(ChangeEvent event) { _select(); }
		});
	}


	@Override
	protected void layoutChildren()
	{
		if (_type == null) // évite la première passe inutile
		{
			_init();
			return;
		}

		_filters.setPrefWidth(super.getWidth());
		super.layoutChildren();

		if (_current == null) return;
		short w = (short) (super.getWidth() - _description.getLayoutX());
		for (UIDescription i : _current.descriptions) i.setHSize(w);
	}


	private short _getTypeColumnWidth()
	{
		short x = 0;
		short w;

		for (UIType j : _current.ui.keySet())
		{
			w = (short) j.getBoundsInLocal().getWidth();
			if (w > x) x = w;
		}

		return x;
	}


	private void _init()
	{
		_type = Graphics.CREATE_TEXT_FIELD("Type");
		_description = Graphics.CREATE_TEXT_FIELD("Description");
		_counter = Graphics.CREATE_TEXT_FIELD("(0)", FontManager.DIGIT_FONT);
		super.layoutChildren();

		short y = (short) (_filters.getHeight() + _TOP_PADDING);
		super.getChildren().add(1, _type);
		super.getChildren().add(2, _counter);
		super.getChildren().add(3, _description);

		_counter.setLayoutX(_type.getBoundsInLocal().getWidth() + Graphics.PADDING);
		_counter.setLayoutY(y + (_type.getBoundsInLocal().getHeight() - _counter.getBoundsInLocal().getHeight()) / 2);
		_type.setLayoutY(y);
		_description.setLayoutY(y);

		_filters.setCurrent(_filters.getButton(Strings.CAPITAL_A));
		_select();
	}


	private void _select()
	{
		ObservableList<Node> c = super.getChildren();
		c.remove(4, c.size());
		_counter.setText(Strings.PARENTHESISE(0));
		FilterButton b = _filters.getCurrent();
		if (b == null) return;

		String s = b.getName();
		if (s == null || s.isEmpty()) return;
		s = String.valueOf(s.charAt(0)).toUpperCase();
		_current = _lists[_filters.indexof()];

		if (_current == null || _current.ui == null)
		{
			_current = _initList(s);
			if (_current == null) return;

			if (_historic.size() >= _CAPACITY)
			{
				FilteredList l = _historic.get(0);
				if (l == _current) l = _historic.get(1);
				_historic.remove(l);
				l.ui.clear();
				l.ui = null;
				l.descriptions.clear();
				l.descriptions = null;
			}

			_historic.add(_current);
			super.layoutChildren();
			c.addAll(_current.ui.keySet());
			c.addAll(_current.descriptions);
			_initListLayout();
		}
		else
		{
			c.addAll(_current.ui.keySet());
			c.addAll(_current.descriptions);
		}

		if (_current.descriptions.size() > 0)
			_description.setLayoutX(_current.descriptions.get(0).getLayoutX());
		else
			_description.setLayoutX(Math.max(_getTypeColumnWidth(), _counter.getBoundsInParent().getMaxX()) + _H_PADDING);

		_counter.setText(Strings.PARENTHESISE(_current.ui.size()));
	}


	private FilteredList _initList(String c) // initialise une liste pré-initialisée ou non
	{
		FilteredList f = _lists[_filters.indexof()];
		if (f == null) f = new FilteredList();

		UIType u;
		UIDescription d;
		String s;

		if (f.data == null) // liste non pré-initialisée
		{
			ArrayList<Type> l = new ArrayList<Type>();
			boolean b = false; // sélection commencée
			Type t;
			short j;

			for (Type i : _types) // A-Z
			{
				s = i.getName();
				if (s == null) continue;

				if (!String.valueOf(s.charAt(0)).equals(c))
				{
					if (b) break;
					continue;
				}

				if (!b) b = true;
				l.add(i);
			}

			b = false;
			j = (short) l.size();

			for (int i = _types.length - 1; i > -1; --i) // z-a
			{
				t = _types[i];
				s = t.getName();
				if (s == null) continue;
				s = String.valueOf(s.charAt(0));

				if (s.codePointAt(0) < 91 || !s.toUpperCase().equals(c))
				{
					if (b) break;
					continue;
				}

				if (!b) b = true;
				l.add(j--, t);
			}

			if (l.size() == 0) return null;

			f.data = new LinkedHashMap<Type, String>(l.size());
			for (Type i : l) f.data.put(i, _all.get(i));
			_all.keySet().removeAll(l);
			_types = _all.keySet().toArray(new Type[_all.size()]);
		}

		f.ui = new LinkedHashMap<UIType, UIDescription>(f.data.size());
		f.descriptions = new ArrayList<UIDescription>(); // potentiellement vide

		for (Type i : f.data.keySet())
		{
			s = f.data.get(i);
			d = null;
			u = new UIType(i);

			if (s != null)
			{
				d = new UIDescription(i, s, true);
				if (d.isEmpty()) d = null;
				else f.descriptions.add(d);
			}

			f.ui.put(u, d);
		}

		f.descriptions.trimToSize();
		_lists[_filters.indexof()] = f;
		Launcher.INSTANCE.updateMemoryUsage();

		return f;
	}


	private void _initListLayout()
	{
		if (_current == null) return;

		short x = (short) (Math.max(_getTypeColumnWidth(), _counter.getBoundsInParent().getMaxX()) + _H_PADDING);
		UIDescription d;
		short y = (short) (_filters.getHeight() + _TOP_PADDING + _description.getBoundsInLocal().getHeight() +
						   _HEADER_PADDING);

		for (UIType i : _current.ui.keySet())
		{
			i.setLayoutY(y);
			d = _current.ui.get(i);

			if (d != null)
			{
				d.setLayoutX(x);
				d.setLayoutY(y);
			}

			y += i.getBoundsInLocal().getHeight() + _V_PADDING;
		}
	}
}