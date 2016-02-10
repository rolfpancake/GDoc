package data;

import java.lang.ref.WeakReference;
import java.nio.file.Path;
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
 * Le tronc d'une classe. Il est instancié à la demande uniquement et peut être supprimé.
 */
public final class Trunk extends Described
{
	static private final GroupType[] _GROUPS_TYPES = GroupType.class.getEnumConstants();

	private WeakReference<Type> _type;
	private Group[] _groups;


	Trunk(@NotNull Type type)
	{
		super();
		super.setName(type.getName());
		_type = new WeakReference<Type>(type);
		Path cp = Paths.GET_CLASS_PATH(type);
		if (cp == null) return;
		Path lp = cp.resolve(Documentation.INSTANCE.getLanguage().toString());
		Path fp = lp.resolve(XMLTag.DESCRIPTION + Paths.XML);
		XML x = Paths.OPEN(fp);

		if (x == null) // description longue locale absente
		{
			lp = cp.resolve(Language.en.toString());
			fp = lp.resolve(XMLTag.DESCRIPTION + Paths.XML);
			x = Paths.OPEN(fp);
		}

		if (x != null)
		{
			x.parse();
			String v = Strings.CLEAN(x.getValue());
			if (v != null) super.description = v;
			v = Strings.CLEAN(x.getAttribute(XMLTag.NAME));
			if (v == null || !v.equals(type.getName())) Launcher.INSTANCE.log(new ErrorMessage(fp, v));
		}
		else // aucune descriptions longues
		{
			super.description = Documentation.INSTANCE.getClassBrief(type);
		}

		_groups = new Group[_GROUPS_TYPES.length];

		for (GroupType i : _GROUPS_TYPES)
		{
			fp = cp.resolve(i.toString() + Paths.XML);
			if (!Paths.CHECK_DIRECTORY(fp)) continue;
			if (_addGroup(i) != null) return;
		}
	}


	/**
	 * Récupére un groupe en l'instanciant si requis.
	 *
	 * @param groupType Type du groupe
	 * @return Un objet UIGroup ou null si le groupe n'existe pas
	 */
	@Nullable
	public Group getGroup(@Nullable GroupType groupType)
	{
		try
		{
			return _addGroup(groupType);
		}
		catch (Exception e)
		{
			Launcher.INSTANCE.raise(e);
		}

		return null;
	}


	/**
	 * Récupére le type de groupe d'un nom d'identifiant.
	 * Les fichiers sont chargés dans l'ordre jusqu'à ce que le nom soit trouvé.
	 *
	 * @return Le type du groupe ou null
	 */
	@Nullable
	public GroupType getGroupType(@Nullable String name)
	{
		if (name == null) return null;

		Type t = _type.get();
		if (t == null) return null;

		Group g;

		for (int i = 0; i < _GROUPS_TYPES.length; ++i)
		{
			g = _groups[i];
			if (g == null) g = _addGroup(_GROUPS_TYPES[i]);
			if (g != null && g.hasIdentifier(name)) return g.getType();
		}

		return null;
	}


	/**
	 * Récupére le type associé.
	 */
	@Nullable
	public Type getType() { return _type.get(); }


	/**
	 * Détermine si le fichier XML d'un groupe existe sans le charger. Le fichier ne doit pas être obligatoirement
	 * accessible au moment de l'appel. Si groupType vaut null la méthode vérifie si au moins un fichier XML de
	 * groupe existe.
	 *
	 * @param groupType Type du groupe
	 * @return true si le groupe existe, sinon false
	 */
	public boolean hasGroup(@Nullable GroupType groupType)
	{
		Path p;

		if (groupType == null)
		{
			p = Paths.GET_CLASS_PATH(_type.get());

			for (GroupType i : _GROUPS_TYPES)
				if (p != null && Paths.CHECK_FILE(p.resolve(i.toString() + Paths.XML), false)) return true;

			return false;
		}

		if (_groups[groupType.ordinal()] != null) return true;
		p = Paths.GET_CLASS_PATH(_type.get());
		return p != null && Paths.CHECK_FILE(p.resolve(groupType.toString() + Paths.XML), false);
	}


	/**
	 * Détermine si un identifiant représente une méthode.
	 */
	public boolean isMethod(@Nullable String name)
	{
		if (name == null) return false;

		Type t = _type.get();
		if (t == null) return false;

		Group g = _addGroup(GroupType.methods);
		return g != null && g.hasIdentifier(name);
	}


	@Override
	public boolean nameIsValid()
	{
		Type t = _type.get();
		return t != null && t.nameIsValid();
	}

	@Nullable
	private Group _addGroup(@Nullable GroupType r)
	{
		if (r == null) return null;
		Group g = _groups[r.ordinal()];

		if (g == null)
		{
			g = new Group(this, r);
			if (g.getSize() == 0) return null;
			_groups[r.ordinal()] = g;
		}

		return g;
	}
}