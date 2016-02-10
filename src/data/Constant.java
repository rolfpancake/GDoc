package data;

import xml.XML;
import xml.XMLTag;


public final class Constant extends Defined
{
	Constant(Group group, XML data)
	{
		super(group, data); // data != null
		super.setValue(data.getAttribute(XMLTag.VALUE));
	}
}