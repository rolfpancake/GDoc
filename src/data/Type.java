package data;

import java.util.ArrayList;
import org.jetbrains.annotations.Nullable;
import exceptions.ErrorMessage;
import exceptions.ErrorMessageType;
import main.Launcher;
import main.Strings;


/**
 * Un type unique, immuable et permanent.
 */
public final class Type extends Named
{
	static private final String _ALL_PATTERN = "^[@a-zA-Z][ \\w]+$";
	static private final String _ANY_PATTERN = ".*[a-zA-Z].*";

	static public final Type ARRAY = new Type("Array");
	static public final Type BOOL = new Type("bool");
	static public final Type FLOAT = new Type("float");
	static public final Type INT = new Type("int");
	static public final Type NIL = new Type("Nil");
	static public final Type OBJECT = new Type("Object");
	static public final Type GD_SCRIPT = new Type("@GDScript");
	static public final Type GLOBAL_SCOPE = new Type("@Global Scope");
	static public final Type STRING = new Type("String");
	static public final Type UNDEFINED = new Type("undefined"); // non référencé par Documentation

	static public final String NULL_VALUE = "null"; // != NIL::getName()
	static public final String UNTYPED_NAME = "var";
	static public final String UNTYPED_TOKEN = Strings.ASTERISK;
	static public final String REST_NAME = "...";

	static public final String RESOURCE_TYPE_NAME = "Resource";
	static public final String CONTROL_TYPE_NAME = "Control";
	static public final String NODE_2D_TYPE_NAME = "Node2D";
	static public final String SPATIAL_TYPE_NAME = "Spatial";
	static public final String SCENE_TREE_TYPE_NAME = "SceneTree";
	static public final String VECTOR2_TYPE_NAME = "Vector2";

	static private final String _ARRAY_RE = "Dictionary|.*Array$";
	static private final String _VECTOR_RE = "Vector2|Vector3|Rect2|Matrix32|Matrix3|Plane|Quat|AABB|Transform";

	private Category _category;
	private Type _super;
	private ArrayList<Type> _subTypes;
	private boolean _valid;


	Type(@Nullable String name)
	{
		if (name == null) return;
		name = name.trim();

		if (name.matches(_ANY_PATTERN)) // au moins un caractère valide
		{
			super.setName(name);
			if (name.matches(_ALL_PATTERN)) _valid = true;
		}

		if (!_valid) Launcher.INSTANCE.log(new ErrorMessage(ErrorMessageType.invalidName, super.getClass(), name));
	}


	/**
	 * Détermine si l'occurrence est un sous-type d'un autre type.
	 *
	 * @return true si l'occurrence est un sous-type ou le type lui-même, sinon false
	 */
	public boolean isSubTypeOf(@Nullable Type type)
	{
		return type == this || (_super != null && _super.isSubTypeOf(type));
	}


	/**
	 * Récupére la catégorie.
	 * Ne doit pas renvoyer la catégorie par défaut car Documentation utilise la valeur réelle pour éviter les
	 * doublons, donc peut renvoyer null pendant l'initialisation.
	 *
	 * @return Un objet Category non nul
	 */
	@Nullable
	public Category getCategory() { return _category; }


	/**
	 * Récupére les sous-types.
	 *
	 * @return Un tableau de types de longueur > 0 ou null
	 */
	@Nullable
	public Type[] getSubTypes()
	{
		return _subTypes == null ? null : _subTypes.toArray(new Type[_subTypes.size()]);
	}


	/**
	 * Récupére le super type.
	 *
	 * @return Un objet Type ou null
	 */
	@Nullable
	public Type getSuper() { return _super; }


	/**
	 * Détermine si au moins un sous-type existe.
	 *
	 * @return true si un sous-type existe, sinon false
	 */
	public boolean hasSubType() { return _subTypes != null; }


	/**
	 * Détermine si l'occurrence est un type tableau.
	 */
	public boolean isArray()
	{
		String n = super.getName();
		return (_category == Category.BUILT_IN) && _valid && n != null && n.matches(_ARRAY_RE);
	}


	/**
	 * Détermine si l'occurrence est un type de base
	 */
	public boolean isBasic()
	{
		return this == BOOL || this == INT || this == FLOAT || this == NIL || this == STRING;
	}


	/**
	 * Détermine si l'occurrence est un type vecteur (Vector2, Vector3, Rect2, Matrix32, Matrix3, Plane, Quat,
	 * AABB, Transform).
	 */
	public boolean isVector()
	{
		String n = super.getName();
		return _category == Category.BUILT_IN && n != null && n.matches(_VECTOR_RE);
	}


	@Override
	public boolean nameIsValid() { return _valid; }


	void addSubType(@Nullable Type subType)
	{
		if (subType == null) return;
		if (_subTypes == null) _subTypes = new ArrayList<Type>(1);
		if (!_subTypes.contains(subType)) _subTypes.add(subType);
	}


	void setCategory(Category value) { if (_category == null) _category = value; }


	void setSuper(Type value) { if (_super == null) _super = value; }
}