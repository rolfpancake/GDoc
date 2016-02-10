package settings;

import java.util.LinkedHashMap;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import main.Strings;


/**
 * Une section de propriétés.
 */
public class Section implements ISetting
{
	static private final String _NAME_RE = "[ \\w]*";
	static private final String _INVALID_NAME = "invalid name";

	private LinkedHashMap<String, Property> _properties;
	private String _name;


	public Section(@Nullable String name)
	{
		if (name != null && !name.matches(_NAME_RE)) throw new IllegalArgumentException(_INVALID_NAME);
		_name = name;
		_properties = new LinkedHashMap<String, Property>();
	}


	public final void addProperty(@Nullable Property... property)
	{
		if (property == null) return;

		String s;
		Property p;

		for (Property i : property)
		{
			if (i == null) continue;

			s = i.getName().toLowerCase();
			p = _properties.get(s);

			if (p == null) _properties.put(s, i);
			else _properties.replace(s, i);
		}
	}


	public final void clear() { _properties.clear(); }


	@Nullable
	public String getName() { return _name; }


	@Nullable
	public final Property getProperty(@NotNull String name)
	{
		return _properties.get(name.toLowerCase());
	}


	public final byte getSize() { return (byte) _properties.size(); }


	public final void removeProperty(@Nullable Property property)
	{
		if (property != null) _properties.remove(property.getName().toLowerCase());
	}


	@Override
	@NotNull
	public final String toString()
	{
		String s = _name == null ? Strings.EMPTY : Strings.LEFT_BRACKET + _name + Strings.RIGHT_BRACKET;
		if (_properties.size() == 0) return s;
		s += Strings.NEW_LINE;
		for (Property i : _properties.values()) s += Strings.NEW_LINE + i.toString();
		return s;
	}


	@Override
	@NotNull
	public final String toIniString()
	{
		if (_properties.size() == 0) return Strings.EMPTY;

		String s = Strings.EMPTY;
		String v;

		for (Property i : _properties.values())
		{
			v = i.toIniString();
			if (v.isEmpty()) continue;
			if (!s.isEmpty()) s += Strings.NEW_LINE;
			s += v;
		}

		if (s.isEmpty() || _name == null) return s;
		return Strings.LEFT_BRACKET + _name + Strings.RIGHT_BRACKET + Strings.NEW_LINE + Strings.NEW_LINE + s;
	}


	final void setProperty(String name, String value) // parsing
	{
		name = Strings.CLEAN(name);
		if (name == null) return;
		name = name.toLowerCase();
		Property p = _properties.get(name);
		if (p != null) p.setValue(value);
	}


	protected void validate() { /* void */ }
}