package fonts;

import java.util.ArrayList;
import javafx.scene.text.Font;
import main.Strings;


public final class FontManager
{
	static public final String DEFAULT_FAMILY_NAME = "OpenSans"; // taille minimale 10 ?
	static public final String MONO_SPACE_FAMILY_NAME = "VeraMono";
	static public final FontFamily DEFAULT_FAMILY = new FontFamily(DEFAULT_FAMILY_NAME);
	static public final FontManager INSTANCE = new FontManager();

	static public final byte DEFAULT_SIZE = 12;
	//static public final byte MIN_OFFSET = -4;

	static public final Font DIGIT_FONT = INSTANCE.getFont(FontWeight.Light, (byte) -2);
	static private final byte _MIN_SIZE = 6;
	static private final byte _MAX_OFFSET = 5;

	private ArrayList<FontFamily> _families = new ArrayList<FontFamily>(1);


	private FontManager()
	{
		_families.add(DEFAULT_FAMILY);
	}


	public FontFamily getFamily(String family)
	{
		family = Strings.CLEAN(family);
		if (family == null) return null;

		for (FontFamily i : _families) if (family.equals(i.getName())) return i;
		FontFamily f = new FontFamily(family);
		_families.add(f);
		return f;
	}


	public Font getFont()
	{
		return DEFAULT_FAMILY.getFont(FontWeight.Regular, false, DEFAULT_SIZE);
	}


	public Font getFont(boolean italic)
	{
		return getFont(DEFAULT_FAMILY_NAME, FontWeight.Regular, italic, (byte) 0);
	}


	public Font getFont(byte offset)
	{
		return getFont(DEFAULT_FAMILY_NAME, FontWeight.Regular, false, offset);
	}


	public Font getFont(boolean italic, byte offset)
	{
		return getFont(DEFAULT_FAMILY_NAME, FontWeight.Regular, italic, offset);
	}


	public Font getFont(FontWeight weight)
	{
		return getFont(DEFAULT_FAMILY_NAME, weight, false, (byte) 0);
	}


	public Font getFont(FontWeight weight, byte offset)
	{
		return getFont(DEFAULT_FAMILY_NAME, weight, false, offset);
	}


	public Font getFont(FontWeight weight, boolean italic, byte offset)
	{
		return getFont(DEFAULT_FAMILY_NAME, weight, italic, offset);
	}


	public Font getFont(String family)
	{
		return getFont(family, FontWeight.Regular, false, (byte) 0);
	}


	public Font getFont(String family, byte offset)
	{
		return getFont(family, FontWeight.Regular, false, offset);
	}


	public Font getFont(String family, FontWeight weight, byte offset)
	{
		return getFont(family, weight, false, offset);
	}


	public Font getFont(String family, FontWeight weight, boolean italic, byte offset)
	{
		FontFamily f = getFamily(family);
		byte s = (byte) (Math.max(_MIN_SIZE, DEFAULT_SIZE + Math.min(offset, _MAX_OFFSET)));
		return f != null ? f.getFont(weight, italic, s) : null;
	}
}