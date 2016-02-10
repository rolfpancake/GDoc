package ui.classes;

import java.lang.ref.WeakReference;
import org.jetbrains.annotations.NotNull;
import javafx.collections.ObservableList;
import javafx.geometry.Bounds;
import javafx.geometry.Pos;
import javafx.geometry.VPos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.layout.HBox;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import data.Argument;
import data.Constant;
import data.Identifier;
import data.Parameterized;
import fonts.FontManager;
import fonts.FontWeight;
import main.Strings;
import ui.Graphics;


final class UIPrototype<D extends Identifier, U extends Docable<D>> extends Parent
{
	private Text _name;
	private WeakReference<U> _docable;
	private boolean _initialized = false;
	private byte _padding;


	UIPrototype(@NotNull U docable)
	{
		_docable = new WeakReference<U>(docable);

		D d = docable.getData();
		_name = new Text(docable.getData().getName());
		_name.setTextOrigin(VPos.TOP);
		_name.setFill(d.nameIsValid() ? Graphics.TEXT_COLOR : Graphics.INVALID_ID);
		_name.setFont(FontManager.INSTANCE.getFont(FontWeight.Bold, Graphics.DOUBLE));
		_name.setMouseTransparent(true);
		super.getChildren().add(_name);

		if (d instanceof Parameterized) _fromParameterized((Parameterized) d);
		else if (d instanceof Constant) _fromConstant((Constant) d);
	}


	final U getDocable() { return _docable.get(); }


	final void highlight(boolean value)
	{
		U d = _docable.get();
		if (d == null) return;
		if (value) _name.setFill(Graphics.HIGHLIGHT);
		else _name.setFill(d.getData().nameIsValid() ? Graphics.TEXT_COLOR : Graphics.INVALID_ID);
	}


	@Override
	protected void layoutChildren()
	{
		if (_initialized) return;

		_initialized = true;
		super.layoutChildren();
		double x = 0;
		double m = 0;
		double h;
		Bounds b;

		for (Node i : super.getChildren())
		{
			h = i.getBoundsInLocal().getHeight();
			if (h > m) m = h;
		}

		for (Node i : super.getChildren())
		{
			b = i.getBoundsInLocal();
			i.setLayoutX(x);
			i.setLayoutY((m - b.getHeight()) / 2);
			x += b.getWidth() + _padding;
		}
	}


	private Text _createText(String t, boolean b, boolean i, byte o)
	{
		Text f = Graphics.CREATE_TEXT_FIELD(t);
		f.setFont(FontManager.INSTANCE.getFont(b ? FontWeight.Bold : FontWeight.Regular, i, o));
		return f;
	}


	private void _fromParameterized(Parameterized p)
	{
		super.getChildren().add(Graphics.CREATE_TEXT_FIELD(Strings.LEFT_PARENTHESIS));
		byte n = p.getSize();
		ObservableList<Node> l = super.getChildren();
		_padding = Graphics.THIN;

		if (n > 0)
		{
			String c = String.valueOf(Strings.COMMA) + Strings.SPACE;
			Font f = FontManager.INSTANCE.getFont(FontWeight.Bold, false, Graphics.DOUBLE);
			UIArgument a;
			int j = 1;

			for (Argument i : p.getArguments())
			{
				a = new UIArgument(i);
				a.layout();
				l.add(a);
				if (j < n) l.add(Graphics.CREATE_TEXT_FIELD(c, f));
				j += 1;
			}
		}

		l.add(Graphics.CREATE_TEXT_FIELD(Strings.RIGHT_PARENTHESIS));
	}


	private void _fromConstant(Constant c)
	{
		String v = c.getValue();
		if (v == null) return;
		_padding = Graphics.PADDING;

		Font f = FontManager.INSTANCE.getFont(Graphics.THIN);
		HBox b = new HBox(Graphics.PADDING);
		b.setAlignment(Pos.CENTER_LEFT);
		b.getChildren().add(Graphics.CREATE_TEXT_FIELD(Strings.EQUAL, f));
		b.getChildren().add(Graphics.CREATE_TEXT_FIELD(v, f));

		super.getChildren().add(b);
	}
}