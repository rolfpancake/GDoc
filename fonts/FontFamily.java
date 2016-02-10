package fonts;


import java.util.ArrayList;
import javafx.scene.text.Font;
import exceptions.EmptyArgumentException;
import exceptions.NullArgumentException;


public final class FontFamily
{
	private String _name;
	private ArrayList<FontVariant> _variants = new ArrayList<FontVariant>(1);


	FontFamily(String name)
	{
		if (name == null) throw new NullArgumentException();
		name = name.trim();
		if (name.isEmpty()) throw new EmptyArgumentException();
		_name = name;
	}


	/**
	 * Récupère le nom.
	 *
	 * @return Une chaîne de caractères non vide et non nulle
	 */
	public String getName() { return _name; }


	/**
	 * Récupère une variante.
	 *
	 * @return Un objet FontVariant non nul
	 */
	public FontVariant getVariant(FontWeight weight)
	{
		if (weight == null) weight = FontWeight.Regular;
		for (FontVariant i : _variants) if (i.getWeight() == weight) return i;
		FontVariant w = new FontVariant(this, weight);
		_variants.add(w);
		return w;
	}


	Font getFont(FontWeight weight, boolean italic, byte size)
	{
		return getVariant(weight).getFont(italic, size);
	}
}