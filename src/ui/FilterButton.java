package ui;

import com.sun.istack.internal.NotNull;
import com.sun.istack.internal.Nullable;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.Region;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;
import javafx.scene.text.Text;
import events.ChangeEvent;
import exceptions.EmptyArgumentException;
import exceptions.NullArgumentException;
import fonts.FontManager;


public final class FilterButton extends Region
{
	static public final byte CORNER = 4;

	static private Background _UP_LEFT_BACKGROUND;
	static private Background _UP_CENTER_BACKGROUND;
	static private Background _UP_RIGHT_BACKGROUND;
	static private Background _DOWN_LEFT_BACKGROUND;
	static private Background _DOWN_CENTER_BACKGROUND;
	static private Background _DOWN_RIGHT_BACKGROUND;
	static private final byte _OFFSET = -2;

	private Node _icon;
	private HPos _position;
	private boolean _toggled;


	public FilterButton(HPos position, String name)
	{
		super();
		if (position == null) throw new NullArgumentException("position");
		if (name == null) throw new NullArgumentException("name");
		name = name.trim();
		if (name.isEmpty()) throw new EmptyArgumentException("name");
		_position = position;

		_init();
		Text t = Graphics.CREATE_TEXT_FIELD(name);
		t.setFont(FontManager.INSTANCE.getFont(_OFFSET));
		_icon = t;
		super.setMinWidth(Graphics.TEXT_SIZE(name, t.getFont()));

		super.getChildren().add(_icon);
	}


	public FilterButton(HPos position, ImageView icon)
	{
		super();
		if (position == null) throw new NullArgumentException("position");
		if (icon == null) throw new NullArgumentException("icon");
		_position = position;
		_icon = icon;
		_init();
		super.setMinWidth(icon.getFitWidth());
	}


	@Nullable
	public String getName() { return _icon instanceof Text ? ((Text) _icon).getText() : null; }


	@NotNull
	public HPos getPosition() { return _position; }


	public boolean isToggled() { return _toggled; }


	public void toggle(boolean toggle)
	{
		if (toggle == _toggled) return;
		_toggled = toggle;

		if (_toggled)
		{
			if (_position == HPos.LEFT) super.setBackground(_DOWN_LEFT_BACKGROUND);
			else if (_position == HPos.CENTER) super.setBackground(_DOWN_CENTER_BACKGROUND);
			else super.setBackground(_DOWN_RIGHT_BACKGROUND);
			_icon.setEffect(Graphics.FULL_LIGHT_EFFECT);
		}
		else
		{
			if (_position == HPos.LEFT) super.setBackground(_UP_LEFT_BACKGROUND);
			else if (_position == HPos.CENTER) super.setBackground(_UP_CENTER_BACKGROUND);
			else super.setBackground(_UP_RIGHT_BACKGROUND);
			_icon.setEffect(null);
		}

		super.fireEvent(new ChangeEvent());
	}


	public void toggle() { toggle(!_toggled); }


	@Override
	protected void layoutChildren()
	{
		super.layoutChildren();
		_icon.setLayoutX((super.getWidth() - _icon.getBoundsInLocal().getWidth()) / 2);
		_icon.setLayoutY((super.getHeight() - _icon.getBoundsInLocal().getHeight()) / 2);
	}


	private void _init()
	{
		if (_UP_LEFT_BACKGROUND == null)
		{
			Insets i = new Insets(Graphics.THIN);
			CornerRadii lc = new CornerRadii(CORNER, 0, 0, CORNER, false);
			CornerRadii cc = CornerRadii.EMPTY;
			CornerRadii rc = new CornerRadii(0, CORNER, CORNER, 0, false);

			Stop s0 = new Stop(0, Graphics.GRAY_24);
			Stop s1 = new Stop(1, Graphics.GRAY_21);
			LinearGradient g = new LinearGradient(0, 0, 0, 1.0, true, CycleMethod.NO_CYCLE, s0, s1);
			BackgroundFill lb = new BackgroundFill(Graphics.GRAY_12, lc, Insets.EMPTY);
			BackgroundFill cb = new BackgroundFill(Graphics.GRAY_12, cc, Insets.EMPTY);
			BackgroundFill rb = new BackgroundFill(Graphics.GRAY_12, rc, Insets.EMPTY);
			BackgroundFill f = new BackgroundFill(g, lc, i);
			_UP_LEFT_BACKGROUND = new Background(lb, f);

			f = new BackgroundFill(g, cc, i);
			_UP_CENTER_BACKGROUND = new Background(cb, f);

			f = new BackgroundFill(g, rc, i);
			_UP_RIGHT_BACKGROUND = new Background(rb, f);

			s0 = new Stop(0, Graphics.GRAY_12);
			s1 = new Stop(1, Graphics.GRAY_15);
			g = new LinearGradient(0, 0, 0, 1.0, true, CycleMethod.NO_CYCLE, s0, s1);
			f = new BackgroundFill(g, lc, i);
			_DOWN_LEFT_BACKGROUND = new Background(lb, f);

			f = new BackgroundFill(g, cc, i);
			_DOWN_CENTER_BACKGROUND = new Background(cb, f);

			f = new BackgroundFill(g, rc, i);
			_DOWN_RIGHT_BACKGROUND = new Background(rb, f);
		}

		if (_position == HPos.CENTER) super.setBackground(_UP_CENTER_BACKGROUND);
		else if (_position == HPos.LEFT) super.setBackground(_UP_LEFT_BACKGROUND);
		else super.setBackground(_UP_RIGHT_BACKGROUND);
	}
}