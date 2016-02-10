package ui.content;

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
import ui.Graphics;
import ui.UIType;
import ui.descriptions.BriefDescription;
import ui.descriptions.EmptyDescription;
import ui.descriptions.TextArea;
import ui.filters.FilterBar;
import ui.filters.FilterButton;
import ui.filters.FilterGroup;


final class ClassSummary extends Region
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
	private FilterBar _filterBar;
	private FilterGroup _filters;
	private FilteredList _current;


	private class FilteredList
	{
		LinkedHashMap<Type, String> data; // données de pré-initialisation
		LinkedHashMap<UIType, TextArea> descriptions;
		short x; // abscisse pour le label de colonne
	}


	ClassSummary()
	{
		super();

		_all = Documentation.INSTANCE.getClassBriefs();
		_lists = new FilteredList[_LETTERS];
		_historic = new ArrayList<FilteredList>(_LETTERS);
		_types = _all.keySet().toArray(new Type[_all.size()]);

		FilterButton[] l = new FilterButton[_LETTERS];
		l[0] = new FilterButton(Strings.AT.charAt(0));
		for (char i = 65; i < 91; ++i) l[i - 64] = new FilterButton(i);
		_filters = new FilterGroup(false, false);
		_filters.addButton(l);
		_filterBar = new FilterBar(null, _filters, null);
		super.getChildren().add(_filterBar);

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

		_filterBar.setPrefWidth(super.getWidth());
		super.layoutChildren();

		if (_current == null) return;
		short w = (short) (super.getWidth() - _current.x);
		for (TextArea i : _current.descriptions.values()) i.setWidth(w);
	}


	private short _getTypeColumnWidth()
	{
		short x = 0;
		short w;

		for (UIType j : _current.descriptions.keySet())
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

		short y = (short) (_filterBar.getHeight() + _TOP_PADDING);
		super.getChildren().add(1, _type);
		super.getChildren().add(2, _counter);
		super.getChildren().add(3, _description);

		_type.setLayoutY(y);
		_counter.setLayoutX(_type.getBoundsInLocal().getWidth() + Graphics.PADDING);
		_counter.setLayoutY(y + _type.getBaselineOffset() - _counter.getBaselineOffset());
		_description.setLayoutY(y);

		_filterBar.layout(); // sinon hauteur 0
		_filters.setCurrent(_filters.getButton(Strings.CAPITAL_A));
		_select();
	}


	private void _select()
	{
		ObservableList<Node> l = super.getChildren();
		l.remove(4, l.size());
		_counter.setText(Strings.PARENTHESISE(0));
		FilterButton b = _filters.getCurrent();
		if (b == null) return;

		char c = b.getLetter();
		_current = _lists[_filters.indexOf()];

		if (_current == null || _current.descriptions == null)
		{
			_current = _initList(c);
			if (_current == null) return;

			if (_historic.size() >= _CAPACITY)
			{
				FilteredList f = _historic.get(0);
				if (f == _current) f = _historic.get(1);
				_historic.remove(f);
				f.descriptions.clear();
				f.descriptions = null;
			}

			_historic.add(_current);
			super.layoutChildren();
			l.addAll(_current.descriptions.keySet());
			l.addAll(_current.descriptions.values());
			_initListLayout();
		}
		else
		{
			l.addAll(_current.descriptions.keySet());
			l.addAll(_current.descriptions.values());
		}

		_description.setLayoutX(_current.x);
		_counter.setText(Strings.PARENTHESISE(_current.descriptions.size()));
	}


	private FilteredList _initList(char c) // initialise une liste pré-initialisée ou non
	{
		FilteredList f = _lists[_filters.indexOf()];
		if (f == null) f = new FilteredList();

		UIType u;
		TextArea d;
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

				if (s.charAt(0) != c)
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

				if (s.codePointAt(0) < 91 || s.toUpperCase().charAt(0) != c) // majuscule ou différence
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

		f.descriptions = new LinkedHashMap<UIType, TextArea>(f.data.size());

		for (Type i : f.data.keySet())
		{
			s = f.data.get(i);
			d = null;
			u = new UIType(i);

			if (s != null)
			{
				d = new BriefDescription(i, s);
				if (d.isEmpty()) d = new EmptyDescription();
			}

			if (d == null) d = new EmptyDescription();
			f.descriptions.put(u, d);
		}

		_lists[_filters.indexOf()] = f;
		Launcher.INSTANCE.updateMemoryUsage();

		return f;
	}


	private void _initListLayout()
	{
		if (_current == null) return;

		_current.x = (short) (Math.max(_getTypeColumnWidth(), _counter.getBoundsInParent().getMaxX()) + _H_PADDING);
		short y = (short) (_filters.getHeight() + _TOP_PADDING + _description.getBoundsInLocal().getHeight() +
						   _HEADER_PADDING);
		TextArea d;

		for (UIType i : _current.descriptions.keySet())
		{
			i.setLayoutY(y);
			d = _current.descriptions.get(i);
			d.relocate(_current.x, y + i.getBaselineOffset() - d.getBaselineOffset());
			y += i.getBoundsInLocal().getHeight() + _V_PADDING;
		}
	}
}