package ui.classes;

import com.sun.istack.internal.NotNull;
import data.Identifier;


/**
 * Classe de base abstraite des conteneurs de méthodes, signaux, membres, constantes et thèmes.
 * Elle ajoute le prototype, il est donc possible de cadrer le prototype et la description.
 */
abstract class DocPrototyped<D extends Identifier> extends Docable<D>
{
	static public final byte PROTOTYPE_ROW_HEIGHT = 25;

	static private final byte _V_GAP = 2;

	private UIPrototype<D, Docable<D>> _prototype;


	protected DocPrototyped(D identifier)
	{
		super(identifier); // identifier != null
		_prototype = new UIPrototype<D, Docable<D>>(this);
		super.getChildren().add(_prototype);
		super.vSize += PROTOTYPE_ROW_HEIGHT + (super.description != null ? _V_GAP : 0);
	}


	@NotNull
	final UIPrototype<D, Docable<D>> getPrototype() { return _prototype; }


	@Override
	public void setHSize(short size)
	{
		super.setHSize(size);

		_prototype.setLayoutY((PROTOTYPE_ROW_HEIGHT - _prototype.getBoundsInLocal().getHeight()) / 2);

		if (super.description != null)
		{
			super.description.setLayoutY(PROTOTYPE_ROW_HEIGHT + _V_GAP);
			super.vSize += PROTOTYPE_ROW_HEIGHT + _V_GAP;
		}
		else
		{
			super.vSize = PROTOTYPE_ROW_HEIGHT;
		}
	}
}