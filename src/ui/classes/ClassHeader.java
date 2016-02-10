package ui.classes;

import java.util.ArrayList;
import javafx.geometry.Bounds;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import data.Type;
import exceptions.NullArgumentException;
import fonts.FontManager;
import fonts.FontWeight;
import main.Strings;
import ui.Graphics;
import ui.UIType;
import ui.Verticable;


final class ClassHeader extends Verticable
{
	static private final FontWeight _MAIN_WEIGHT = FontWeight.Bold;
	static private final FontWeight _COMMA_WEIGHT = FontWeight.Bold;
	static private final String _SUBCLASSES = "Subclasses";

	static private final byte _MAIN_OFFSET = 5;
	static private final byte _COMMA_OFFSET = 2;
	static private final byte _MARGIN = 5;
	static private final byte _H_GAP = 10;
	static private final byte _V_GAP = 2;
	static private final byte _SUBTYPES_PADDING = 15;

	static private final Insets _INSETS = new Insets(Graphics.PADDING, _MARGIN, 3, _MARGIN);

	private Text _name;
	private UIType[] _inheritance;
	private Text _title;
	private Node[] _subtypes;
	private boolean _initialized;
	private Rectangle _background;


	ClassHeader(Type type)
	{
		super();
		if (type == null) throw new NullArgumentException();

		_initialized = false;
		_initInheritance(type);
		_initSubTypes(type);
	}


	@Override
	public void setHSize(short size)
	{
		_background.setWidth(size);
		if (!_initialized) _initLayout();
		if (_subtypes == null) return;

		short x = _MARGIN;
		short y = (short) (_title.getBoundsInParent().getMaxY() + Graphics.PADDING);
		short r = (short) (size - _MARGIN); // marge droite
		boolean f = true; // premier d'une ligne
		short j = (short) (_subtypes.length - 1);
		Node o1, o2;
		Bounds b;

		for (short i = 0; i < j; i += 2)
		{
			o1 = _subtypes[i];
			o2 = _subtypes[i + 1];
			b = o1.getBoundsInLocal();

			if (f)
			{
				f = false;
			}
			else if (x + b.getWidth() + o2.getBoundsInLocal().getWidth() > r)
			{
				x = _MARGIN;
				y += b.getHeight() + _V_GAP;
				f = true;
			}

			o1.setLayoutX(x);
			o1.setLayoutY(y);
			o2.setLayoutX(o1.getBoundsInParent().getMaxX());
			o2.setLayoutY(y);
			x = (short) (o2.getBoundsInParent().getMaxX() + Graphics.PADDING);
		}

		o1 = _subtypes[j];
		b = o1.getBoundsInLocal();

		if (!f && x + b.getWidth() > r)
		{
			x = _MARGIN;
			y += b.getHeight() + _V_GAP;
		}

		o1.setLayoutX(x);
		o1.setLayoutY(y);
		super.vSize = (short) (y + b.getHeight() + _MARGIN);
		_background.setHeight(super.vSize);
	}


	private void _initInheritance(Type t)
	{
		_background = new Rectangle(100, 50, Graphics.HEADER_COLOR);
		_name = Graphics.CREATE_TEXT_FIELD(t.getName(), FontManager.INSTANCE.getFont(_MAIN_WEIGHT, _MAIN_OFFSET));
		ArrayList<UIType> l = new ArrayList<UIType>();
		super.getChildren().addAll(_background, _name);
		t = t.getSuper();

		while (t != null)
		{
			l.add(new UIType(t));
			t = t.getSuper();
		}

		if (l.size() > 0)
		{
			_inheritance = l.toArray(new UIType[l.size()]);
			super.getChildren().addAll(l);
		}
	}


	private void _initLayout()
	{
		_initialized = true;
		Text c = null;

		if (_subtypes != null)
		{
			String s = Strings.PARENTHESISE((_subtypes.length + 1) / 2);
			c = Graphics.CREATE_TEXT_FIELD(s, FontManager.DIGIT_FONT);
			_title = Graphics.CREATE_TEXT_FIELD(_SUBCLASSES);
			super.getChildren().addAll(_title, c);
		}

		super.layoutChildren();
		_name.setLayoutX(_MARGIN);
		_name.setLayoutY(_MARGIN);
		super.vSize = (short) (_name.getBoundsInParent().getMaxY() + _MARGIN);
		_background.setHeight(super.vSize);

		if (_inheritance != null)
		{
			double x = _name.getBoundsInParent().getMaxX() + _H_GAP;
			double y = _MARGIN + (_name.getBoundsInLocal().getHeight() - _inheritance[0].getBoundsInLocal().getHeight()) / 2;

			for (UIType i : _inheritance)
			{
				i.setLayoutX(x);
				i.setLayoutY(y);
				x += i.getBoundsInLocal().getWidth() + _H_GAP;
			}
		}

		if (_subtypes == null) return;

		_title.setLayoutX(_MARGIN);
		_title.setLayoutY(_name.getBoundsInLocal().getMaxY() + _SUBTYPES_PADDING);

		if (c != null)
		{
			c.setLayoutX(_title.getBoundsInParent().getMaxX() + Graphics.PADDING);
			//c.setLayoutY(_title.getLayoutY() + (_title.getBoundsInLocal().getHeight() - c.getBoundsInLocal().getHeight()) / 2);
			c.setLayoutY(_title.getLayoutY() + _title.getBaselineOffset() - c.getBaselineOffset());
		}
	}


	private void _initSubTypes(Type t)
	{
		if (!t.hasSubType()) return;

		Type[] l = t.getSubTypes();
		if (l == null) return; // warning

		int n = l.length;
		ArrayList<Type> l2 = new ArrayList<Type>(n);
		Font f = FontManager.INSTANCE.getFont(_COMMA_WEIGHT, _COMMA_OFFSET);
		String s1, s2;
		int k;
		n = 0;

		for (Type i : l) // tri alphabétique
		{
			k = 0;
			s1 = i.getName();
			if (s1 == null) continue; // toujours false

			for (Type j : l2)
			{
				s2 = j.getName();
				if (s2 != null && s1.compareTo(s2) < 0) break;
				k += 1;
			}

			l2.add(k, i);
			++n;
		}

		_subtypes = new Node[2 * n - 1];
		k = n - 1;

		for (short i = 0; i < n; ++i)
		{
			_subtypes[2 * i] = new UIType(l2.get(i));
			if (i < k) _subtypes[2 * i + 1] = Graphics.CREATE_TEXT_FIELD(Strings.COMMA, f);
		}

		super.getChildren().addAll(_subtypes);
	}
}