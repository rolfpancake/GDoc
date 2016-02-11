package data;

import org.jetbrains.annotations.NotNull;
import xml.XML;
import xml.XMLTag;


public final class Method extends Parameterized
{
	private Return _return;


	Method(Group group, XML data)
	{
		super(group, data);

		XML d = data.getFirstChild(XMLTag.RETURN);
		Type t = d != null ? Documentation.INSTANCE.getType(d.getAttribute(XMLTag.TYPE)) : null;
		if (t == null) t = Type.NIL;
		_return = new Return(this, t);
	}


	/**
	 * Récupére le retour.
	 *
	 * @return Un objet Return non nul
	 */
	@NotNull
	public Return getReturn() { return _return; }
}