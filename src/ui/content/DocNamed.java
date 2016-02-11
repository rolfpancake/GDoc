package ui.content;


import org.jetbrains.annotations.NotNull;
import javafx.scene.text.Text;
import data.Argument;
import data.Described;
import data.Return;
import fonts.FontManager;
import ui.Graphics;


/**
 * Conteneur des suffix et retours. Il ajoute un label externe.
 */
final class DocNamed<D extends Described> extends Docable<D>
{
	static private final String _RETURNS = "Returns";

	private Text _name;


	DocNamed(@NotNull D described)
	{
		super(described);
	}


	@Override
	short getMinLeftOffset()
	{
		return (short) _name.getBoundsInLocal().getWidth();
	}


	@Override
	protected void initDisplay()
	{
		super.initDisplay();
		Described d = super.getData();

		if (d instanceof Argument)
			_name = Graphics.CREATE_TEXT_FIELD(d.getName(), FontManager.INSTANCE.getFont(true));

		else if (d instanceof Return)
			_name = Graphics.CREATE_TEXT_FIELD(_RETURNS);

		else throw new IllegalArgumentException();

		super.getChildren().add(_name);
	}


	@Override
	protected void initLayout()
	{
		super.initLayout();
		_name.setLayoutY(super.getDescription().getBaselineOffset() - _name.getBaselineOffset());
	}
}