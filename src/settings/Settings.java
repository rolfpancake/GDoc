package settings;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import main.Launcher;
import main.Paths;
import main.Strings;


public final class Settings implements ISetting
{
	static private final String _NAME_GROUP = "N";
	static private final String _VALUE_GROUP = "V";
	static private final String _COMMENT_RE = "^(;|#).*(\n|$)";
	static private final String _WHITESPACE_RE = "\\p{Space}*";

	static private final Pattern _SECTION = Pattern.compile("^(?:\\p{Blank}*\\[)(?<N>.*?)(?:\\]\\p{Blank}*(\n|$))");
	static private final Pattern _PROPERTY = Pattern.compile("^(?<N>[^ ]+)(?: *= ?)(?<V>[^\n]*)(?:\n|$)");

	static public final Settings INSTANCE = new Settings();

	private ArrayList<Section> _sections;
	private Section _globals;


	private Settings()
	{
		_sections = new ArrayList<Section>();

		Path p = Paths.USER_DIR.resolve("GDoc.ini");
		if (!Files.exists(p) || Files.isDirectory(p)) return;

		List<String> l = null;

		try
		{
			l = Files.readAllLines(p);
		}
		catch (IOException e)
		{
			Launcher.INSTANCE.log(e);
		}

		if (l == null) return;

		Section s = null;
		Matcher m;

		for (String i : l)
		{
			if (i == null || i.isEmpty() || i.matches(_WHITESPACE_RE) || i.matches(_COMMENT_RE)) continue;

			m = _SECTION.matcher(i);

			if (m.find())
			{
				try
				{
					s = getSection(m.group(_NAME_GROUP));
				}
				catch (Exception e)
				{
					Launcher.INSTANCE.log(e);
				}

				continue;
			}

			m = _PROPERTY.matcher(i);

			if (m.find())
			{
				if (s == null) // propriété globale
				{
					if (_globals == null) _globals = new Section(null);
					s = _globals;
				}

				try
				{
					s.addProperty(new Property(m.group(_NAME_GROUP), m.group(_VALUE_GROUP)));
				}
				catch (Exception e)
				{
					Launcher.INSTANCE.log(e);
				}
			}
		}
	}


	/**
	 * Récupére une section en la créant si elle n'existe pas.
	 */
	@NotNull
	public Section getSection(@Nullable String name)
	{
		if (name == null)
		{
			if (_globals == null) _globals = new Section(null);
			return _globals;
		}

		String n = name.toLowerCase();
		String s;

		for (Section i : _sections)
		{
			s = i.getName();
			if (s != null && n.equals(s.toLowerCase())) return i;
		}

		Section o = new Section(name);
		_sections.add(o);
		return o;
	}


	public void removeSection(@Nullable Section section)
	{
		if (section == _globals) _globals = null;
		else _sections.remove(section);
	}


	/**
	 * Réécrit tous les paramètres.
	 */
	public void save()
	{
		Path p = Paths.USER_DIR.resolve("GDoc.ini");
		if (Files.isDirectory(p)) return;
		FileWriter w = null;

		try
		{
			String v = toIniString(); // peut être vide pour valider un retour à une valeur par défaut
			w = new FileWriter(p.toFile());
			w.write(v);
		}
		catch (IOException e)
		{
			Launcher.INSTANCE.log(e);
		}

		if (w != null)
		{
			try
			{
				w.close();
			}
			catch (IOException e)
			{
				Launcher.INSTANCE.log(e);
			}
		}
	}


	@Override
	@NotNull
	public String toString()
	{
		String s = _globals != null ? _globals.toString() : Strings.EMPTY;
		for (Section i : _sections) s += Strings.NEW_LINE + Strings.NEW_LINE + i.toString();
		return s;
	}


	@Override
	@NotNull
	public String toIniString()
	{
		String s = Strings.EMPTY;
		String v;

		if (_globals != null)
		{
			v = _globals.toIniString();
			if (!v.isEmpty()) s = v;
		}

		for (Section i : _sections)
		{
			v = i.toIniString();
			if (v.isEmpty()) continue;
			if (!s.isEmpty()) s += Strings.NEW_LINE + Strings.NEW_LINE;
			s += v;
		}

		return s;
	}
}