package data;

import java.lang.ref.WeakReference;
import org.jetbrains.annotations.NotNull;
import xml.XML;
import xml.XMLTag;


/**
 * Un argument de méthode ou de signal.
 */
public final class Argument extends Defined
{
	private WeakReference<Parameterized> _parameterized;


	Argument(@NotNull Parameterized parameterized, Group group, XML data)
	{
		super(group, data); // data != null
		_parameterized = new WeakReference<Parameterized>(parameterized);
		super.setValue(data.getAttribute(XMLTag.DEFAULT));
	}


	/**
	 * Récupére le paramétré associé.
	 *
	 * @return Un objet Parameterized non nul
	 */
	public Parameterized getParameterized() { return _parameterized != null ? _parameterized.get() : null; }
}