package ui.descriptions;


import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.jetbrains.annotations.Nullable;
import javafx.geometry.Bounds;
import javafx.scene.Node;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import data.Documentation;
import data.Type;
import fonts.FontManager;
import fonts.FontWeight;
import main.Strings;
import ui.Graphics;
import ui.UIType;
import ui.Verticable;

/* en étendant Region la hauteur de l'occurrence reste celle d'une seule ligne, updateBounds() ne change rien,
   il faut un Platform::runLater() + un requestParenLayout() + un événement pour les conteneurs parents;
   en étendant Verticable la hauteur de l'occurrence est disponible après définition de sa largeur */

final class Paragraph extends Verticable
{
	static private final String _CONTENT_GROUP = "C";
	static private final String _TAG_GROUP = "T";

	static private final String _REMOVABLES_RE = "\\[image .*?/\\]|TODO.*$|\\p{Space}$";
	static private final String _IGNORED_TAGS_RE = "center|img";
	static private final String _ITALIC_TAG_RE = "i|I";
	static private final String _BOLD_TAG_RE = "b|B";
	static private final String _CODE_TAG_RE = "code";
	static private final String _CONTENT_RE = "(?<" + _CONTENT_GROUP + ">.*?)";
	static private final String _TAG_RE = "\\[(?<" + _TAG_GROUP + ">[a-zA-Z]+)\\]" + _CONTENT_RE +
										  "\\[/\\k<" + _TAG_GROUP + ">\\]";
	static private final String _LINK_RE = "\\[(?<" + _CONTENT_GROUP + ">.*?)\\]";
	static private final String _MEMBER_RE = "[a-zA-Z_][\\w_]*";
	static private final String _TYPE_RE = "[a-zA-Z@][\\w _@]*";
	static private final String _LONG_MEMBER_RE = "method +" + _TYPE_RE + "\\." + _MEMBER_RE;
	static private final String _INTERNAL_RE = "method +" + _MEMBER_RE;
	static private final String _SHORT_MEMBER_RE = _TYPE_RE + "\\." + _MEMBER_RE;
	static private final String _END_PUNCT_RE = "[\\.,;:?!)]+";

	static private final Pattern _TAG_PATTERN = Pattern.compile(_TAG_RE);
	static private final Pattern _LINK_PATTERN = Pattern.compile(_LINK_RE);

	/* [^ ] accepte les tabulations antérieures */
	static private final Pattern _WORD_PATTERN = Pattern.compile("(?<" + _CONTENT_GROUP + ">[^ ]+?)(?: |$)");

	static private final byte _OFFSET = 1;
	static private final Font _REGULAR = FontManager.INSTANCE.getFont(_OFFSET);
	static private final Font _ITALIC = FontManager.INSTANCE.getFont(FontWeight.Regular, true, _OFFSET);
	static private final Font _BOLD = FontManager.INSTANCE.getFont(FontWeight.Bold, _OFFSET);
	static private final Font _BOLD_ITALIC = FontManager.INSTANCE.getFont(FontWeight.Bold, true, _OFFSET);

	static private final byte _H_GAP = 4;
	static private final byte _RIGHT_GUTTER = 2;
	static private final byte _STICKING_PADDING = 1;

	private ArrayList<Node> _blocks;
	private ArrayList<Node> _stuck;
	private boolean _brief;

	//TODO utiliser une autre police/icône/shape pour les " . ,
	//TODO Control Node::_input_event, set_as_subwindow, is_inside_scene
	//TODO MeshInstance Scenario
	//TODO trouver d'abord les chaînes entre guillemets


	Paragraph(@Nullable Type host, String text, boolean brief)
	{
		super();
		_brief = brief;
		if (text == null) return;

		text = text.replaceAll(_REMOVABLES_RE, Strings.EMPTY); // images, todos et espaces finaux
		if (text.isEmpty()) return;

		_blocks = new ArrayList<Node>();
		_findTags(host, text, false, false);

		if (_blocks.size() > 0)
		{
			_blocks.trimToSize();
			if (_stuck != null) _stuck.trimToSize();
			super.getChildren().addAll(_blocks);
		}
		else _blocks = null;
	}


	boolean isEmpty() { return _blocks == null; }


	@Override
	public void setHSize(short size)
	{
		if (_blocks == null) return;

		size -= _RIGHT_GUTTER;
		double x = 0;
		double y = 0;
		double r; // limite droite
		boolean s; // collé
		double w;
		double h = 0; // hauteur d'un mot regular (quand le dernier mot est un code)
		Node p = null;

		for (Node i : _blocks)
		{
			if (i instanceof Text)
			{
				h = i.getBoundsInLocal().getHeight();
				break;
			}
		}

		if (!_brief) // description longue
		{
			Bounds b;

			for (Node i : _blocks)
			{
				b = i.getBoundsInLocal();
				w = b.getWidth();

				if (p != null)
				{
					s = _stuck != null && _stuck.contains(i);
					if (!s) x += _H_GAP;
					r = x + w;

					if (r > size) // fin de ligne
					{
						x = 0;
						y += h;
						r = w;

						if (s) // bloc collé
						{
							p.relocate(x, y);
							x = p.getBoundsInLocal().getWidth() + _STICKING_PADDING;
							r += x;
						}
					}

					if (i instanceof CodeBlock) i.relocate(x, y + (h - b.getHeight()) / 2);
					else i.relocate(x, y);
				}
				else // premier mot du paragraphe
				{
					r = w;
				}

				x = r;
				p = i;
			}

			super.vSize = (short) (y + h);
		}
		else // description courte
		{
			for (Node i : _blocks)
			{
				w = i.getBoundsInLocal().getWidth();

				if (p != null)
				{
					s = _stuck != null && _stuck.contains(i);
					if (!s) x += _H_GAP;
					i.setLayoutX(x);
				}

				x += w;
				p = i;
			}

			super.vSize = (short) h;
		}

	}


	private void _addFirstBlock(Node b)
	{
		if (b instanceof Text && ((Text) b).getText().matches(_END_PUNCT_RE)) // [*].,;:?!)
		{
			if (_stuck == null) _stuck = new ArrayList<Node>(1);
			_stuck.add(b);
		}
		else
		{
			Node p = _blocks.size() > 0 ? _blocks.get(_blocks.size() - 1) : null;

			if (p instanceof Text && ((Text) p).getText().equals(String.valueOf(Strings.LEFT_PARENTHESIS))) // ([*]
			{

				if (_stuck == null) _stuck = new ArrayList<Node>(1);
				_stuck.add(b);
			}
		}

		_blocks.add(b);
	}


	private void _findLinks(Type o, String s, Font f)
	{
		if (s == null || s.isEmpty()) return;

		Matcher m = _LINK_PATTERN.matcher(s);
		UIType u;
		String v, w;
		Type t;
		int e = 0;
		int i;

		while (m.find())
		{
			if (m.start() > e) _splitWords(s.substring(e, m.start()), f); // texte précédent
			v = m.group(_CONTENT_GROUP);
			u = null;

			if (v.matches(_LONG_MEMBER_RE)) // method Type.name
			{
				i = v.indexOf(Strings.DOT);
				w = v.substring(v.lastIndexOf(Strings.SPACE) + 1, i);
				t = Documentation.INSTANCE.getType(w);
				if (t == null) t = Documentation.INSTANCE.getWideType(w);
				if (t != null) u = new UIType(t, v.substring(i + 1), o != null && t == o);
			}
			else if (v.matches(_INTERNAL_RE)) // method name
			{
				if (o != null) u = new UIType(o, v.substring(v.lastIndexOf(Strings.SPACE) + 1), true);
			}
			else if (v.matches(_SHORT_MEMBER_RE)) // Type.name
			{
				i = v.indexOf(Strings.DOT);
				w = v.substring(0, i);
				t = Documentation.INSTANCE.getType(w);
				if (t == null) t = Documentation.INSTANCE.getWideType(w);
				if (t != null) u = new UIType(t, v.substring(i + 1), o != null && t == o);
			}
			else if (v.matches(_TYPE_RE)) // Type
			{
				t = Documentation.INSTANCE.getType(v);
				if (t == null) t = Documentation.INSTANCE.getWideType(v);
				if (t != null) u = new UIType(t);
			}
			else if (v.matches(_MEMBER_RE)) // name
			{
				if (o != null) u = new UIType(o, v, true);
			}

			if (u != null) _addFirstBlock(u);
			else _splitWords(m.group(), f); // conversion en texte brut

			e = m.end();
		}

		if (e < s.length()) _splitWords(e == 0 ? s : s.substring(e), f);
	}


	private void _findTags(Type o, String s, boolean b, boolean i)
	{
		Matcher m = _TAG_PATTERN.matcher(s);
		int e = 0;
		String n, v;
		Font f = b ? (i ? _BOLD_ITALIC : _BOLD) : (i ? _ITALIC : _REGULAR);

		while (m.find())
		{
			if (m.start() > e) _findLinks(o, s.substring(e, m.start()), f); // texte précédent
			n = m.group(_TAG_GROUP);

			if (!n.toLowerCase().matches(_IGNORED_TAGS_RE))
			{
				v = m.group(_CONTENT_GROUP);

				if (n.matches(_BOLD_TAG_RE))
				{
					if (i) _findLinks(o, v, _BOLD_ITALIC);
					else _findTags(o, v, true, false);
				}
				else if (n.matches(_ITALIC_TAG_RE))
				{
					if (b) _findLinks(o, v, _BOLD_ITALIC);
					else _findTags(o, v, false, true);
				}
				else if (n.matches(_CODE_TAG_RE))
				{
					_splitWords(v, null);
				}
				else // balise inconnue
				{
					_splitWords(m.group(), f);
				}
			}

			e = m.end();
		}

		if (e < s.length()) _findLinks(o, e == 0 ? s : s.substring(e), f);
	}


	private void _splitWords(String s, Font f) // f = null -> monospaced
	{
		Matcher m = _WORD_PATTERN.matcher(s);
		String v;
		boolean i = true;

		while (m.find())
		{
			v = m.group(_CONTENT_GROUP);

			if (f == null)
			{
				_blocks.add(new CodeBlock(v));
			}
			else if (i)
			{
				_addFirstBlock(Graphics.CREATE_TEXT_FIELD(v, f));
				i = false;
			}
			else
			{
				_blocks.add(Graphics.CREATE_TEXT_FIELD(v, f));
			}
		}
	}
}