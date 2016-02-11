package settings;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.jetbrains.annotations.NotNull;
import exceptions.SingletonException;
import main.Launcher;
import main.Paths;


public final class ApplicationSettings extends Settings
{
	static private boolean _INSTANCED = false;

	static private final String _FILE_NAME = "GDoc.ini";
	static private final String _NAME_GROUP = "N";
	static private final String _VALUE_GROUP = "V";
	static private final String _COMMENT_RE = "^(;|#).*?(\n|$)";
	static private final String _WHITESPACE_RE = "\\p{Space}*";

	static private final Pattern _SECTION = Pattern.compile("^(?:\\p{Blank}*\\[)(?<N>.*?)(?:\\]\\p{Blank}*(\n|$))");
	static private final Pattern _PROPERTY = Pattern.compile("^(?<N>[^ ]+)(?: *= ?)(?<V>[^\n]*)(?:\n|$)");

	private LogSection _log;
	private UISection _ui;
	private WindowSection _window;


	public ApplicationSettings()
	{
		if (_INSTANCED) throw new SingletonException();
		_INSTANCED = true;

		_window = new WindowSection();
		_ui = new UISection();
		_log = new LogSection();
		super.addSection(_window, _ui, _log);

		Path p = Paths.USER_DIR.resolve(_FILE_NAME);
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
		boolean k = false;
		Matcher m;

		for (String i : l)
		{
			if (i == null || i.isEmpty() || i.matches(_WHITESPACE_RE) || i.matches(_COMMENT_RE)) continue;

			m = _SECTION.matcher(i);

			if (m.find())
			{
				s = super.getSection(m.group(_NAME_GROUP));
				k = s == null;
				continue;
			}

			if (k) continue;

			if (s == null)
			{
				s = super.getSection(null);

				if (s == null)
				{
					s = new Section(null);
					super.addSection(s);
				}
			}

			m = _PROPERTY.matcher(i);

			if (m.find()) s.setProperty(m.group(_NAME_GROUP), m.group(_VALUE_GROUP));
		}

		super.validate();
	}


	@NotNull
	public LogSection log() { return _log; }


	@NotNull
	public UISection ui() { return _ui; }


	@NotNull
	public WindowSection window() { return _window; }


	/**
	 * Réécrit tous les paramètres.
	 */
	public void save()
	{
		Path p = Paths.USER_DIR.resolve(_FILE_NAME);
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
}