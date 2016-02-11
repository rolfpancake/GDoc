package ui.descriptions;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import data.Type;
import main.Strings;
import xml.XML;


public final class BriefDescription extends TextArea
{
	static private final String _LAST_PARAGRAPHS_RE = "\n.*$";
	static private final Pattern _PATTERN = Pattern.compile("((\".*?\")|[^\\.\n\"]*)*(\\.|$)");

	private Line _line;


	public BriefDescription(@Nullable Type host, @NotNull String description)
	{
		super(description);
		_line = new Line(host, EXTRACT(description));
		if (_line.isEmpty()) _line = null;
		else super.getChildren().add(_line);
	}


	/**
	 * Extrait une phrase.
	 */
	@NotNull
	static String EXTRACT(@NotNull String description)
	{
		// TODO extraire le brief sans supprimer les derniers paragraphes
		description = Strings.UNESCAPE_XML_DOLLAR(XML.UNESCAPE(description));
		description = description.replaceFirst(_LAST_PARAGRAPHS_RE, Strings.EMPTY);
		Matcher m = _PATTERN.matcher(description);
		return m.find() ? m.group() : description;
	}


	@Override
	public boolean isEmpty() { return _line == null; }


	@Override
	protected short computeHeight()
	{
		return _line != null ? _line.getHeight() : 0;
	}
}