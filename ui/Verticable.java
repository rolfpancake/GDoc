package ui;

import javafx.scene.Parent;


/**
 * Un conteneur à hauteur variable, dimensionné en largeur par son parent.
 */
abstract public class Verticable extends Parent
{
	protected short vSize = 0;


	public final short getVSize() { return vSize; }


	abstract public void setHSize(short size);
}