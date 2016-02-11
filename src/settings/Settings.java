package settings;

import java.util.LinkedHashMap;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import main.Strings;


public class Settings implements ISetting
{
	private LinkedHashMap<String, Section> _sections;


	public Settings()
	{
		_sections = new LinkedHashMap<String, Section>(3);
		_sections.put(null, new Section(null)); // doit être en premier
	}


	public void addSection(@Nullable Section... section)
	{
		if (section == null) return;

		String n;
		Section s;

		for (Section i : section)
		{
			if (i == null) continue;
			n = i.getName();

			if (n != null)
			{
				n = n.toLowerCase();
				s = _sections.get(n);
				if (s == null) _sections.put(n, i);
				else _sections.replace(n, i);
			}
			else
			{
				_sections.replace(null, i);
			}
		}
	}


	@Nullable
	public final Section getSection(@Nullable String name)
	{
		return _sections.get(name);
	}


	/**
	 * Supprime une section. La section globale est simplement vidée.
	 */
	public final void removeSection(@Nullable Section section)
	{
		if (section != null) _sections.remove(section.getName());
	}


	@Override
	@NotNull
	public final String toString()
	{
		String s = Strings.EMPTY;
		for (Section i : _sections.values())
			s += s.isEmpty() ? i.toString() : Strings.NEW_LINE + Strings.NEW_LINE + i.toString();
		return s;
	}


	@Override
	@NotNull
	public final String toIniString()
	{
		String s = Strings.EMPTY;
		String v;

		for (Section i : _sections.values())
		{
			v = i.toIniString();
			if (v.isEmpty()) continue;
			s += s.isEmpty() ? v : Strings.NEW_LINE + Strings.NEW_LINE + v;
		}

		return s;
	}


	protected final void validate()
	{
		for (Section i : _sections.values()) i.validate();
	}


}