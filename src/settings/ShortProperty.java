package settings;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import main.Strings;


/**
 * Une propriété entière signée ou non dont la valeur par défaut est "0".
 */
public final class ShortProperty extends Property
{
	static private final String _ZERO = "0";
	static private final String _INT_RE = "\\-?\\d+";
	static private final String _UINT_RE = "\\d+";
	static private final String _PLUS_RE = "^\\+";
	static private final String _WHITESPACE = "\\p{Blank}*"; // entre - et le premier chiffre

	private boolean _unsigned;


	public ShortProperty(boolean persistent, @NotNull String name, boolean unsigned, short defaultValue)
	{
		super(persistent, name, String.valueOf(unsigned ? Math.max(0, defaultValue) : defaultValue));
		_unsigned = unsigned;
	}


	public boolean isUnsigned() { return _unsigned; }


	@Override
	public void setDefault(@Nullable String defaultValue)
	{
		super.setDefault(_parse(defaultValue, super.getDefault().isEmpty() ? _ZERO : super.getDefault()));
	}


	public void setDefault(short value)
	{
		super.setDefault(String.valueOf(_unsigned ? Math.max(0, value) : value));
	}


	@Override
	public void setValue(@Nullable String value)
	{
		super.setValue(_parse(value, super.getDefault()));
	}


	public void setValue(short value)
	{
		super.setValue(String.valueOf(_unsigned ? Math.max(0, value) : value));
	}


	public short toShort() { return Short.valueOf(super.getValue()); }


	private String _parse(String v, String d)
	{
		if (v == null) return d;

		v = v.trim().replaceFirst(_PLUS_RE, Strings.EMPTY);
		if (_unsigned) return v.matches(_UINT_RE) ? v : d;

		v = v.replaceFirst(_WHITESPACE, Strings.EMPTY);
		return v.matches(_INT_RE) ? v : d;
	}


	@Override
	protected void validate()
	{
		super.setValue(_parse(super.getValue(), super.getDefault()));
	}
}