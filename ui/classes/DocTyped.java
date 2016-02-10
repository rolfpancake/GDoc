package ui.classes;


import com.sun.istack.internal.NotNull;
import data.Type;
import data.Typed;
import ui.UIType;


/**
 * Conteneur des membres, constantes et thèmes. Il ajoute un type.
 */
final class DocTyped<D extends Typed> extends DocPrototyped<D>
{
	private UIType _type;


	DocTyped(D typed)
	{
		super(typed); // typed != null
		Type t = typed.getType();
		_type = t != null ? new UIType(t) : new UIType(typed.getAlias());
	}


	@NotNull
	UIType getType() { return _type; }
}