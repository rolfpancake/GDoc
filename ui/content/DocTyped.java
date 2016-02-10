package ui.content;


import org.jetbrains.annotations.NotNull;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import data.Constant;
import data.Type;
import data.Typed;
import fonts.FontManager;
import main.Strings;
import ui.Graphics;
import ui.UIType;


/**
 * Conteneur des membres, constantes et thèmes. Il ajoute un type.
 */
final class DocTyped<D extends Typed> extends DocPrototyped<D>
{
	static private final byte _OFFSET = 1;

	private UIType _type;


	DocTyped(@NotNull D typed)
	{
		super(typed);
	}


	@Override
	short getMinLeftOffset() { return (short) _type.getBoundsInLocal().getWidth(); }


	@Override
	protected void initDisplay()
	{
		super.initDisplay();
		Typed d = super.getData();
		Type t = d.getType();
		if (t != null) _type =  new UIType(t);
		else if (d.getAlias() != null) _type = new UIType(d.getAlias());
		if (_type != null) super.getChildren().add(_type);
		if (!(d instanceof Constant)) return;

		super.suffix = new Group();
		Font f = FontManager.INSTANCE.getFont(_OFFSET);
		super.suffix.getChildren().add(Graphics.CREATE_TEXT_FIELD(Strings.EQUAL, f));
		super.suffix.getChildren().add(Graphics.CREATE_TEXT_FIELD(((Constant) d).getValue(), f));
	}


	@Override
	protected void initLayout()
	{
		super.initLayout();

		if (_type != null)
		{
			Text t = super.getName();
			_type.setLayoutY(t.getLayoutY() + t.getBaselineOffset() - _type.getBaselineOffset());
		}

		if (super.suffix != null)
		{
			double x = Graphics.PADDING;

			for (Node i : super.suffix.getChildren())
			{
				i.setLayoutX(x);
				x += i.getBoundsInLocal().getWidth() + Graphics.PADDING;
			}
		}
	}
}