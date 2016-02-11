package settings;

import org.jetbrains.annotations.NotNull;


public final class LogSection extends Section
{
	private BooleanProperty _hide;


	LogSection()
	{
		super("log");
		_hide = new BooleanProperty(true, "hide", true);
		super.addProperty(_hide);
	}


	@NotNull
	public BooleanProperty hide() { return _hide; }
}