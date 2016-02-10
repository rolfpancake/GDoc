package ui.types;

import java.util.ArrayList;
import java.util.Arrays;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.RowConstraints;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import data.Category;
import data.Documentation;
import data.Type;
import events.ChangeEvent;
import exceptions.NullArgumentException;
import fonts.FontManager;
import main.Launcher;
import main.Strings;
import ui.ConstraintFactory;
import ui.FilterBar;
import ui.Graphics;
import ui.UIType;


public final class SubList extends GridPane implements EventHandler<ChangeEvent>
{
	static private final byte _LINE_PADDING = 1;
	static private final byte _V_GAP = 10;

	static private final String _BASIC = "Basic";
	static private final String _VECTOR = "Vector";
	static private final String _ARRAY = "Array";
	static private final String _AUDIO = "Audio";
	static private final String _SHAPE = "Shape";
	static private final String _SHADER = "Shader";
	static private final String _BUTTON = "Button";
	static private final String _BOX = "Box";
	static private final String _RANGE = "Range";
	static private final String _POPUP = "Popup";
	static private final String _PHYSIC = "Physic";
	static private final String _JOINT = "Joint";
	static private final String _INSTANCE = "Instance";
	static private final String _REFERENCE = "Reference";
	static private final String _NODE = "Node";

	private ArrayList<Type> _temp;
	private ArrayList<UIType> _types;
	private ArrayList<UIType> _visibles;
	private byte[] _filters; // la liste All ne contient pas de filtres
	private VBox _list;
	private ListType _type;
	private Text _counter;
	private FilterBar _filterBar;

	// TODO marquer les classes de base dans les sous-listes filtrées
	// TODO indiquer le nombre de sous-types totaux de chaque type ?


	SubList(ListType type, ArrayList<Type> types)
	{
		super.setVgap(_V_GAP);
		if (type == null) throw new NullArgumentException("type");
		if (types == null) throw new NullArgumentException("types");
		_type = type;
		_temp = _initTypes(types);
		_temp.trimToSize();
	}


	@Nullable
	public Type getFirst()
	{
		if (_types != null && _types.size() > 0) return _types.get(0).getType();
		if (_temp != null && _temp.size() > 0) return _temp.get(0);
		return null;
	}


	@Override
	public void handle(ChangeEvent event)
	{
		if (_filterBar.isReversed()) _select(true);
		else if (_filterBar.getCurrent() == null) _selectAll();
		else _select(false);
	}


	@NotNull
	public ListType getType() { return _type; }


	boolean hasType(Type type)
	{
		if (type == null) return false;
		for (Node i : _list.getChildren()) if (type == ((UIType) i).getType()) return true;
		return false;
	}


	void reset()
	{
		if (_filterBar != null && (_filterBar.isReversed() || _filterBar.getCurrent() != null))
		{
			_filterBar.reset();
			_selectAll();
		}
	}


	@Override
	protected void layoutChildren()
	{
		if (_temp != null) _initDisplay();
		if (_filterBar != null) _filterBar.setPrefWidth(super.getWidth());
		super.layoutChildren();
	}


	private ArrayList<Type> _getSubTypes(ArrayList<Type> d, byte c, String n)
	{
		ArrayList<Type> l = new ArrayList<Type>(c);
		Type t = Documentation.INSTANCE.getType(n);
		for (Type i : d) if (i.isSubTypeOf(t)) l.add(i);
		return l;
	}


	/**
	 * Initialise l'affichage sur demande en créant les labels types et les filtres.
	 */
	private void _initDisplay()
	{
		_types = new ArrayList<UIType>(_temp.size());
		_visibles = new ArrayList<UIType>();

		for (Type i : _temp) _types.add(new UIType(i));
		_filters = new byte[_types.size()];
		Arrays.fill(_filters, (byte) -1);

		Documentation d = Documentation.INSTANCE;
		Type s0, s1, s2, s3, s4, t;
		String s = null;
		short j = 0;

		if (_type == ListType.ALL)
		{
			s = "All";
		}
		else if (_type == ListType.ROOT)
		{
			_filterBar = new FilterBar(true, true, _REFERENCE, _NODE);
			s = "Root";
			s0 = d.getType("Reference");
			s1 = d.getType("Node");

			for (UIType i : _types)
			{
				t = i.getType();
				if (t.isSubTypeOf(s0)) _filters[j] = 0;
				else if (t.isSubTypeOf(s1)) _filters[j] = 1;
				j += 1;
			}
		}
		else if (_type == ListType.BUILT_IN)
		{
			s = "Built-In";
			_filterBar = new FilterBar(true, true, _BASIC, _VECTOR, _ARRAY);

			for (UIType i : _types)
			{
				t = i.getType();
				if (t.isBasic()) _filters[j] = 0;
				else if (t.isVector()) _filters[j] = 1;
				else if (t.isArray()) _filters[j] = 2;
				j += 1;
			}
		}
		else if (_type == ListType.RESOURCE)
		{
			_filterBar = new FilterBar(true, true, _AUDIO, _SHAPE, _SHADER);
			s = Type.RESOURCE_TYPE_NAME;
			s0 = d.getType("AudioStream");
			s1 = d.getType("Shape");
			s2 = d.getType("Shape2D");
			s3 = d.getType("Shader");

			for (UIType i : _types)
			{
				t = i.getType();
				if (t.isSubTypeOf(s0)) _filters[j] = 0;
				else if (t.isSubTypeOf(s1) || t.isSubTypeOf(s2)) _filters[j] = 1;
				else if (t.isSubTypeOf(s3)) _filters[j] = 2;
				j += 1;
			}
		}
		else if (_type == ListType.CONTROL)
		{
			_filterBar = new FilterBar(true, true, _BUTTON, _BOX, _RANGE, _POPUP);
			s = Type.CONTROL_TYPE_NAME;
			s0 = d.getType("BaseButton");
			s1 = d.getType("Container");
			s2 = d.getType("Range");
			s3 = d.getType("Popup");

			for (UIType i : _types)
			{
				t = i.getType();
				if (t.isSubTypeOf(s0)) _filters[j] = 0;
				else if (t.isSubTypeOf(s1)) _filters[j] = 1;
				else if (t.isSubTypeOf(s2)) _filters[j] = 2;
				else if (t.isSubTypeOf(s3)) _filters[j] = 3;
				j += 1;
			}
		}
		else if (_type == ListType.NODE2D)
		{
			_filterBar = new FilterBar(true, true, _PHYSIC, _JOINT);
			s = "2D";
			s0 = d.getType("CollisionObject2D");
			s1 = d.getType("CollisionPolygon2D");
			s2 = d.getType("CollisionShape2D");
			s3 = d.getType("Joint2D");

			for (UIType i : _types)
			{
				t = i.getType();
				if (t.isSubTypeOf(s0) || t.isSubTypeOf(s1) || t.isSubTypeOf(s2)) _filters[j] = 0;
				else if (t.isSubTypeOf(s3)) _filters[j] = 1;
				j += 1;
			}
		}
		else if (_type == ListType.SPATIAL)
		{
			_filterBar = new FilterBar(true, true, _PHYSIC, _JOINT, _INSTANCE);
			s = "3D";
			s0 = d.getType("CollisionObject");
			s1 = d.getType("CollisionPolygon");
			s2 = d.getType("CollisionShape");
			s3 = d.getType("Joint");
			s4 = d.getType("VisualInstance");

			for (UIType i : _types)
			{
				t = i.getType();
				if (t.isSubTypeOf(s0) || t.isSubTypeOf(s1) || t.isSubTypeOf(s2)) _filters[j] = 0;
				else if (t.isSubTypeOf(s3)) _filters[j] = 1;
				else if (t.isSubTypeOf(s4)) _filters[j] = 2;
				j += 1;
			}
		}

		ColumnConstraints c0 = ConstraintFactory.GET_AUTO_GROWING_COLUMN();
		RowConstraints r0 = ConstraintFactory.GET_AUTO_FIXED_ROW();
		r0.setFillHeight(true);
		super.getColumnConstraints().add(c0);
		super.getRowConstraints().addAll(r0, r0);

		_counter = Graphics.CREATE_TEXT_FIELD(Strings.PARENTHESISE(_temp.size()));
		_counter.setFont(FontManager.DIGIT_FONT);
		HBox b = new HBox(Graphics.PADDING);
		b.setAlignment(Pos.CENTER_LEFT);
		b.getChildren().addAll(Graphics.CREATE_TEXT_FIELD(s + " Types"), _counter);
		super.add(b, 0, 0);

		_list = new VBox(_LINE_PADDING);
		_list.setFillWidth(false);

		if (_filterBar != null)
		{
			super.getRowConstraints().add(r0);
			super.add(_filterBar, 0, 1);
			super.add(_list, 0, 2);
			_filterBar.addEventHandler(ChangeEvent.CHANGE, this);
		}
		else // All
		{
			super.add(_list, 0, 1);
		}

		_temp.clear();
		_temp = null;

		Launcher.INSTANCE.updateMemoryUsage();
		_selectAll();
	}


	/**
	 * Initialise les types d'une sous-liste à partir d'une liste commune modifiée.
	 * Toutes les sous-listes sont initialisées en même temps à cause de la liste root qui est le reste des autres.
	 */
	private ArrayList<Type> _initTypes(ArrayList<Type> l)
	{
		if (_type == ListType.ALL) return new ArrayList<Type>(l);

		ArrayList<Type> l2 = null;

		if (_type == ListType.ROOT)
		{
			Documentation d = Documentation.INSTANCE;
			l2 = new ArrayList<Type>(l);
			_insertType(l2, d.getType(Type.RESOURCE_TYPE_NAME));
			_insertType(l2, d.getType(Type.SPATIAL_TYPE_NAME));
		}
		else
		{
			if (_type == ListType.BUILT_IN) l2 = new ArrayList<Type>(Arrays.asList(Category.BUILT_IN.getTypes()));
			else if (_type == ListType.RESOURCE) l2 = _getSubTypes(l, (byte) 77, Type.RESOURCE_TYPE_NAME);
			else if (_type == ListType.CONTROL) l2 = _getSubTypes(l, (byte) 64, Type.CONTROL_TYPE_NAME);
			else if (_type == ListType.NODE2D) l2 = _getSubTypes(l, (byte) 64, Type.NODE_2D_TYPE_NAME);
			else if (_type == ListType.SPATIAL) l2 = _getSubTypes(l, (byte) 64, Type.SPATIAL_TYPE_NAME);
			if (l2 != null) l.removeAll(l2);
		}

		return l2;
	}


	private void _insertType(ArrayList<Type> l, Type t)
	{
		if (t == null) return;
		String s1 = t.getName();
		if (s1 == null) return;
		String s2;

		for (short i = 0; i < l.size(); ++i)
		{
			s2 = l.get(i).getName();

			if (s2 != null && s1.compareTo(s2) < 0)
			{
				l.add(i, t);
				return;
			}
		}
	}


	private void _select(boolean r)
	{
		byte j = r ? -1 : _filterBar.indexof();
		short n = 0;
		_visibles.clear();

		for (short i = 0; i < _types.size(); ++i)
		{
			if (_filters[i] == j)
			{
				_visibles.add(_types.get(i));
				n += 1;
			}
		}

		_list.getChildren().clear();
		_list.getChildren().addAll(_visibles);
		_counter.setText(Strings.PARENTHESISE(n));
	}


	private void _selectAll()
	{
		_list.getChildren().clear();
		_list.getChildren().addAll(_types);
		_counter.setText(Strings.PARENTHESISE(_types.size()));
	}
}