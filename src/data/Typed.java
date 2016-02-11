package data;

import java.lang.ref.WeakReference;
import org.jetbrains.annotations.Nullable;
import exceptions.ErrorMessage;
import exceptions.ErrorMessageType;
import main.Launcher;
import xml.XML;
import xml.XMLTag;


/**
 * Classe de base abstraite des identifiants typés.
 */
abstract public class Typed extends Identifier
{
	private WeakReference<Type> _type;
	private String _alias;


	protected Typed(Group group, XML data)
	{
		super(group, data); // data != null
		String v = data.getAttribute(XMLTag.TYPE);
		Type t = null;

		if (v != null)
		{
			v = v.trim();

			if (!v.isEmpty())
			{
				t = Documentation.INSTANCE.getType(v);
				if (t == null) t = Documentation.INSTANCE.getWideType(v);
				if (t != null) _type = new WeakReference<Type>(t);
				else
				{
					_alias = v;

					if (!v.equals(Type.UNTYPED_NAME))
						Launcher.INSTANCE.log(new ErrorMessage(ErrorMessageType.unknownType, v));
				}
			}
		}

		if (t == null && _alias == null) _type = new WeakReference<Type>(Type.UNDEFINED);
	}


	/**
	 * Récupére l'alias qui est un type non référencé. Il est définit seulement si type est indéfini.
	 *
	 * @return Un objet String non vide ou null
	 */
	@Nullable
	public final String getAlias() { return _alias; }


	/**
	 * Récupére le type de l'identifiant.
	 *
	 * @return Un objet Type ou null
	 */
	@Nullable
	public final Type getType() { return _type != null ? _type.get() : null; }


	protected void setType(Type value) // inféré pour une constante
	{
		if (value != null && ((_type == null || _type.get() == Type.UNDEFINED) && _alias == null))
			_type = new WeakReference<Type>(value);
	}
}