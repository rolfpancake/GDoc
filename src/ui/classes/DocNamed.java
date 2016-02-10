package ui.classes;


import com.sun.istack.internal.NotNull;
import javafx.scene.text.Text;
import data.Argument;
import data.Described;
import data.Return;
import fonts.FontManager;
import ui.Graphics;


/**
 * Conteneur des arguments et retours. Il ajoute un label externe.
 */
final class DocNamed<D extends Described> extends Docable<D>
{
	static private final String _RETURNS = "Returns :";

	private Text _name;


	DocNamed(D described)
	{
		super(described); // described != null

		if (described instanceof Argument)
			_name = Graphics.CREATE_TEXT_FIELD(described.getName(), FontManager.INSTANCE.getFont(true));

		else if (described instanceof Return)
			_name = Graphics.CREATE_TEXT_FIELD(_RETURNS);

		else throw new IllegalArgumentException();
	}


	@NotNull
	Text getName() { return _name; }
}