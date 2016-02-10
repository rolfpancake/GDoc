package data;


import org.jetbrains.annotations.Nullable;


/**
 * Classe de base abstraite des données nommées.
 */
abstract class Named
{
	private String _name;


	protected Named() { /* void */ }


	/**
	 * Récupére le nom.
	 *
	 * @return Un objet String non vide ou null
	 */
	@Nullable
	public final String getName() { return _name; }


	/**
	 * Détermine si le nom est valide.
	 */
	abstract public boolean nameIsValid();


	@Override
	public String toString() { return _name; }


	void setName(String value) { if (_name == null) _name = value; }
}