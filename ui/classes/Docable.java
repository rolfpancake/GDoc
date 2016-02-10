package ui.classes;

import org.jetbrains.annotations.NotNull;
import data.Described;
import data.Documentation;
import ui.Verticable;
import ui.descriptions.UIDescription;


/**
 * Classe de base abstraite des conteneurs de documentation.
 */
abstract class Docable<D extends Described> extends Verticable
{
	protected UIDescription description;

	private D _data;


	Docable(@NotNull D described)
	{
		super();
		_data = described;

		if (described.description != null)
		{
			description = new UIDescription(Documentation.INSTANCE.getDataType(described),
											described.description, false);
			if (description.isEmpty()) description = null;
			else
			{
				super.getChildren().add(description);
				super.vSize = description.getVSize();
			}
		}
	}


	@NotNull
	final D getData() { return _data; }


	@Override
	public void setHSize(short size)
	{
		if (description != null)
		{
			description.setHSize(size);
			super.vSize = description.getVSize();
		}
	}
}