package fonts;


import java.lang.ref.WeakReference;
import java.util.HashMap;
import javafx.scene.text.Font;
import exceptions.ErrorMessage;
import exceptions.ErrorMessageType;
import exceptions.NullArgumentException;
import main.Launcher;
import main.Paths;
import main.Strings;


public final class FontVariant
{
	static public final byte MIN_SIZE = 10;
	static public final byte MAX_SIZE = 20;

	private WeakReference<FontFamily> _family;
	private FontWeight _weight;
	private HashMap<Boolean, HashMap<Byte, Font>> _fonts = new HashMap<Boolean, HashMap<Byte, Font>>(1);


	FontVariant(FontFamily family)
	{
		if (family == null) throw new NullArgumentException();
		_family = new WeakReference<FontFamily>(family);
		_weight = FontWeight.Regular;
	}


	FontVariant(FontFamily family, FontWeight weight)
	{
		if (family == null) throw new NullArgumentException("family");
		_family = new WeakReference<FontFamily>(family);
		_weight = weight == null ? FontWeight.Regular : weight;
	}


	/**
	 * Récupére la famille.
	 *
	 * @return Un objet FontFamily
	 */
	public FontFamily getFamily() { return _family.get(); }


	/**
	 * Récupére le poids.
	 *
	 * @return Un objet FontWeight non nul
	 */
	public FontWeight getWeight() { return _weight; }


	/**
	 * Récupére une police.
	 *
	 * @return Un objet Font ou nul
	 */
	Font getFont(byte size)
	{
		return getFont(false, size);
	}


	/**
	 * Récupére une police.
	 *
	 * @return Un objet Font ou nul
	 */
	Font getFont(boolean italic, byte size)
	{
		size = (byte) Math.max(MIN_SIZE, Math.min(size, MAX_SIZE));
		HashMap<Byte, Font> m = _fonts.get(italic);

		if (m == null)
		{
			m = new HashMap<Byte, Font>(1);
			_fonts.put(italic, m);
		}

		Font f = m.get(size);

		if (f == null)
		{
			FontFamily a = _family.get();
			if (a == null) return null;
			if (m.size() == Byte.MAX_VALUE) return null;
			String n = Paths.FONTS + a.getName() + Strings.DASH;
			if (italic) n += (_weight == FontWeight.Regular ? "" : _weight.toString()) + "Italic";
			else n += _weight.toString();
			n += Paths.TTF;
			f = Font.loadFont((Object.class).getResourceAsStream(n), size);

			if (f == null) Launcher.INSTANCE.log(new ErrorMessage(ErrorMessageType.fontNotFound, n));
			if (f != null) m.put(size, f);
		}

		return f;
	}
}