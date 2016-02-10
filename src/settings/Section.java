package settings;

import java.util.ArrayList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import main.Strings;


/**
 * Une section de propriétés.
 */
public final class Section implements ISetting
{
	static private final String _NAME_RE = "[ \\w]*";

	private ArrayList<Property> _properties;
	private String _name;


	public Section(@Nullable String name)
	{
		if (name != null && !name.matches(_NAME_RE)) throw new IllegalArgumentException(INVALID_NAME);
		_name = name;
		_properties = new ArrayList<Property>();
	}


	public void addProperty(@Nullable Property property)
	{
		if (property == null) return;

		String s = property.getID();

		for (Property i : _properties)
		{
			if (s.equals(i.getID()))
			{
				i.copy(property);
				return;
			}
		}

		_properties.add(property);
	}


	@Nullable
	public String getName() { return _name; }


	/**
	 * Récupére une propriété en la créant si elle n'existe pas.
	 */
	@NotNull
	public Property getProperty(@NotNull String name)
	{
		String s = name.toLowerCase();
		for (Property i : _properties) if (s.equals(i.getName().toLowerCase())) return i;
		Property p = new Property(name);
		_properties.add(p);
		return p;
	}


	public byte getSize() { return (byte) _properties.size(); }


	public void removeProperty(@Nullable Property property) { _properties.remove(property); }


	@Override
	@NotNull
	public String toString()
	{
		String s = Strings.LEFT_BRACKET + _name + Strings.RIGHT_BRACKET;
		if (_properties.size() == 0) return s;
		s += Strings.NEW_LINE;
		for (Property i : _properties) s += Strings.NEW_LINE + i.toString();
		return s;
	}


	@Override
	@NotNull
	public String toIniString()
	{
		if (_properties.size() == 0) return Strings.EMPTY;

		String s = Strings.EMPTY;
		String v;

		for (Property i : _properties)
		{
			v = i.toIniString();
			if (v.isEmpty()) continue;
			if (!s.isEmpty()) s += Strings.NEW_LINE;
			s += v;
		}

		if (s.isEmpty()) return Strings.EMPTY;
		return Strings.LEFT_BRACKET + _name + Strings.RIGHT_BRACKET + Strings.NEW_LINE + Strings.NEW_LINE + s;
	}


	void setName(@Nullable String name)
	{
		if (name == null)
		{
			if (_name != null) throw new IllegalArgumentException(INVALID_NAME);
		}
		else if (_name == null)
		{
			throw new IllegalArgumentException(INVALID_NAME);
		}
		else if (!name.toLowerCase().equals(_name.toLowerCase()))
		{
			throw new IllegalArgumentException(INVALID_NAME);
		}

		_name = name;
	}
}