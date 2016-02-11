package ui.header;

import org.jetbrains.annotations.NotNull;
import javafx.scene.layout.Region;
import javafx.scene.shape.Rectangle;
import ui.Graphics;
import ui.content.Historic;


public final class Header extends Region
{
	static private final byte _BARS = TabBar.MIN_HEIGHT + MenuBar.HEIGHT + HistBar.MIN_HEIGHT;
	static private final byte _SPACES = 2 * Graphics.PADDING - TypeTab.OVERLAP;
	static private final byte _TABS_HEIGHT = 30;

	private TabBar _tabBar;
	private HistBar _histBar;
	private MenuBar _menuBar;
	private Rectangle _background;


	public Header(@NotNull Historic historic)
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

		_tabBar.setPrefSize(w, _TABS_HEIGHT);

		_menuBar.setLayoutY(_TABS_HEIGHT + Graphics.PADDING - TypeTab.OVERLAP);
		_menuBar.setPrefWidth(w);

		_histBar.setLayoutY(_menuBar.getBoundsInParent().getMaxY() + Graphics.PADDING);
		_histBar.setPrefSize(w, super.getHeight() - _histBar.getLayoutY());

		_background.setLayoutY(_TABS_HEIGHT - TypeTab.OVERLAP);
		_background.setWidth(w);
		_background.setHeight(super.getHeight() - _background.getLayoutY());
		super.layoutChildren();
	}
}