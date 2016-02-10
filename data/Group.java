package data;

import java.lang.ref.WeakReference;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import exceptions.ErrorMessage;
import main.Language;
import main.Launcher;
import main.Paths;
import main.Strings;
import xml.XML;
import xml.XMLTag;


/**
 * Un groupe de méthodes, membres, constantes, signaux ou thèmes.
 * Group s'occupe seul des descriptions afin que chaque fichier (1 pour l'API et 0 à 2 pour les descriptions) ne
 * soit chargé qu'une fois.
 */
public final class Group
{
	static private final String _FINAL_S_RE = "s$";

	private GroupType _type;
	private ArrayList<Identifier> _list;
	private WeakReference<Trunk> _trunk;


	Group(@NotNull Trunk trunk, @NotNull GroupType groupType)
	{
		_trunk = new WeakReference<Trunk>(trunk);
		_type = groupType;

		Path cp = Paths.GET_CLASS_PATH(trunk.getType());
		if (cp == null) return;

		String g = groupType.toString();
		String f = g + Paths.XML;
		Path fp = cp.resolve(f);

		XML api = Paths.OPEN(fp);
		if (api == null) return;

		api.parse();
		Type t = trunk.getType();
		String v = Strings.CLEAN(api.getAttribute(XMLTag.NAME));
		if (v == null || (t != null && !v.equals(t.getName()))) Launcher.INSTANCE.log(new ErrorMessage(fp, v));

		String c = g.replaceFirst(_FINAL_S_RE, Strings.EMPTY);
		ArrayList<XML> l = api.getChildren(c);
		int n = l.size();
		if (n == 0) return;

		_list = new ArrayList<Identifier>(n);
		HashMap<String, Identifier> u1 = new HashMap<String, Identifier>(n); // identifiants non décrits
		HashMap<String, HashMap<String, Argument>> u2 = null; // suffix non décrits
		HashMap<String, Return> u3 = null; // retours non décrits

		Identifier id = null;
		Argument a;
		Argument[] a1;
		HashMap<String, Argument> a2;
		Return r;

		// API

		for (XML i : l)
		{
			try
			{
				if (groupType == GroupType.methods) id = new Method(this, i);
				else if (groupType == GroupType.members) id = new Member(this, i);
				else if (groupType == GroupType.constants) id = new Constant(this, i);
				else if (groupType == GroupType.signals) id = new Signal(this, i);
				else if (groupType == GroupType.theme_items) id = new ThemeItem(this, i);
			}
			catch (Exception e)
			{
				Launcher.INSTANCE.log(e);
				continue;
			}

			if (id == null) continue;
			v = id.getName();
			if (v == null) continue;
			_list.add(id);
			u1.put(v, id);

			if (id instanceof Parameterized)
			{
				a1 = ((Parameterized) id).getArguments();
				a2 = null;

				if (a1.length > 0)
				{
					a2 = new HashMap<String, Argument>(a1.length);
					for (Argument j : a1) a2.put(j.getName(), j);
				}

				if (a2 != null)
				{
					if (u2 == null) u2 = new HashMap<String, HashMap<String, Argument>>(n);
					u2.put(v, a2);
				}

				if (id instanceof Method)
				{
					r = ((Method) id).getReturn();

					if (r.getType() != null && r.getType() != Type.NIL)
					{
						if (u3 == null) u3 = new HashMap<String, Return>(n);
						u3.put(v, r);
					}
				}
			}
		}

		_list.trimToSize();
		if (_list.size() == 0) return;

		// Descriptions

		Language[] lg = {Documentation.INSTANCE.getLanguage(), null};
		if (lg[0] != Language.en) lg[1] = Language.en;
		Path lp;
		XML doc, o;
		String d;

		for (Language i : lg)
		{
			if (i == null) return; // documentation en anglais incomplète

			lp = cp.resolve(i.toString());
			fp = lp.resolve(f);
			doc = Paths.OPEN(fp);
			if (doc == null) continue;

			doc.parse();
			v = Strings.CLEAN(doc.getAttribute(XMLTag.NAME)); // nom de classe
			if (v == null || (t != null && !v.equals(t.getName()))) Launcher.INSTANCE.log(new ErrorMessage(fp, v));

			for (XML j : doc.getChildren(c))
			{
				v = Strings.CLEAN(j.getAttribute(XMLTag.NAME)); // nom de méthode
				if (!u1.containsKey(v)) continue;

				id = u1.get(v);
				a2 = u2 != null ? u2.get(v) : null;

				if (id instanceof Parameterized)
				{
					o = j.getFirstChild(XMLTag.DESCRIPTION); // description
					if (o != null) id.description = Strings.CLEAN(o.getValue());

					if (a2 != null) // description des suffix
					{
						for (XML k : j.getChildren(XMLTag.ARGUMENT))
						{
							a = a2.get(Strings.CLEAN(k.getAttribute(XMLTag.NAME)));
							if (a == null) continue;
							a.description = Strings.CLEAN(k.getValue());
							if (a.description != null) a2.remove(a.getName());
						}

						if (a2.size() == 0) u2.remove(v);
					}

					if (id instanceof Method) // description du retour
					{
						o = j.getFirstChild(XMLTag.RETURN);

						if (o != null && u3 != null && u3.containsKey(v))
						{
							r = u3.get(v);
							r.description = Strings.CLEAN(o.getValue());
							if (r.description != null) u3.remove(v);
						}
					}
				}
				else // membre, constante ou thème
				{
					id.description = Strings.CLEAN(j.getValue());
				}

				if (id.description != null) u1.remove(v);
			}

			// Documentation complète (locale, anglaise ou bilingue)
			if (u1.size() == 0 && (u2 == null || u2.size() == 0) && (u3 == null || u3.size() == 0))	return;
		}
	}


	/**
	 * Récupére le type de groupe.
	 */
	@NotNull
	public GroupType getType() { return _type; }


	/**
	 * Récupére une liste désordonnée des identifiants.
	 *
	 * @return Un tableau non vide ou null
	 */
	@Nullable
	public Identifier[] getIdentifiers()
	{
		return _list != null ? _list.toArray(new Identifier[_list.size()]) : null;
	}


	/**
	 * Récupére le nombre d'identifiants.
	 *
	 * @return Un entier >= 0
	 */
	public short getSize() { return _list != null ? (short) _list.size() : 0; }


	/**
	 * Récupére le tronc associé.
	 */
	@Nullable
	public Trunk getTrunk() { return _trunk != null ? _trunk.get() : null; }


	/**
	 * Détermine si un nom d'identifiant est référencé.
	 */
	public boolean hasIdentifier(@Nullable String name)
	{
		if (_list == null || name == null) return false;
		for (Identifier i : _list) if (name.equals(i.getName())) return true;
		return false;
	}
}