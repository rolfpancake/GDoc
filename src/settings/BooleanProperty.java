package settings;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;


/**
 * Une propriété booléenne qui vaut "0" (par défaut) ou "1".
 */
public final class BooleanProperty extends Property
{
	static private final String _FALSE = "0";
	static private final String _TRUE = "1";
	static private final String _BOOL_RE = "0|1";


	public BooleanProperty(boolean persistent, @NotNull String name, boolean defaultValue)
	{
		super(persistent, name, defaultValue ? _TRUE : _FALSE);
	}


	@Override
	public void setDefault(@Nullable String defaultValue)
	{
		super.setDefault(_parse(defaultValue, super.getDefault().isEmpty() ? _FALSE : super.getDefault()));
	}


	public void setDefault(boolean defaultValue)
	{
		super.setDefault(defaultValue ? _TRUE : _FALSE);
	}


	@Override
	public void setValue(@Nullable String value)
	{
		super.setValue(_parse(value, super.getDefault()));
	}


	public void setValue(boolean value)
	{
		super.setValue(value ? _TRUE : _FALSE);
	}


	public boolean toBoolean() { return super.getValue().equals(_TRUE); }


	public boolean toggle()
	{
		if (super.getValue().equals(_TRUE))
		{
			super.setValue(_FALSE);
			return false;
		}
		else
		{
			super.setValue(_TRUE);
			return true;
		}
	}


	private String _parse(String v, String d)
	{
		if (v == null) return d;
		v = v.trim();
		return v.matches(_BOOL_RE) ? v : d;
	}


	@Override
	protected void validate()
	{
		super.setValue(_parse(super.getValue(), super.getDefault()));
	}
}