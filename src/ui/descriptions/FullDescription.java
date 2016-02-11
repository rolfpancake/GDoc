package ui.descriptions;

import java.util.ArrayList;
import java.util.regex.Matcher;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import data.Type;
import main.Strings;
import xml.XML;


/**
 * Classe de base abstraite des descriptions complètes. Une description complète peut être pliée en une ligne.
 */
abstract public class FullDescription extends TextArea
{
	static protected final byte EMPTY_LINE = 10;

	static private final String _BREAK_RE = "\\[(b|B)(r|R)\\]";
	static private final String _WHITE_CHAR_RE = "\\p{Space}";

	protected ArrayList<Paragraph> paragraphs; /* l'indice 0 peut être null */
	private Stub _stub;
	private boolean _folded = false;
	private boolean _initialized = false;


	protected FullDescription(@Nullable Type host, @NotNull String description)
	{
		super(description);

		description = Strings.UNESCAPE_XML_DOLLAR(XML.UNESCAPE(description));
		description = description.replaceAll(_BREAK_RE, Strings.NEW_LINE);
		Paragraph p;
		Matcher m;

		String[] l = description.split(String.valueOf(Strings.NEW_LINE));
		paragraphs = new ArrayList<Paragraph>(l.length);
		short n = 0;
		boolean f = true;

		for (String i : l)
		{
			if (i.length() == 0 || i.matches(_WHITE_CHAR_RE)) // ligne vide
			{
				if (!f) paragraphs.add(null);
				continue;
			}

			if (!f)
			{
				p = new Paragraph(host, i);
				if (!p.isEmpty()) paragraphs.add(p);
			}
			else
			{
				_stub = new Stub(host, i);

				if (_stub.isEmpty())
				{
					_stub = null;
					return;
				}

				f = false;
			}
		}

		if (paragraphs.size() > 0) paragraphs.trimToSize();
		else paragraphs = null;

		_initialized = true;
		super.init();
	}


	public void fold(boolean value)
	{
		if (_stub == null || value == _folded) return;

		_folded = value;
		_stub.truncate(value);

		if (paragraphs != null)
		{
			if (value) super.getChildren().removeAll(paragraphs);
			else for (Paragraph i : paragraphs) if (i != null && i.getParent() == null) super.getChildren().add(i);
		}

		super.requestHeightComputing();
	}


	@Override
	public final boolean isEmpty() { return _stub == null; }


	/* 	stub peut être mono-ligne sans pliage et non tronqué avec pliage donc il est préférable que cette méthode
		renvoie la valeur réelle de _folded sans autre interprétation */
	public final boolean isFolded() { return _folded; }


	@Override
	public void setWidth(short width)
	{
		width = (short) Math.max(0, width);
		if (width == super.getWidth() || _stub == null) return;
		_stub.setWidth(width);
		if (paragraphs != null) for (Paragraph i : paragraphs) if (i != null) i.setWidth(width);
		super.setWidth(width);
	}


	@Override
	protected short computeHeight()
	{
		if (_stub == null) return 0;

		short y = _stub.getHeight();
		if (_folded) return y;

		if (paragraphs != null)
		{
			for (Paragraph i : paragraphs)
			{
				if (i != null)
				{
					i.setLayoutY(y);
					y += i.getHeight();
				}
				else // ligne vide
				{
					y += EMPTY_LINE;
				}
			}
		}

		return y;
	}


	@Nullable
	protected Stub getStub() { return _stub; }


	@Override
	protected void initDisplay()
	{
		if (_stub == null) return;
		ObservableList<Node> c = super.getChildren();
		c.add(_stub);
		if (paragraphs != null) for (Paragraph i : paragraphs) if (i != null) c.add(i);
	}


	@Override
	protected boolean isInitialized() { return _initialized; }
}