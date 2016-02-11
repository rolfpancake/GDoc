package data;


/**
 * Classe de base abstraite des données décrites. Toutes les occurrences de Described peuvent être supprimées.
 */
abstract public class Described extends Named
{
	public String description;


	protected Described() { super(); }
}