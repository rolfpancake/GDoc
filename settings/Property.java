package settings;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import events.ChangeEvent;
import events.EventDispatcher;
import main.Strings;


/**
 * Classe de base des propriétés, une chaîne de caractères.
 */
public class Property extends EventDispatcher<ChangeEvent> implements ISetting
{
	static private final String _NAME_RE = "\\p{Blank}*\\w+\\p{Blank}*";
	static private final String _INVALID_NAME = "invalid name";

	private String _name;
	private String _default = Strings.EMPTY;
	private String _value = Strings.EMPTY;
	private boolean _persistent = false;


	public Property(boolean persistent, @NotNull String name, @Nullable String defaultValue)
	{
		super();
		if (!name.matches(_NAME_RE)) throw new IllegalArgumentException(_INVALID_NAME);
		_name = name.trim();
		_persistent = persistent;
		setDefault(defaultValue);
		_value = _default;
	}


	Property(@NotNull String name) // auto-création
	{
		super();
		if (!name.matches(_NAME_RE)) throw new IllegalArgumentException(_INVALID_NAME);
		_name = name.trim();
		_value = _default;
	}


	@NotNull
	public final String getDefault() { return _default; }


	@NotNull
	public String getName() { return _name; }


	@NotNull
	public final String getValue() { return _value; }


	/**
	 * Détermine si la propriété doit être sauvegardée même si elle est égale à sa valeur par défaut.
	 */
	public final boolean isPersistent() { return _persistent; }


	public void setDefault(@Nullable String defaultValue)
	{
		_default = defaultValue != null ? defaultValue : Strings.EMPTY;
		validate();
	}


	public final void setPersistent(boolean value) { _persistent = value; }


	public void setValue(@Nullable String value)
	{
		if ((value == null && _value == null) || (value != null && value.equals(_value))) return;
		_value = value != null ? value : _default;
		super.dispatchEvent(new ChangeEvent());
	}


	@Override
	@NotNull
	public final String toString()
	{
		return _name + Strings.SPACE + Strings.EQUAL + Strings.SPACE + _value;
	}


	@Override
	@NotNull
	public final String toIniString()
	{
		return !_persistent && _value.equals(_default) ? Strings.EMPTY : toString();
	}

	protected void validate() { /* void */ }
}