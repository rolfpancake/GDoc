package data;

import java.util.ArrayList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xml.XML;
import xml.XMLTag;


/**
 * Classe de base abstraite des identifiants paramétrés.
 */
abstract public class Parameterized extends Identifier
{
	private ArrayList<Argument> _arguments;


	protected Parameterized(Group group, XML data)
	{
		super(group, data); // data != null, name != null

		ArrayList<XML> l1 = data.getChildren(XMLTag.ARGUMENT);
		byte n = (byte) l1.size();
		if (n == 0) return;

		ArrayList<Argument> l2 = new ArrayList<Argument>(n);
		ArrayList<String> l3 = new ArrayList<String>(n);
		ArrayList<Byte> l4 = new ArrayList<Byte>(n);
		Argument a;
		String s;
		n = 0;

		for (XML i : l1)
		{
			a = new Argument(this, group, i);
			s = a.getName();
			if (s == null || l3.contains(s)) continue;
			l3.add(s);
			l2.add(a);
			s = i.getAttribute(XMLTag.INDEX);
			l4.add(s != null && s.trim().matches("^\\d+$") ? Byte.valueOf(s) : 0);
			n += 1;
			if (n == Byte.MAX_VALUE) break;
		}

		if (n == 0) return;
		_arguments = new ArrayList<Argument>(n);
		byte k; // valeur dans l4
		byte i1 = 0; // indice dans l2
		byte i2; // indice dans _arguments

		for (Argument i : l2)
		{
			k = l4.get(i1);
			i2 = 0;

			for (Argument j : _arguments)
			{
				if (k < l4.get(l2.indexOf(j))) break;
				i2 += 1;
			}

			_arguments.add(i2, i);
			i1 += 1;
		}
	}


	/**
	 * Récupére une liste ordonnée des arguments.
	 *
	 * @return Un tableau non null
	 */
	@NotNull
	public final Argument[] getArguments()
	{
		return _arguments == null ? new Argument[0] : _arguments.toArray(new Argument[_arguments.size()]);
	}


	/**
	 * Récupére le nombre d'arguments.
	 *
	 * @return Un entier >= 0
	 */
	public final byte getSize() { return _arguments == null ? 0 : (byte) _arguments.size(); }


	/**
	 * Récupére l'indice d'un argument.
	 *
	 * @param argument Argument
	 * @return Un entier >= 0, ou -1 s'il n'est pas référencé
	 */
	public final byte indexOf(@Nullable Argument argument)
	{
		return _arguments == null ? -1 : (byte) _arguments.indexOf(argument);
	}
}