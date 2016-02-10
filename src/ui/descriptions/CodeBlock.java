package ui.descriptions;


import javafx.geometry.Bounds;
import javafx.scene.Parent;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import fonts.FontManager;
import main.Strings;
import ui.Graphics;


final class CodeBlock extends Parent
{
	static private final Font _MONOSPACED = FontManager.INSTANCE.getFont(FontManager.MONO_SPACE_FAMILY_NAME);

	Rectangle _background;


	CodeBlock(String code)
	{
		super();
		code = Strings.CLEAN(code);
		if (code == null) return;

		Text t = Graphics.CREATE_TEXT_FIELD(code);
		t.setFont(_MONOSPACED);
		super.getChildren().add(t);
	}


	@Override
	protected void layoutChildren()
	{
		if (_background != null) return;
		super.layoutChildren();
		Bounds b = super.getChildren().get(0).getBoundsInLocal();
		_background = new Rectangle(b.getWidth(), Math.ceil(b.getHeight()), Color.WHITE);
		super.getChildren().add(0, _background);
	}
}