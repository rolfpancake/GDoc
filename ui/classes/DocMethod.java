package ui.classes;

import com.sun.istack.internal.NotNull;
import data.Method;
import data.Return;
import data.Type;
import ui.UIType;


/**
 * Conteneur des méthodes. Il ajoute un retour externe et sa description.
 */
final class DocMethod<D extends Method> extends DocParameterized<D>
{
	private UIType _return;
	private DocNamed<Return> _docable;


	DocMethod(D method)
	{
		super(method); // method != null
		Type t = method.getReturn().getType(); // ne devrait pas être nul
		_return = new UIType(t != null ? t : Type.NIL);
		if (method.getReturn().description == null) return;
		_docable = new DocNamed<Return>(method.getReturn());
		super.getChildren().add(_docable);
	}


	@NotNull
	UIType getReturn() { return _return; }


	@Override
	public void setHSize(short size)
	{
		super.setHSize(size);
		if (_docable == null) return;
		_docable.getName().setLayoutY(super.vSize + DocParameterized.V_GAP);
		double x1 = super.getHAlign();
		double x2 = _docable.getName().getBoundsInLocal().getWidth() + DocParameterized.H_GAP;
		if (x2 > x1) super.setHAlign(x2);
		_docable.setLayoutX(Math.max(x1, x2));
		_docable.setLayoutY(super.vSize + DocParameterized.V_GAP);
		super.vSize += DocParameterized.V_GAP + _docable.getVSize();
	}
}