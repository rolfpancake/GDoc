package ui.content;

import java.util.ArrayList;
import org.jetbrains.annotations.NotNull;
import javafx.collections.ObservableList;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.text.Font;
import data.Argument;
import data.Parameterized;
import data.Return;
import fonts.FontManager;
import fonts.FontWeight;
import main.Launcher;
import main.Strings;
import ui.Graphics;


/**
 * Conteneurs des méthodes et signaux. Il ajoute une liste d'suffix décrits.
 */
class DocParameterized<D extends Parameterized> extends DocPrototyped<D>
{
	static protected final byte V_GAP = 5;
	static protected final byte H_GAP = 10;

	static private final boolean _HIDE_UNDOCUMENTED_ARGUMENT = Launcher.SETTINGS.ui().hideUndocumentedArgument().toBoolean();
	static private final boolean _HIDE_UNDOCUMENTED_RETURN = Launcher.SETTINGS.ui().hideUndocumentedReturn().toBoolean();

	protected DocNamed<Return> returns;

	private ArrayList<DocNamed<Argument>> _children;


	DocParameterized(@NotNull D parameterized)
	{
		super(parameterized);
	}


	@Override
	public void setWidth(short width)
	{
		width = (short) Math.max(0, width);
		if (width == super.getWidth()) return;

		if (_children != null)
		{
			short w = (short) Math.max(0, width - _children.get(0).getLayoutX());
			for (DocNamed<Argument> i : _children) i.setWidth(w);
			if (returns != null) returns.setWidth(w);
		}
		else if (returns != null)
		{
			returns.setWidth((short) Math.max(0, width - returns.getLayoutX()));
		}

		super.setWidth(width);
	}


	@Override
	void open(boolean value)
	{
		if (value == super.isOpened()) return;

		if (super.getDisplayMode() != DisplayMode.MULTILINE)
		{
			if (value)
			{
				if (_children != null) super.getChildren().addAll(_children);
				if (returns != null) super.getChildren().add(returns);
			}
			else
			{
				if (_children != null) super.getChildren().removeAll(_children);
				super.getChildren().remove(returns);
			}
		}

		super.open(value);
	}


	@Override
	final void setDisplayMode(@NotNull DisplayMode mode)
	{
		if (mode == super.getDisplayMode()) return;

		if (!super.isOpened())
		{
			if (mode != DisplayMode.MULTILINE)
			{
				if (_children != null) super.getChildren().removeAll(_children);
				super.getChildren().remove(returns);
			}
			else
			{
				if (_children != null) super.getChildren().addAll(_children);
				if (returns != null) super.getChildren().add(returns);
			}
		}

		super.setDisplayMode(mode);
	}


	@Override
	void setLeftOffset(short offset)
	{
		super.setLeftOffset(offset);

		if (_children != null)
		{
			short w = (short) Math.max(0, super.getWidth() - offset);

			for (DocNamed<Argument> i : _children)
			{
				i.setLayoutX(offset);
				i.setWidth(w);
			}

			if (returns != null)
			{
				returns.setLayoutX(offset);
				returns.setWidth(w);
			}
		}
		else if (returns != null)
		{
			returns.setLayoutX(offset);
			returns.setWidth((short) Math.max(0, super.getWidth() - offset));
		}
	}


	@Override
	protected short computeHeight()
	{
		short h = super.computeHeight();

		if (super.getDisplayMode() != DisplayMode.MULTILINE && !super.isOpened()) return h;

		if (_children != null)
		{
			h += V_GAP;

			for (DocNamed<Argument> i : _children)
			{
				h += V_GAP;
				i.setLayoutY(h);
				h += i.getHeight();
			}

			if (returns != null)
			{
				h += V_GAP;
				returns.setLayoutY(h);
				h += returns.getHeight();
			}
		}
		else if (returns != null)
		{
			h += 2 * V_GAP;
			returns.setLayoutY(h);
			h += returns.getHeight();
		}

		return h;
	}


	@Override
	protected void initDisplay()
	{
		super.initDisplay();
		Parameterized d = super.getData();
		byte n = d.getSize();
		if (n == 0) return;

		super.suffix = new Group();
		ObservableList<Node> l = super.suffix.getChildren();
		_children = new ArrayList<DocNamed<Argument>>(n);
		String c = String.valueOf(Strings.COMMA) + Strings.SPACE;
		Font f = FontManager.INSTANCE.getFont(FontWeight.Bold, false, Graphics.DOUBLE);
		UIArgument a;
		DocNamed<Argument> a2;
		int j = 1;

		for (Argument i : d.getArguments())
		{
			a = new UIArgument(i); // prototype
			a.layout(); // évite de tester le type pour le faire dans initLayout()
			l.add(a);
			a2 = new DocNamed<Argument>(i); // description
			if (a2.getDescription().isEmpty() && _HIDE_UNDOCUMENTED_ARGUMENT) continue;
			if (j < n) l.add(Graphics.CREATE_TEXT_FIELD(c, f));
			_children.add(a2);
			j += 1;
		}

		super.getChildren().add(super.suffix);

		if (_children.size() > 0)
		{
			_children.trimToSize();
			super.getChildren().addAll(_children);
		}
		else // aucun argument documenté
		{
			_children = null;
		}
	}


	@Override
	protected void initLayout()
	{
		if (super.suffix != null)
		{
			ObservableList<Node> l = super.suffix.getChildren();
			double o = l.get(0).getBaselineOffset();
			double x = 0;

			for (Node i : l)
			{
				i.relocate(x, o - i.getBaselineOffset());
				x += i.getBoundsInLocal().getWidth() + Graphics.THIN;
			}
		}

		super.initLayout();
		short m = returns != null ? returns.getMinLeftOffset() : 0;
		short v;

		if (_children != null)
		{
			for (DocNamed<Argument> i : _children)
			{
				v = i.getMinLeftOffset();
				if (v > m) m = v;
			}

			m += H_GAP;
			for (DocNamed<Argument> i : _children) i.setLeftOffset(m);
		}
		else
		{
			m += H_GAP;
		}

		if (returns != null) returns.setLeftOffset(m);
	}
}