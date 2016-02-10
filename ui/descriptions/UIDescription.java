package ui.descriptions;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.jetbrains.annotations.Nullable;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import data.Type;
import main.Strings;
import ui.Verticable;
import xml.XML;


public final class UIDescription extends Verticable
{
	static private final String _BREAK_RE = "\\[(b|B)(r|R)\\]";
	static private final String _WHITE_CHAR_RE = "\\p{Space}";
	static private final String _LAST_PARAGRAPHS_RE = "\n.*$";
	static private final Pattern _BRIEF_PATTERN = Pattern.compile("((\".*?\")|[^\\.\n\"]*)*(\\.|$)");

	static private final byte _EMPTY_LINE = 10;

	private ArrayList<Paragraph> _paragraphs;


	public UIDescription(@Nullable Type host, String description, boolean brief)
	{
		super();
		if (description == null) return;

		description = Strings.UNESCAPE_XML_DOLLAR(XML.UNESCAPE(description));
		description = description.replaceAll(_BREAK_RE, Strings.NEW_LINE);
		Matcher m;
		Paragraph p;

		if (brief)
		{
			// TODO extraire le brief sans supprimer les derniers paragraphes
			description = description.replaceFirst(_LAST_PARAGRAPHS_RE, Strings.EMPTY);
			m = _BRIEF_PATTERN.matcher(description);
			if (m.find()) description = m.group();
			p = new Paragraph(host, description, true);

			if (!p.isEmpty())
			{
				_paragraphs = new ArrayList<Paragraph>(1);
				_paragraphs.add(p);
				super.getChildren().add(p);
			}
		}
		else
		{
			String[] l = description.split(String.valueOf(Strings.NEW_LINE));
			_paragraphs = new ArrayList<Paragraph>(l.length);
			short n = 0;
			ObservableList<Node> c = super.getChildren();

			for (String i : l)
			{
				if (i.length() == 0 || i.matches(_WHITE_CHAR_RE)) // ligne vide
				{
					if (n > 0) _paragraphs.add(null);
					continue;
				}

				p = new Paragraph(host, i, false);

				if (!p.isEmpty())
				{
					_paragraphs.add(p);
					c.add(p);
					n += 1;
				}
			}

			if (n == 0) _paragraphs = null;
		}
	}


	public boolean isEmpty() { return _paragraphs == null; }


	@Override
	public void setHSize(short size)
	{
		if (_paragraphs == null) return;
		super.vSize = 0;

		for (Paragraph i : _paragraphs)
		{
			if (i != null)
			{
				i.setHSize(size);
				i.setLayoutY(super.vSize);
				super.vSize += i.getVSize();
			}
			else // ligne vide
			{
				super.vSize += _EMPTY_LINE;
			}
		}
	}
}