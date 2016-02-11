package ui.content;

import java.util.ArrayList;
import org.jetbrains.annotations.NotNull;
import javafx.geometry.Bounds;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import data.Type;
import fonts.FontManager;
import fonts.FontWeight;
import main.Strings;
import ui.Graphics;
import ui.UIType;
import ui.VContainer;


final class ClassHeader extends VContainer
{
	static private final FontWeight _MAIN_WEIGHT = FontWeight.Bold;
	static private final FontWeight _COMMA_WEIGHT = FontWeight.Bold;
	static private final String _SUBCLASSES = "Subclasses";

	static private final byte _MAIN_OFFSET = 5;
	static private final byte _COMMA_OFFSET = 2;
	static private final byte _MARGIN = 5;
	static private final byte _H_GAP = 10;
	static private final byte _V_GAP = 2;
	static private final byte _SUBTYPES_V_PADDING = 15;
	static private final byte _SUBTYPES_H_PADDING = 8;

	static private final Insets _INSETS = new Insets(Graphics.PADDING, _MARGIN, 3, _MARGIN);

	private Text _name;
	private UIType[] _inheritance;
	private Text _title;
	private Text _counter;
	private Node[] _subtypes;
	private Rectangle _background;


	ClassHeader(@NotNull Type type)
	{
		super();
		_initInheritance(type);
		_initSubTypes(type);
		super.init();
	}


	@Override
	public void setWidth(short width)
	{
		width = (short) Math.max(0, width);
		if (width == super.getWidth()) return;
		_background.setWidth(width);
		super.setWidth(width);
	}


	@Override
	protected short computeHeight()
	{
		short h = (short) (_name.getBoundsInParent().getMaxY() + _MARGIN);

		if (_subtypes == null)
		{
			_background.setHeight(h);
			return h;
		}

		Node n = _subtypes[_subtypes.length - 1];
		short x = _MARGIN;
		short r = (short) (super.getWidth() - _MARGIN); // marge droite
		boolean f = true; // premier d'une ligne
		short j = (short) (_subtypes.length - 1);
		h = (short) (_title.getBoundsInParent().getMaxY() + Graphics.PADDING);
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
				h += b.getHeight() + _V_GAP;
				f = true;
			}

			o1.relocate(x, h);
			o2.relocate(o1.getBoundsInParent().getMaxX(), h);
			x = (short) (o2.getBoundsInParent().getMaxX() + _SUBTYPES_H_PADDING);
		}

		o1 = _subtypes[j];
		b = o1.getBoundsInLocal();

		if (!f && x + b.getWidth() > r)
		{
			x = _MARGIN;
			h += b.getHeight() + _V_GAP;
		}

		o1.relocate(x, h);
		h += b.getHeight() + _MARGIN;
		_background.setHeight(h);

		return h;
	}


	@Override
	protected boolean isInitialized() { return _name != null; }


	@Override
	protected void initDisplay()
	{
		super.initDisplay();
		super.getChildren().addAll(_background, _name);

		if (_inheritance != null) super.getChildren().addAll(_inheritance);

		if (_subtypes != null)
		{
			super.getChildren().addAll(_title, _counter);
			super.getChildren().addAll(_subtypes);
		}
	}


	@Override
	protected void initLayout()
	{
		_name.relocate(_MARGIN, _MARGIN);

		if (_inheritance != null)
		{
			double x = _name.getBoundsInParent().getMaxX() + _H_GAP;
			double y = _name.getLayoutY() + _name.getBaselineOffset() - _inheritance[0].getBaselineOffset();

			for (UIType i : _inheritance)
			{
				i.relocate(x, y);
				x += i.getBoundsInLocal().getWidth() + _H_GAP;
			}
		}

		if (_subtypes != null)
		{
			_title.relocate(_MARGIN, _name.getBoundsInLocal().getMaxY() + _SUBTYPES_V_PADDING);
			_counter.setLayoutX(_title.getBoundsInParent().getMaxX() + Graphics.PADDING);
			_counter.setLayoutY(_title.getLayoutY() + _title.getBaselineOffset() - _counter.getBaselineOffset());
		}
	}


	private void _initInheritance(Type t)
	{
		_background = new Rectangle(100, 50, Graphics.HEADER_COLOR);
		_name = Graphics.CREATE_TEXT_FIELD(t.getName(), FontManager.INSTANCE.getFont(_MAIN_WEIGHT, _MAIN_OFFSET));
		ArrayList<UIType> l = new ArrayList<UIType>();
		t = t.getSuper();

		while (t != null)
		{
			l.add(new UIType(t));
			t = t.getSuper();
		}

		if (l.size() > 0) _inheritance = l.toArray(new UIType[l.size()]);
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

		_counter = Graphics.CREATE_TEXT_FIELD(Strings.PARENTHESISE(n), FontManager.DIGIT_FONT);
		_title = Graphics.CREATE_TEXT_FIELD(_SUBCLASSES);
	}
}