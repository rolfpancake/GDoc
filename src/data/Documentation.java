package data;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.xml.sax.SAXException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import exceptions.ErrorMessage;
import exceptions.ErrorMessageType;
import main.Language;
import main.Launcher;
import main.Pair;
import main.Paths;
import main.Strings;
import xml.XML;
import xml.XMLTag;


// TODO SpatialGizmo dans Spatial.methods
// TODO Stream dans SpatialStreamPlayer/StreamPlayer/VideoPlayer

public final class Documentation
{
	static public final DocumentBuilder BUILDER = _GET_BUILDER();
	static public final Documentation INSTANCE = new Documentation();

	static private final String _GLOBAL_SCOPE_FIX = "@globalscope";
	static private final String _SCENE_TREE_FIX = "scenemainloop";

	private LinkedHashMap<String, Type> _types;
	private ArrayList<Category> _categories;
	private HashMap<Type, Pair<Language, String>> _briefs;
	private HashMap<Type, Trunk> _trunks;
	private String _version;
	private Language _language = Language.en;


	private Documentation()
	{
		try
		{
			XML x = _loadXMLTypes();
			if (x == null) return; // impossible sans exception
			x.parse();

			_version = x.getAttribute(XMLTag.VERSION);
			ArrayList<XML> l = x.getChildren(XMLTag.CLASS);

			_types = new LinkedHashMap<String, Type>(x.getSize());
			_categories = new ArrayList<Category>(2);
			_briefs = new HashMap<Type, Pair<Language, String>>(5);
			_trunks = new HashMap<Type, Trunk>(20);
			_registerStatics();

			String v;
			Type t, s;
			Category c;
			ArrayList<Type> u = new ArrayList<Type>(); // types sans catégorie

			for (XML i : l)
			{
				t = new Type(i.getAttribute(XMLTag.NAME)); // validation du nom
				v = t.getName();
				if (v == null) continue;

				if (_types.containsKey(v))
				{
					t = _types.get(v);

					if (t.getCategory() != null)
					{
						Launcher.INSTANCE.log(new ErrorMessage(ErrorMessageType.duplicatedType, v));
						continue;
					}
				}
				else
				{
					_types.put(v, t);
				}

				_setCategory(t, i.getAttribute(XMLTag.CATEGORY));
				u.remove(t);

				v = i.getAttribute(XMLTag.INHERITS);
				if (v == null) continue;
				v = v.trim();
				if (v.isEmpty()) continue;
				s = _types.get(v);

				if (s == null)
				{
					s = new Type(v);
					v = s.getName();
					if (v == null) continue;
					_types.put(v, s);
					u.add(s);
				}

				t.setSuper(s);
				s.addSubType(t);
			}

			for (Type i : u) i.setCategory(Category.CORE);
			_sort(); // une liste ordonnée est nécessaire à deux endroits
		}
		catch (Exception e)
		{
			Launcher.INSTANCE.raise(e);
		}
	}


	static private DocumentBuilder _GET_BUILDER()
	{
		try
		{
			return DocumentBuilderFactory.newInstance().newDocumentBuilder();
		}
		catch (ParserConfigurationException e)
		{
			Launcher.INSTANCE.raise(e);
		}

		return null;
	}


	/**
	 * Récupére une description courte de classe.
	 *
	 * @param type Type associé
	 * @return Un objet String non vide ou null
	 */
	public String getClassBrief(@Nullable Type type)
	{
		if (type == null) return null;
		Pair<Language, String> b = null;

		try
		{
			if (!_briefs.containsKey(type)) b = _getBrief(type);
		}
		catch (Exception e)
		{
			Launcher.INSTANCE.log(e);
			return null;
		}

		if (b == null) return null;
		_briefs.put(type, b);
		return b.getValue();
	}


	/**
	 * Récupére les descriptions brèves de toutes les classes.
	 *
	 * @return 	Une liste ordonnée (majuscules avant minuscules) où tous les types sont définis et leur description
	 * 			courte définie ou nulle
	 */
	@NotNull
	public LinkedHashMap<Type, String> getClassBriefs()
	{
		LinkedHashMap<Type, String> l = new LinkedHashMap<Type, String>(_types.size());
		Pair<Language, String> b;

		for (Type i : _types.values())
		{
			if (_briefs.containsKey(i))
			{
				b = _briefs.get(i);
			}
			else
			{
				b = null;

				try
				{
					b = _getBrief(i);
				}
				catch (Exception e)
				{
					Launcher.INSTANCE.log(e);
				}

				_briefs.put(i, b);
			}

			l.put(i, b != null ? b.getValue() : null);
		}

		return l;
	}


	public Category[] getCategories() { return _categories.toArray(new Category[_categories.size()]); }


	public Category getCategory(@Nullable String name)
	{
		if (name == null || name.isEmpty()) return null;
		for (Category i : _categories) if (name.equals(i.getName())) return i;
		return null;
	}


	/**
	 * Récupére la classe de tout type de données.
	 *
	 * @return Un type ou null
	 */
	@Nullable
	public Type getDataType(@Nullable Described described)
	{
		if (described == null) return null;
		if (described instanceof Trunk) return ((Trunk) described).getType();
		if (described instanceof Return) described = ((Return) described).getMethod();
		if (!(described instanceof Identifier)) return null; // warning

		Group g = ((Identifier) described).getGroup();
		Trunk k = g != null ? g.getTrunk() : null;
		return k != null ? k.getType() : null;
	}


	/**
	 * Récupére le premier type commençant par une lettre.
	 *
	 * @return Un type ou null
	 */
	@Nullable
	public Type getFirstType()
	{
		int c;

		for (String i : _types.keySet())
		{
			c = i.codePointAt(0);
			if (c > 64 && c < 91) return _types.get(i);
		}

		return null;
	}


	@NotNull
	public Language getLanguage() { return _language; }


	public short getSize() { return (short) _types.size(); }


	/**
	 * Récupére un type par son nom en respectant la casse.
	 *
	 * @param name Nom
	 * @return Un objet Type ou null
	 */
	@Nullable
	public Type getType(@Nullable String name) { return _types.get(name); }


	/**
	 * Récupére une liste ordonnée de tous les types.
	 *
	 * @return Un tableau de types non null de longueur > 0
	 */
	@NotNull
	public Type[] getTypes() { return _types.values().toArray(new Type[_types.size()]); }


	/**
	 * Récupére un tronc.
	 *
	 * @param type Type
	 * @return Un objet Trunk, ou null s'il ne contient ni API ni description ou en cas d'erreur
	 */
	@Nullable
	public Trunk getTrunk(@Nullable Type type)
	{
		if (type == null) return null;
		if (_trunks.containsKey(type)) return _trunks.get(type);
		Trunk t = new Trunk(type);
		if (!t.hasGroup(null) && t.description == null) return null; // sans description Trunk charge son brief
		_trunks.put(type, t);
		return t;
	}


	@Nullable
	public String getVersion() { return _version; }


	/**
	 * Récupére un type par son nom en ignorant la casse et les espaces. Permet de retrouver un type avec un nom
	 * ayant une faute de casse.
	 *
	 * @param name Nom
	 * @return Un objet Type ou null
	 */
	@Nullable
	public Type getWideType(@Nullable String name)
	{
		if (name == null) return null;
		name = name.toLowerCase().replace(Strings.SPACE, Strings.EMPTY);
		if (name.equals(_GLOBAL_SCOPE_FIX)) return Type.GLOBAL_SCOPE;
		if (name.equals(_SCENE_TREE_FIX)) return getType(Type.SCENE_TREE_TYPE_NAME);
		for (String i : _types.keySet()) if (name.equals(i.toLowerCase())) return _types.get(i);
		return null;
	}


	public void removeTrunk(@Nullable Type type)
	{
		if (type == null) return;
		_briefs.remove(type);
		_trunks.remove(type);
	}


	public void setLanguage(@Nullable Language value)
	{
		if (value == null || value == _language) return;
		_language = value;
	}


	@Nullable
	private Pair<Language, String> _getBrief(@NotNull Type t)
	{
		String n = t.getName();
		Path d = Paths.GET_CLASS_PATH(t);
		if (d == null) return null;

		Language l = _language;
		Path lp = d.resolve(l.toString());
		Path fp = null;

		if (!Paths.CHECK_DIRECTORY(lp))
		{
			if (l == Language.en) return null;
			l = Language.en;
			lp = d.resolve(l.toString());
			if (!Paths.CHECK_DIRECTORY(lp)) return null;
		}

		XML x = Paths.OPEN(lp.resolve(XMLTag.BRIEF + ".xml"));

		if (x == null)
		{
			if (l == Language.en) return null;
			l = Language.en;
			lp = d.resolve(l.toString());
			if (!Paths.CHECK_DIRECTORY(lp)) return null;
			fp = lp.resolve(XMLTag.BRIEF + Paths.XML);
			x = Paths.OPEN(fp);
			if (x == null) return null;
		}

		x.parse();
		String v = Strings.CLEAN(x.getAttribute(XMLTag.NAME));

		if (v == null || !v.equals(n))
			Launcher.INSTANCE.log(new ErrorMessage(ErrorMessageType.invalidName, fp, v));
		v = Strings.CLEAN(x.getValue());
		return v != null ? new Pair<Language, String>(l, v) : null;
	}


	@Nullable
	private XML _loadXMLTypes() throws IOException, SAXException
	{
		if (!Paths.CHECK_DATA_DIRECTORY())
		{
			Launcher.INSTANCE.raise(new ErrorMessage(ErrorMessageType.directoryAccessDenied, Paths.DATA_DIR));
			return null;
		}

		Path p = Paths.DATA_DIR.resolve("types.xml");

		if (Files.notExists(p) || Files.isDirectory(p))
		{
			Launcher.INSTANCE.raise(new ErrorMessage(ErrorMessageType.fileNotFound, p));
			return null;
		}

		if (!Files.isReadable(p))
		{
			Launcher.INSTANCE.raise(new ErrorMessage(ErrorMessageType.fileAccessDenied, p));
			return null;
		}

		InputStream s = Files.newInputStream(p, StandardOpenOption.READ);
		XML x = new XML(BUILDER.parse(s).getDocumentElement());
		s.close();
		return x;
	}


	private void _registerStatics()
	{
		_categories.add(Category.BUILT_IN);
		_categories.add(Category.CORE);
		_types.put(Type.ARRAY.getName(), Type.ARRAY);
		_types.put(Type.BOOL.getName(), Type.BOOL);
		_types.put(Type.FLOAT.getName(), Type.FLOAT);
		_types.put(Type.INT.getName(), Type.INT);
		_types.put(Type.NIL.getName(), Type.NIL);
		_types.put(Type.OBJECT.getName(), Type.OBJECT);
		_types.put(Type.GD_SCRIPT.getName(), Type.GD_SCRIPT);
		_types.put(Type.GLOBAL_SCOPE.getName(), Type.GLOBAL_SCOPE);
		_types.put(Type.STRING.getName(), Type.STRING);
	}


	private void _setCategory(@NotNull Type t, @Nullable String n)
	{
		String v;
		Category c = null;

		if (n != null)
		{
			v = n.trim();
			if (!v.isEmpty()) c = getCategory(v);

			if (c == null)
			{
				try
				{
					c = new Category(v);
					_categories.add(c);
				}
				catch (Exception e)
				{
					Launcher.INSTANCE.log(e);
				}
			}
		}

		if (c == null) c = Category.CORE;
		t.setCategory(c);
		c.addType(t);
	}


	private void _sort()
	{
		ArrayList<String> n = new ArrayList<String>(_types.size());
		short k;

		for (String i : _types.keySet())
		{
			k = 0;

			for (String j : n)
			{
				if (i.compareTo(j) < 0) break;
				k += 1;
			}

			n.add(k, i);
		}

		LinkedHashMap<String, Type> l = new LinkedHashMap<String, Type>(_types.size());
		for (String i : n) l.put(i, _types.get(i));
		_types = l;
	}
}
