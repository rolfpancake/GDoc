package ui.descriptions;


import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import javafx.collections.ObservableList;
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
import static main.Debug.trace;

//TODO utiliser une autre police/icône/shape pour les " . ,
//TODO Control Node::_input_event, set_as_subwindow, is_inside_scene / MeshInstance Scenario / Node::get_process_time
//TODO trouver d'abord les chaînes entre guillemets

/**
 * Classe de base abstraite des paragraphes.
 */
class Paragraph extends TextArea
{
	static protected final byte STICKING_PADDING = 1; // concerne aussi les blocs sans espace précédent donc 1px max
	static protected final byte H_GAP = 4;

	static protected byte REGULAR_HEIGHT = 0; // hauteur d'un mot regular (quand le dernier mot est un code)

	static private final String _REMOVABLES_RE = "\\[image .*?/\\]|TODO.*?(\n|$)|\\p{Space}$";
	static private final String _IGNORED_TAGS_RE = "center|img";
	static private final String _ITALIC_TAG_RE = "i|I";
	static private final String _BOLD_TAG_RE = "b|B";
	static private final String _CODE_TAG_RE = "code";
	static private final String _CONTENT_RE = "(?<C>.*?)";
	static private final String _TAG_RE = "\\[(?<T>[a-zA-Z]+)\\]" + _CONTENT_RE + "\\[/\\k<T>\\]";
	static private final String _LINK_RE = "\\[(?<C>.*?)\\]";
	static private final String _MEMBER_RE = "[a-zA-Z_][\\w_]*";
	static private final String _TYPE_RE = "[a-zA-Z@][\\w _@]*";
	static private final String _LONG_MEMBER_RE = "method +" + _TYPE_RE + "\\." + _MEMBER_RE;
	static private final String _INTERNAL_RE = "method +" + _MEMBER_RE;
	static private final String _SHORT_MEMBER_RE = _TYPE_RE + "\\." + _MEMBER_RE;

	static private final String _END_PUNCT_RE = "[\\.,;:?!)]+";
	static private final Pattern _TAG_PATTERN = Pattern.compile(_TAG_RE);
	static private final Pattern _LINK_PATTERN = Pattern.compile(_LINK_RE);
	/* les tabulations antérieures sont acceptées */
	static private final Pattern _WORD_PATTERN = Pattern.compile("[^ ]+| ");

	static private final String _CONTENT_GROUP = "C";
	static private final String _TAG_GROUP = "T";
	static private final byte _OFFSET = 1;
	static private final Font _REGULAR = FontManager.INSTANCE.getFont(_OFFSET);
	static private final Font _ITALIC = FontManager.INSTANCE.getFont(FontWeight.Regular, true, _OFFSET);
	static private final Font _BOLD = FontManager.INSTANCE.getFont(FontWeight.Bold, _OFFSET);
	static private final Font _BOLD_ITALIC = FontManager.INSTANCE.getFont(FontWeight.Bold, true, _OFFSET);

	static private class TEMP
	{
		ArrayList<Node> blocks;
		Type host;
	}

	protected Node[] blocks; // null pour 1 espace unique (n espaces), 2 blocs successifs sont collés
	private boolean _initialized = false;


	Paragraph(@Nullable Type host, @NotNull String text)
	{
		super(text);

		text = text.replaceAll(_REMOVABLES_RE, Strings.EMPTY); // images, todos et espaces finaux
		if (text.isEmpty()) return;

		TEMP d = new TEMP();
		d.blocks = new ArrayList<Node>();
		d.host = host;
		_findTags(d, text, false, false);
		if (d.blocks.size() > 0) blocks = d.blocks.toArray(new Node[d.blocks.size()]);

		_initialized = true;
		super.init();
	}


	@Override
	public void setWidth(short width)
	{
		if (blocks == null) return;
		width = (short) Math.max(0, width);
		if (width != super.getWidth()) super.setWidth(width);
	}


	/**
	 * Récupére l'abscisse droite du dernier mot.
	 */
	short getXOffset()
	{
		return blocks == null ? 0 : (short) blocks[blocks.length - 1].getBoundsInParent().getMaxX();
	}


	/**
	 * Récupére l'ordonnée de la ligne de base du dernier mot.
	 */
	short getYOffset()
	{
		if (blocks == null) return 0;
		Node b = blocks[blocks.length - 1];
		return (short) (b.getLayoutY() + b.getBaselineOffset());
	}


	@Override
	public final boolean isEmpty() { return blocks == null; }


	boolean isMonoLine() { return false; }


	@Override
	protected short computeHeight()
	{
		short m = (short) Math.max(0, super.getWidth()); // x max
		Node p = null; // bloc ou espace  précédent
		short n = 0; // nombre de mots sur la ligne en cours (boucle infinie avec une faible largeur)
		double x = 0;
		double y = 0;
		double r; // limite droite
		double w;
		Bounds b;

		for (Node i : blocks)
		{
			if (i == null) // espace
			{
				x += H_GAP;
				p = null;
				n += 1;
				continue;
			}

			b = i.getBoundsInLocal();
			w = b.getWidth();

			if (p != null) x += STICKING_PADDING; // bloc collé
			r = x + w;

			if (r > m && n > 0) // fin de ligne
			{
				n = 0;
				x = 0;
				y += REGULAR_HEIGHT;
				r = w;

				if (p != null) // bloc collé
				{
					p.relocate(x, y); // déplacement du bloc précédent sur la ligne suivante
					x = p.getBoundsInLocal().getWidth() + STICKING_PADDING;
					r += x;
				}
			}

			if (i.getParent() == null) super.getChildren().add(i);
			if (i instanceof CodeBlock) i.relocate(x, y + (REGULAR_HEIGHT - b.getHeight()) / 2);
			else i.relocate(x, y);

			x = r;
			p = i;
			n += 1;
		}

		return (short) blocks[blocks.length - 1].getBoundsInParent().getMaxY();
	}


	@Override
	protected boolean isInitialized() { return _initialized; }


	@Override
	protected void initDisplay()
	{
		if (blocks != null)
		{
			ObservableList<Node> c = super.getChildren();
			for (Node i : blocks) if (i != null) c.add(i);
		}
	}


	@Override
	protected void initLayout()
	{
		if (REGULAR_HEIGHT == 0 && blocks != null)
		{
			for (Node i : blocks)
			{
				if (i instanceof Text)
				{
					REGULAR_HEIGHT = (byte) i.getBoundsInLocal().getHeight();
					break;
				}
			}

			if (REGULAR_HEIGHT == 0) REGULAR_HEIGHT = (byte) blocks[0].getBoundsInLocal().getHeight();
		}
	}


	/*private void _addFirstBlock(TEMP d, Node b)
	{
		if (b instanceof Text && ((Text) b).getText().matches(_END_PUNCT_RE)) // [*].,;:?!)
		{
			trace("Collé_ponctuation_finale", ((Text) b).getText());
			d.blocks.add(null);
			d.blocks.add(b);
		}
		else
		{
			Node p = d.blocks.size() > 0 ? d.blocks.get(d.blocks.size() - 1) : null;

			if (p instanceof Text && ((Text) p).getText().equals(String.valueOf(Strings.LEFT_PARENTHESIS))) // ([*]
			{
				trace("Collé_parenthèse_précédente", ((Text) b).getText());
				d.blocks.add(null);
				d.blocks.add(b);
			}
			else
			{
				d.blocks.add(b);
			}
		}
	}*/


	private void _findLinks(TEMP d, String s, Font f)
	{
		if (s == null || s.isEmpty()) return;

		Matcher m = _LINK_PATTERN.matcher(s);
		UIType u;
		String v, w;
		Type t;
		int e = 0;
		int i;
		//trace("findLinks >" + s + "<");
		while (m.find())
		{
			if (m.start() > e) _splitWords(d, s.substring(e, m.start()), f); // texte précédent
			v = m.group(_CONTENT_GROUP);
			u = null;
			//trace("Link", v);
			if (v.matches(_LONG_MEMBER_RE)) // method Type.name
			{
				i = v.indexOf(Strings.DOT);
				w = v.substring(v.lastIndexOf(Strings.SPACE) + 1, i);
				t = Documentation.INSTANCE.getType(w);
				if (t == null) t = Documentation.INSTANCE.getWideType(w);
				if (t != null) u = new UIType(t, v.substring(i + 1), d.host != null && t == d.host);
			}
			else if (v.matches(_INTERNAL_RE)) // method name
			{
				if (d.host != null) u = new UIType(d.host, v.substring(v.lastIndexOf(Strings.SPACE) + 1), true);
			}
			else if (v.matches(_SHORT_MEMBER_RE)) // Type.name
			{
				i = v.indexOf(Strings.DOT);
				w = v.substring(0, i);
				t = Documentation.INSTANCE.getType(w);
				if (t == null) t = Documentation.INSTANCE.getWideType(w);
				if (t != null) u = new UIType(t, v.substring(i + 1), d.host != null && t == d.host);
			}
			else if (v.matches(_TYPE_RE)) // Type
			{
				t = Documentation.INSTANCE.getType(v);
				if (t == null) t = Documentation.INSTANCE.getWideType(v);
				if (t != null) u = new UIType(t);
			}
			else if (v.matches(_MEMBER_RE)) // name
			{
				if (d.host != null) u = new UIType(d.host, v, true);
			}

			//if (u != null) _addFirstBlock(d, u);
			if (u != null) d.blocks.add(u);
			else _splitWords(d, m.group(), f); // conversion en texte brut (+ crochets)

			e = m.end();
		}

		if (e < s.length()) _splitWords(d, e == 0 ? s : s.substring(e), f);
	}


	private void _findTags(TEMP d, String s, boolean b, boolean i)
	{
		Matcher m = _TAG_PATTERN.matcher(s);
		int e = 0;
		String n, v;
		Font f = b ? (i ? _BOLD_ITALIC : _BOLD) : (i ? _ITALIC : _REGULAR);

		while (m.find())
		{
			if (m.start() > e) _findLinks(d, s.substring(e, m.start()), f); // texte précédent
			n = m.group(_TAG_GROUP);

			if (!n.toLowerCase().matches(_IGNORED_TAGS_RE))
			{
				v = m.group(_CONTENT_GROUP);
				//trace("Tag", v);
				if (n.matches(_BOLD_TAG_RE))
				{
					if (i) _findLinks(d, v, _BOLD_ITALIC);
					else _findTags(d, v, true, false);
				}
				else if (n.matches(_ITALIC_TAG_RE))
				{
					if (b) _findLinks(d, v, _BOLD_ITALIC);
					else _findTags(d, v, false, true);
				}
				else if (n.matches(_CODE_TAG_RE))
				{
					_splitWords(d, v, null);
				}
				else // balise inconnue
				{
					_splitWords(d, m.group(), f);
				}
			}

			e = m.end();
		}

		if (e < s.length()) _findLinks(d, e == 0 ? s : s.substring(e), f);
	}


	private void _splitWords(TEMP d, String s, Font f) // f = null -> monospaced
	{
		Matcher m = _WORD_PATTERN.matcher(s);
		String v;
		boolean i = true;

		//trace("_splitWords", ">" + s + "<");
		while (m.find())
		{
			v = m.group();

			if (v.equals(Strings.SPACE)) // espace
			{
				d.blocks.add(null);
			}
			else if (f == null) // bloc de code
			{
				d.blocks.add(new CodeBlock(v));
			}
			/*else if (i) // premier mot du fragment
			{
				//trace(">" + v + "<", m.start(), m.regionStart());
				if (m.start() == 0 && d.blocks.size() > 0) // pas d'espace précédent
				{
					Text b = Graphics.CREATE_TEXT_FIELD(v, f);
					//trace("Collé_sans_espace", v);
					d.blocks.add(null);
					d.blocks.add(b);
				}
				else
				{
					_addFirstBlock(d, Graphics.CREATE_TEXT_FIELD(v, f));
				}

				i = false;
			}*/
			else // mot
			{
				d.blocks.add(Graphics.CREATE_TEXT_FIELD(v, f));
			}
		}
	}
}