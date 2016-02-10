package settings;

import org.jetbrains.annotations.NotNull;


public final class WindowSection extends Section
{
	private ShortProperty _width;
	private ShortProperty _height;
	private BooleanProperty _maximized;


	WindowSection()
	{
		super("window");
		_width = new ShortProperty(false, "width", true, (short) 900);
		_height = new ShortProperty(false, "height", true, (short) 650);
		_maximized = new BooleanProperty(false, "maximized", false);
		super.addProperty(_width, _height, _maximized);
	}


	@NotNull
	public ShortProperty width() { return _width; }


	@NotNull
	public ShortProperty height() { return _height; }


	@NotNull
	public BooleanProperty maximized() { return _maximized; }
}