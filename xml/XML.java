package xml;

import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import exceptions.EmptyArgumentException;
import exceptions.NullArgumentException;


/**
 * Un nœud XML. Les espaces inutiles ne sont supprimés que dans les noms de balise.
 */
public final class XML
{
	static private final String _TAG_PATTERN = "[<>'\"&]";
	static private final String[] _ESCAPING = {"<", "&lt;", ">", "&gt;", "\"", "&quot;", "'", "&apo;",
											   "&", "&amp;"};

	private String _tag;
	private ArrayList<XML> _children;
	private LinkedHashMap<String, String> _attributes;
	private String _value = "";
	private WeakReference<XML> _parent;
	private Node _data;
	private byte _state = 3;


	public XML(String tag)
	{
		if (tag == null) throw new NullArgumentException();
		tag = tag.trim();
		if (tag.isEmpty()) throw new EmptyArgumentException();
		if (tag.matches(_TAG_PATTERN)) throw new IllegalArgumentException("invalid tag name");
		_tag = tag;
	}


	public XML(String tag, String value)
	{
		if (tag == null) throw new NullArgumentException();
		tag = tag.trim();
		if (tag.isEmpty()) throw new EmptyArgumentException("tag");
		if (tag.matches(_TAG_PATTERN)) throw new IllegalArgumentException("invalid tag name");
		_tag = tag;
		_value = value == null ? "" : value;
	}


	public XML(Element root)
	{
		if (root == null) throw new NullArgumentException();
		_tag = root.getTagName();
		_data = root;
		_state = 0;
	}


	public XML(Node node)
	{
		if (node == null) throw new NullArgumentException();
		_tag = node.getNodeName().trim();
		_data = node;
		_state = 0;
	}


	static public String ESCAPE(String value)
	{
		if (value == null || value.isEmpty()) return value;
		for (int i = 0; i < _ESCAPING.length; i += 2) value = value.replaceAll(_ESCAPING[i], _ESCAPING[i + 1]);
		return value;
	}


	static public String UNESCAPE(String value)
	{
		if (value == null || value.isEmpty()) return value;
		for (int i = 1; i < _ESCAPING.length; i += 2) value = value.replaceAll(_ESCAPING[i], _ESCAPING[i - 1]);
		return value;
	}


	public void addAttribute(String name, String value)
	{
		if (name == null) throw new NullArgumentException();
		if (name.isEmpty()) throw new EmptyArgumentException("name");
		if (_data != null) throw new RuntimeException("node not parsed");
		if (value == null) value = "";
		if (_attributes == null) _attributes = new LinkedHashMap<String, String>(1);
		if (_attributes.containsKey(name)) _attributes.replace(name, value);
		else _attributes.put(name, value);
	}


	public void addChild(XML node)
	{
		if (node == null) throw new NullArgumentException();
		if (node._parent != null && node._parent.get() == this) return;
		if (_data != null) throw new RuntimeException("node not parsed");
		if (_children == null) _children = new ArrayList<XML>();

		if (node._parent != null)
		{
			XML p = node._parent.get();
			if (p != null) p.removeChild(node);
		}

		_children.add(node);
		node._parent = new WeakReference<XML>(this);
		_value = null;
	}


	public void addChild(XML node, int index)
	{
		if (node == null) throw new NullArgumentException();
		if (node._parent != null && node._parent.get() == this) return;
		if (_data != null) throw new RuntimeException("node not parsed");
		if (_children == null) _children = new ArrayList<XML>();

		if (node._parent != null)
		{
			XML p = node._parent.get();
			if (p != null) p.removeChild(node);
		}

		index = Math.max(0, Math.min(index, _children.size()));
		_children.add(index, node);
		node._parent = new WeakReference<XML>(this);
		_value = null;
	}


	public void clearAttributes()
	{
		if (_attributes != null)
		{
			_attributes.clear();
			_attributes = null;
		}
	}


	public String getAttribute(String name) { return _attributes == null ? null : _attributes.get(name); }


	public XML getChild(int index)
	{
		return _children != null && index > -1 && index < _children.size() ? _children.get(index) : null;
	}


	public ArrayList<XML> getChildren()
	{
		if (_children == null) return new ArrayList<XML>(0);
		ArrayList<XML> l = new ArrayList<XML>(_children.size());
		l.addAll(_children);
		return l;
	}


	public ArrayList<XML> getChildren(String tag)
	{
		ArrayList<XML> c = new ArrayList<XML>();
		if (_children == null || tag == null || tag.isEmpty()) return c;
		for (XML i : _children) if (i.getTag().equals(tag)) c.add(i);
		return c;
	}


	/**
	 * Récupère le premier nœud enfant par son nom de balise.
	 * Cette méthode est utile quand on sait qu'un nœud ne contient, ou ne devrait contenir, qu'un seul enfant
	 * portant ce nom parmi d'autres enfants de nom différent.
	 *
	 * @param tag Nom de balise
	 * @return Un nœud XML ou null
	 */
	public XML getFirstChild(String tag)
	{
		if (_children == null || tag == null || tag.isEmpty()) return null;
		for (XML i : _children) if (tag.equals(i.getTag())) return i;
		return null;
	}


	public XML getParent() { return _parent != null ? _parent.get() : null; }


	public XML getRoot()
	{
		if (_parent != null)
		{
			XML p = _parent.get();
			if (p != null) return p.getRoot();
		}

		return this;
	}


	public int getSize() { return _children == null ? 0 : _children.size(); }


	public int getSize(boolean attributes)
	{
		return attributes ? (_attributes == null ? 0 : _attributes.size()) : getSize();
	}


	/**
	 * Récupère l'état du nœud.
	 * 0: non parsé, 1: attributs parsés, 2: nœuds parsés, 3: attributs et nœuds parsés
	 */
	public byte getState() { return _state; }


	public String getTag() { return _tag; }


	/**
	 * Récupère la valeur du nœud.
	 *
	 * @return La valeur du nœud ou une chaîne vide si le nœud est terminal, ou null si le nœud est parent
	 */
	public String getValue() { return _value; }


	public int indexOf(XML node) { return _children == null ? -1 : _children.indexOf(node); }


	/**
	 * Parse toute l'arborescence.
	 */
	public void parse()
	{
		_parseAttributes();
		_parseChildren(true, true);
	}


	/**
	 * Parse sélectivement.
	 *
	 * @param attributes         Parser les attributs
	 * @param children           Parser les nœuds enfants
	 * @param childrenAttributes Parser les attributs des nœuds enfants (ignoré si children vaut false)
	 * @param recursively        Parser toute l'arborescence de nœuds sous ce nœud (ignoré si children vaut false)
	 */
	public void parse(boolean attributes, boolean children, boolean childrenAttributes, boolean recursively)
	{
		if (attributes) _parseAttributes();
		if (children) _parseChildren(childrenAttributes, recursively);
	}


	public void removeAttribute(String... name)
	{
		if (_attributes == null || name == null) return;
		for (String i : name) _attributes.remove(i);
		if (_attributes.size() == 0) _attributes = null;
	}


	public void removeChild(XML... node)
	{
		if (_children == null || node == null) return;

		for (XML i : node) if (i != null && _children.remove(i)) i._parent = null;

		if (_children.size() == 0)
		{
			_children = null;
			_value = "";
		}
	}


	/**
	 * Supprime les nœuds enfants et définit la valeur.
	 *
	 * @param value Valeur
	 */
	public void setValue(String value)
	{
		if (_children != null)
		{
			for (XML i : _children) i._parent = null;
			_children.clear();
			_children = null;
		}

		_value = value == null ? "" : value;
		_data = null;
	}


	/**
	 * Récupère l'occurrence sous la forme d'une chaîne de caractères indentée et non échappée.
	 *
	 * @return Une chaîne de caractères
	 */
	@Override
	public String toString()
	{
		return _toString("", false);
	}


	/**
	 * Récupère l'occurrence sous la forme d'une chaîne de caractères échappée.
	 *
	 * @param indent Indenter
	 * @return Une chaîne de caractères XML
	 */
	public String toXMLString(boolean indent)
	{
		return _toString(indent ? "" : null, true);
	}


	/**
	 * Parse les attributs.
	 */
	private void _parseAttributes()
	{
		if (_state == 3 || _state == 1) return;

		NamedNodeMap l = _data.getAttributes(); // désordonnés (ou classés alphabétiquement ?)
		if (l == null) return;
		int n = l.getLength();
		int i = 0;
		Node t;

		while (i < n)
		{
			t = l.item(i);

			if (t.getNodeType() == Node.ATTRIBUTE_NODE)
			{
				if (_attributes == null) _attributes = new LinkedHashMap<String, String>(n);
				if (_attributes.containsKey(t.getNodeName()))
					_attributes.replace(t.getNodeName(), t.getNodeValue());
				else
					_attributes.put(t.getNodeName(), t.getNodeValue());
			}

			i += 1;
		}

		_state += 1;
		if (_state == 3) _data = null;
	}


	/**
	 * Parse les enfants
	 */
	private void _parseChildren(boolean a, boolean r)
	{
		if (_state > 1) return;

		NodeList l = _data.getChildNodes();
		Node t;
		XML x;
		int i = 0;
		int n = l.getLength();

		if (n > 1) // parent
		{
			while (i < n)
			{
				t = l.item(i);

				if (t.getNodeType() == Node.ELEMENT_NODE) // parent ou terminal
				{
					if (_children == null) _children = new ArrayList<XML>(n);
					x = new XML(t);

					if (a) x._parseAttributes();
					if (r) x._parseChildren(a, true);

					_children.add(x);
					x._parent = new WeakReference<XML>(this);
				}

				i += 1;
			}

			if (_children != null)
			{
				_children.trimToSize();
				_value = null;
			}
		}
		else if (l.item(0) != null)// terminal
		{
			_value = l.item(0).getNodeValue();
		}

		_state += 2;
		if (_state == 3) _data = null;
	}


	/**
	 * Récupère une chaîne de caractères indentée échappée.
	 *
	 * @param t Tabulations antérieures (null pour aucunes)
	 * @param e Échapper
	 */
	private String _toString(String t, boolean e)
	{
		String s = (t == null ? "" : t) + "<" + _tag;

		if (_attributes != null)
		{
			for (String i : _attributes.keySet())
				s += " " + i + "=\"" + (e ? ESCAPE(_attributes.get(i)) : _attributes.get(i)) + "\"";
		}

		if (_children == null)
		{
			if (_value == null || _value.isEmpty()) return s + "/>";
			s += ">" + (e ? ESCAPE(_value) : _value);
		}
		else
		{
			s += ">";
			for (XML i : _children) s += "\n" + i._toString((t == null ? null : t + "\t"), e);
			s += "\n" + t;
		}

		return s + "</" + _tag + ">";
	}
}