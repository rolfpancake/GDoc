package data;

import org.jetbrains.annotations.Nullable;
import main.Launcher;
import main.Strings;
import xml.XML;


/**
 * Classe de base abstraite des identifiants définis.
 */
abstract public class Defined extends Typed
{
	static private final String _INT_RE = "^-??\\d+$";
	static private final String _FLOAT_RE = "^-??\\d+\\.\\d+$";
	static private final String _BOOL_PATTERN = "^true|false$";
	static private final String _NUMBER_RE = "\\-?\\d+(\\.\\d)?";
	static private final String _PSEUDO_VALUE_RE = _NUMBER_RE + Strings.LEFT_PARENTHESIS + Strings.COMMA +
												   _NUMBER_RE + Strings.RIGHT_PARENTHESIS + Strings.ASTERISK;
	static private final String _FLOAT_DEFAULT = "0.0";
	static private final String _VECTOR2_DEFAULT = Type.VECTOR2_TYPE_NAME + Strings.LEFT_PARENTHESIS + _FLOAT_DEFAULT +
												   Strings.COMMA + Strings.SPACE + _FLOAT_DEFAULT + Strings.RIGHT_PARENTHESIS;

	private String _value;
	private boolean _pseudo;


	protected Defined(Group group, XML data)
	{
		super(group, data);
	}


	/**
	 * Récupére la valeur.
	 *
	 * @return Un objet String ou null
	 */
	@Nullable
	public final String getValue() { return _value; }


	/**
	 * Détermine si la valeur est une pseudo valeur, c'est à dire simplifiée.
	 */
	public boolean isPseudo() { return _pseudo; }


	protected void setValue(@Nullable String value)
	{
		if (_value != null || value == null) return;
		value = value.trim();
		String v = value.toLowerCase(); // NULL, True, False
		Type t = null;

		if (value.matches(_INT_RE))
		{
			t = Type.INT;
		}
		else if (value.matches(_FLOAT_RE))
		{
			t = Type.FLOAT;
		}
		else if (v.matches(_BOOL_PATTERN))
		{
			t = Type.BOOL;
			value = v;
		}
		else if (v.equals(Type.NULL_VALUE))
		{
			t = Type.NIL;
			value = v;
		}
		else
		{
			value = Strings.UNESCAPE_XML_DOLLAR(XML.UNESCAPE(value));
			int i = value.indexOf(Strings.LEFT_PARENTHESIS);

			if (i > -1)
			{
				t = Documentation.INSTANCE.getType(i > -1 ? value.substring(0, i) : value);
				value = value.replace(",", ", ");
			}

			if (t == null) t = Type.STRING;
		}

		_value = value;
		Type s = super.getType();

		if (s == Type.UNDEFINED)
		{
			super.setType(t);
		}
		else if (s != null && t != s)
		{
			if (s == Type.STRING)
			{
				_value = Strings.QUOTE + _value + Strings.QUOTE;
				return;
			}
			if (s == Type.FLOAT && t == Type.INT)
			{
				_value += ".0";
				return;
			}
			else if (s.isArray() && t == Type.ARRAY) // un tableau générique où un tableau typé est attendu
			{
				return;
			}
			else if (s.isVector()) // 0,0...
			{
				v = _value.replaceAll(Strings.SPACE, Strings.EMPTY);

				if (v.matches(_PSEUDO_VALUE_RE))
				{
					_pseudo = true;
					return;
				}
			}
			else if (s.getCategory() == Category.CORE)
			{
				if (t == Type.NIL) return; // null pour un objet
				// TODO CanvasItem, ItemList, Mesh, Physic2DServer, SurfaceTool, VisualServer
			}

			Group g = super.getGroup();
			Trunk k = g != null ? g.getTrunk() : null;
			v = null;
			if (k != null) v = " in " + k.getType() + "." + g.getType().toString();

			if (this instanceof Argument)
			{
				Parameterized p = ((Argument) this).getParameterized();
				Method m = p != null ? (Method) p : null;
				if (m != null) v = (v == null ? " in " : v + ".") + m.toString();
			}

			v = (v == null ? " in " : v + ".") + super.getName();
			Launcher.INSTANCE.log("Type mismatch" + v + ": " + t.getName() + " value, " + s.getName() + " expected");
		}
	}
}