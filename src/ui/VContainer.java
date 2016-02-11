package ui;

import javafx.scene.Parent;


/**
 * Un conteneur à hauteur variable, dimensionné en largeur par son parent.
 */
abstract public class VContainer extends Parent
{
	private short _width = 0;
	private short _height = 0;


	protected VContainer() { super(); }


	public final short getHeight() { return _height; }


	public final short getWidth() { return _width; }


	/**
	 * Définit la largeur et appelle computeHeight(). Les sous-classes ne devraient appeler la super méthode qu'à
	 * la fin.
	 */
	public void setWidth(short width)
	{
		width = (short) Math.max(0, width);

		if (width != _width)
		{
			_width = width;
			requestHeightComputing();
		}
	}


	abstract protected short computeHeight();


	/* doit être appelée après l'exécution de tous les constructeurs enfants, donc une sous-classe devrait appeler
	   cette méthode à la fin de son constructeur et implémenter la méthode isInitialized() qui devrait renvoyer
	   false pour chaque appel d'une super classe. Platform::runLater() exécute la méthode trop tard */
	protected final void init()
	{
		if (!isInitialized()) return;
		initDisplay();
		super.layoutChildren();
		initLayout();
	}


	protected void initDisplay() { /* void */ } // liste d'affichage


	protected void initLayout() { /* void */ } // positionnement statique


	abstract protected boolean isInitialized();


	protected void onHeightComputed() { /* void */ }


	protected final void requestHeightComputing()
	{
		short h = (short) Math.max(0, computeHeight());

		if (h != _height)
		{
			_height = h;
			onHeightComputed();
		}
	}
}