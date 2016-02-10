package settings;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import main.Strings;


public final class Property implements ISetting
{
	static private final String _FALSE = "0";
	static private final String _TRUE = "1";

	static private final String _NAME_RE = "\\p{Blank}*\\w+\\p{Blank}*";
	static private final String _BOOL_RE = "0|1";
	static private final String _INT_RE = "-+\\p{Blank}*\\d+";
	static private final String _UINT_RE = "\\d+";
	static private final String _MINUS_RE = "^-*";

	static private final byte _STRING = 0;
	static private final byte _BOOL = 1;
	static private final byte _UINT = 2;
	static private final byte _INT = 3;

	private String _name;
	private String _id;
	private String _default;
	private String _value;
	private byte _type;
	private boolean _persistent;


	public Property(boolean persistent, @NotNull String name, @Nullable String defaultValue)
	{
		if (!name.matches(_NAME_RE)) throw new IllegalArgumentException(INVALID_NAME);

		_type = _STRING;
		_persistent = persistent;
		_name = name.trim();
		_id = _name.toLowerCase();
		setDefault(defaultValue);
		_value = _default;
	}


	Property(@NotNull String name) // auto-création
	{
		if (!name.matches(_NAME_RE)) throw new IllegalArgumentException(INVALID_NAME);

		_type = _STRING;
		_name = name.trim();
		_id = _name.toLowerCase();
		_default = Strings.EMPTY;
		_value = _default;
	}


	Property(@NotNull String name, @Nullable String value) // parsing
	{
		if (!name.matches(_NAME_RE)) throw new IllegalArgumentException(INVALID_NAME);

		_type = _STRING;
		_name = name.trim();
		_id = _name.toLowerCase();
		_default = Strings.EMPTY;
		setValue(value);
	}


	public void copy(@NotNull Property property)
	{
		if (!property._id.equals(_id)) throw new IllegalArgumentException(INVALID_NAME);

		_name = property._name;
		_id = property._id;
		_persistent = property._persistent;
		_type = property._type;
		_default = property._default;
		_value = property._value;
	}


	@NotNull
	public String getDefault() { return _default; }


	/**
	 * Récupére le nom en minuscules.
	 */
	@NotNull
	public String getID() { return _id; }


	@NotNull
	public String getName() { return _name; }


	@NotNull
	public String getValue() { return _value; }


	/**
	 * Détermine si la propriété doit être sauvegardée même si elle est égale à sa valeur par défaut.
	 */
	public boolean isPersistent() { return _persistent; }


	/**
	 * Renvoie true uniquement si la valeur vaut "1" et que la valeur par défaut est un booléen.
	 */
	public boolean isTrue() { return _type == _BOOL && _value.equals(_TRUE); }


	public void setDefault(@Nullable String defaultValue)
	{
		_type = _STRING;
		_default = defaultValue != null ? defaultValue : Strings.EMPTY;
	}


	/**
	 * Définit un booléen en tant que valeur par défaut et valide la valeur en cours si requis.
	 */
	public void setDefault(boolean defaultValue)
	{
		_type = _BOOL;
		_default = defaultValue ? _TRUE : _FALSE;
		_validateBool(_value);
	}


	/**
	 * Définit un entier en tant que valeur par défaut et valide la valeur en cours si requis.
	 */
	public void setDefault(int value, boolean unsigned)
	{
		if (unsigned)
		{
			_type = _UINT;
			_default = String.valueOf(Math.abs(value));
			_validateUint(_value);
		}
		else
		{
			_type = _INT;
			_default = String.valueOf(value);
			_validateInt(_value);
		}
	}


	public void setPersistent(boolean value) { _persistent = value; }


	/**
	 * Définit la valeur et la valide si requis.
	 */
	public void setValue(@Nullable String value)
	{
		_value = value != null ? value : Strings.EMPTY;
		_validate();
	}


	/**
	 * Définit la valeur et la valide si requis.
	 */
	public void setValue(int value)
	{
		_value = String.valueOf(value);
		_validate();
	}


	/**
	 * Définit la valeur et la valide si requis.
	 */
	public void setValue(boolean value)
	{
		_value = value ? _TRUE : _FALSE;
		_validate();
	}


	@Override
	@NotNull
	public String toString()
	{
		return _name + Strings.SPACE + Strings.EQUAL + Strings.SPACE + _value;
	}


	@Override
	@NotNull
	public String toIniString()
	{
		return !_persistent && _value.equals(_default) ? Strings.EMPTY : toString();
	}


	private void _validate()
	{
		if (_type == _BOOL) _validateBool(_value);
		else if (_type == _UINT) _validateUint(_value);
		else if (_type == _INT) _validateInt(_value);
	}


	private void _validateBool(String v)
	{
		v = v.trim();
		_value = v.matches(_BOOL_RE) ? v : _default;
	}


	private void _validateInt(String v)
	{
		v = v.trim().replaceFirst(_MINUS_RE, Strings.DASH);
		_value = v.matches(_INT_RE) ? v : _default;
	}


	private void _validateUint(String v)
	{
		v = v.trim();
		_value = v.matches(_UINT_RE) ? v : _default;
	}
}