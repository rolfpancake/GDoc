package data;

import java.lang.ref.WeakReference;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import exceptions.ErrorMessage;
import exceptions.ErrorMessageType;
import main.Launcher;
import main.Strings;
import xml.XML;
import xml.XMLTag;


/**
 * Classe de base abstraite des identifiants.
 */
abstract public class Identifier extends Described
{
	static private final String _PATTERN = "^[_a-zA-Z]+[_\\w]*$";
	static private final String _FIXABLE_END = ":" + Type.UNTYPED_NAME;

	private WeakReference<Group> _group;
	private boolean _valid;


	protected Identifier(@NotNull Group group, @NotNull XML api)
	{
		super();

		_group = new WeakReference<Group>(group);
		api.parse();
		String v = Strings.CLEAN(api.getAttribute(XMLTag.NAME));

		if (v != null)
		{
			if (v.matches(_PATTERN))
			{
				_valid = true;
			}
			else if (v.endsWith(_FIXABLE_END))
			{
				String v2 = v.replaceFirst(_FIXABLE_END + "$", Strings.EMPTY);

				if (v2.matches(_PATTERN))
				{
					v = v2;
					_valid = true;
				}
			}
			else if (this instanceof Argument && v.equals(Type.REST_NAME))
			{
				_valid = true;
			}

			super.setName(v);
		}

		if (!_valid) Launcher.INSTANCE.log(new ErrorMessage(ErrorMessageType.invalidName, super.getClass(), v));
	}


	/**
	 * Récupére le groupe.
	 *
	 * @return Un objet Group ou null
	 */
	@Nullable
	public final Group getGroup() { return _group.get(); }


	@Override
	public final boolean nameIsValid() { return _valid; }
}