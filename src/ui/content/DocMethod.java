package ui.content;

import org.jetbrains.annotations.NotNull;
import javafx.scene.text.Text;
import data.Method;
import data.Return;
import data.Type;
import main.Launcher;
import ui.UIType;


/**
 * Conteneur des méthodes. Il ajoute un retour externe et sa description.
 */
final class DocMethod<D extends Method> extends DocParameterized<D>
{
	private UIType _return;


	DocMethod(@NotNull D method)
	{
		super(method);
	}


	@Override
	short getMinLeftOffset() { return (short) _return.getBoundsInLocal().getWidth(); }


	@Override
	protected void initDisplay()
	{
		super.initDisplay();
		Method d = super.getData();
		Type t = d.getReturn().getType();
		_return = new UIType(t != null ? t : Type.NIL);
		super.getChildren().add(_return);

		if (t != Type.NIL)
		{
			super.returns = new DocNamed<Return>(d.getReturn());

			if (super.returns.getDescription().isEmpty() &&
				Launcher.SETTINGS.ui().hideUndocumentedReturn().toBoolean())
				super.returns = null;
			else
				super.getChildren().add(super.returns);
		}
	}


	@Override
	protected void initLayout()
	{
		super.initLayout();
		Text n = super.getName();
		_return.setLayoutY(n.getLayoutY() + n.getBaselineOffset() - _return.getBaselineOffset());
	}
}