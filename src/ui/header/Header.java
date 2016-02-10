package ui.header;

import org.jetbrains.annotations.NotNull;
import javafx.scene.layout.Region;
import javafx.scene.shape.Rectangle;
import ui.Graphics;
import ui.classes.Historic;


public final class Header extends Region
{
	static private final byte _BARS = TabBar.MIN_HEIGHT + MenuBar.MIN_HEIGHT + HistBar.MIN_HEIGHT;
	static private final byte _SPACES = 2 * Graphics.PADDING - TypeTab.OVERLAP;

	private TabBar _tabBar;
	private HistBar _histBar;
	private MenuBar _menuBar;
	private Rectangle _background;


	public Header(@NotNull Historic historic )
	{
		super();
		super.setMinHeight(_BARS + _SPACES);

		_tabBar = new TabBar(historic.getCurrent());
		_menuBar = new MenuBar();
		_histBar = new HistBar(historic);
		_background = new Rectangle(2, 2, Graphics.GRAY_23);
		super.getChildren().addAll(_tabBar, _background, _menuBar, _histBar);
	}


	@NotNull
	public HistBar getHistBar() { return _histBar; }


	@NotNull
	public MenuBar getMenuBar() { return _menuBar; }


	@NotNull
	public TabBar getTabBar() { return _tabBar; }


	@Override
	protected void layoutChildren()
	{
		double w = super.getWidth();
		double h = super.getHeight() - _SPACES;
		short h1 = (short) Math.max(TabBar.MIN_HEIGHT, (double) TabBar.MIN_HEIGHT / _BARS * h);
		short h2 = (short) Math.max(MenuBar.MIN_HEIGHT, (double) MenuBar.MIN_HEIGHT / _BARS * h);

		_tabBar.setPrefSize(w, h1);

		_menuBar.setLayoutY(h1 + Graphics.PADDING - TypeTab.OVERLAP);
		_menuBar.setPrefSize(w, h2);

		_histBar.setLayoutY(h1 + h2 + _SPACES);
		_histBar.setPrefSize(w, super.getHeight() - _histBar.getLayoutY());

		_background.setLayoutY(h1 - TypeTab.OVERLAP);
		_background.setWidth(w);
		_background.setHeight(super.getHeight() - _background.getLayoutY());

		super.layoutChildren();
	}
}