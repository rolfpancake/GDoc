package main;

import com.sun.javafx.tk.FontLoader;
import com.sun.javafx.tk.Toolkit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.jetbrains.annotations.NotNull;
import javafx.scene.text.Font;


abstract public class Strings
{
	static public final String ASTERISK = "*";
	static public final String AT = "@";
	static public final char CAPITAL_A = 'A';
	static public final String COMMA = ",";
	static public final char COLON = ':';
	static public final String DASH = "-";
	static public final char DOT = '.';
	static public final String EMPTY = "";
	static public final char GREATER_THAN = '>';
	static public final String EQUAL = "=";
	static public final char LEFT_BRACKET = '[';
	static public final String LEFT_PARENTHESIS = "(";
	static public final char LESS_THAN = '<';
	static public final String NAMESPACE = "::";
	static public final String NEW_LINE = "\n";
	static public final char QUOTE = '"';
	static public final char RIGHT_BRACKET = ']';
	static public final String RIGHT_PARENTHESIS = ")";
	static public final char SEMICOLON = ';';
	static public final char SHARP = '#';
	static public final char SLASH = '/';
	static public final String SPACE = " ";
	static public final char TAB = '\t';
	static public final String UNDERLINE = "_";

	static public final String ELLIPSIS = "..";

	static private final String[] _ESCAPING = {"<", "\\$lt;", ">", "\\$gt;", "\"", "\\$quot;", "'", "\\$apo;",
											   "&", "\\$amp;"};

	static private final Pattern _CAMEL_CASE = Pattern.compile("([^A-Z]+)|([A-Z]+[^A-Z]*)");

	static private final FontLoader _FONT_LOADER = Toolkit.getToolkit().getFontLoader();



	static public String CAPITALIZE(String string)
	{
		if (string == null) return null;
		if (string.length() < 2) return string.toUpperCase();
		return string.substring(0, 1).toUpperCase() + string.substring(1).toLowerCase();
	}


	/**
	 * Routine de nettoyage d'une chaîne de caractères.
	 *
	 * @return Une chaîne si la chaîne n'est pas vide, sinon null
	 */
	static public String CLEAN(String string)
	{
		if (string == null) return null;
		string = string.trim();
		return string.isEmpty() ? null : string;
	}


	static public String ELLIPSE(@NotNull String string, @NotNull Font font, double width)
	{
		if (string.isEmpty()) return string;
		if (width <= 0) return ELLIPSIS;

		String s = string;
		byte i = (byte) (string.length() - 1);

		while (_FONT_LOADER.computeStringWidth(s, font) > width && i > -1)
		{
			s = string.substring(0, i) + ELLIPSIS;
			i -= 1;
		}

		return s;
	}


	static public String PARENTHESISE(Object value)
	{
		return LEFT_PARENTHESIS + String.valueOf(value) + RIGHT_PARENTHESIS;
	}


	static public String SPLIT_CAMEL_CASE(String string)
	{
		if (string == null) return EMPTY;
		String w = EMPTY;
		Matcher m = _CAMEL_CASE.matcher(string);
		while (m.find()) w += (w.isEmpty() ? EMPTY : SPACE) + m.group(0);
		return w;
	}


	/**
	 * Remplace les séquences d'échappement XML commençant par un $ au lieu d'un &.
	 */
	static public String UNESCAPE_XML_DOLLAR(String value)
	{
		if (value == null || value.isEmpty()) return value;
		for (int i = 1; i < _ESCAPING.length; i += 2) value = value.replaceAll(_ESCAPING[i], _ESCAPING[i - 1]);
		return value;
	}
}