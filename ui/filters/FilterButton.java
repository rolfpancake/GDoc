package ui.filters;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import javafx.geometry.Bounds;
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
import events.UIEvent;
import exceptions.EmptyArgumentException;
import fonts.FontManager;
import main.Strings;
import ui.Graphics;


public final class FilterButton extends Region
{
	static public final byte CORNER = 4;
	static public final byte UNQUANTIFIED = -1;

	static private final Background[] _BACKGROUNDS = new Background[8];
	static private final byte _OFFSET = -2;
	static private final byte _PADDING = 6;

	private HPos _position = null;
	private boolean _toggled = false;
	private Text _counter = null;
	private char _letter = 0;
	private short _quantity = UNQUANTIFIED;
	private Node _content;


	public FilterButton(@NotNull Object content)
	{
		if (content instanceof String || content instanceof Character)
		{
			String s = String.valueOf(content).trim();
			if (s.isEmpty()) throw new EmptyArgumentException("content");

			if (content instanceof Character)
			{
				s = s.toUpperCase();
				_letter = s.charAt(0);
			}
			else
			{
				s = Strings.CAPITALIZE(s.replace(Strings.UNDERLINE, Strings.SPACE));
			}

			Text t = Graphics.CREATE_TEXT_FIELD(s);
			t.setFont(FontManager.INSTANCE.getFont(_OFFSET));
			_content = t;
		}
		else if (content instanceof ImageView)
		{
			ImageView i = (ImageView) content;
			i.setSmooth(true);
			_content = i;
		}
		else
		{
			throw new IllegalArgumentException();
		}

		_content.setMouseTransparent(true);
		super.getChildren().add(_content);
		_INIT();
		_updateBackground();
	}


	public FilterButton(@NotNull String content, byte offset)
	{
		String s = content.trim();
		if (s.isEmpty()) throw new EmptyArgumentException("content");
		s = Strings.CAPITALIZE(s.replace(Strings.UNDERLINE, Strings.SPACE));
		_content = Graphics.CREATE_TEXT_FIELD(s, FontManager.INSTANCE.getFont(offset));
		_content.setMouseTransparent(true);
		super.getChildren().add(_content);
		_INIT();
		_updateBackground();
	}


	static private void _INIT()
	{
		if (_BACKGROUNDS[0] != null) return;

		Insets i = new Insets(Graphics.THIN);
		CornerRadii fc = new CornerRadii(CORNER);
		CornerRadii lc = new CornerRadii(CORNER, 0, 0, CORNER, false);
		CornerRadii cc = CornerRadii.EMPTY;
		CornerRadii rc = new CornerRadii(0, CORNER, CORNER, 0, false);

		Stop s0 = new Stop(0, Graphics.GRAY_24);
		Stop s1 = new Stop(1, Graphics.GRAY_21);
		LinearGradient g = new LinearGradient(0, 0, 0, 1.0, true, CycleMethod.NO_CYCLE, s0, s1);
		BackgroundFill fb = new BackgroundFill(Graphics.GRAY_12, fc, Insets.EMPTY);
		BackgroundFill lb = new BackgroundFill(Graphics.GRAY_12, lc, Insets.EMPTY);
		BackgroundFill cb = new BackgroundFill(Graphics.GRAY_12, cc, Insets.EMPTY);
		BackgroundFill rb = new BackgroundFill(Graphics.GRAY_12, rc, Insets.EMPTY);

		BackgroundFill f = new BackgroundFill(g, fc, i);
		_BACKGROUNDS[0] = new Background(fb, f);

		f = new BackgroundFill(g, lc, i);
		_BACKGROUNDS[1] = new Background(lb, f);

		f = new BackgroundFill(g, cc, i);
		_BACKGROUNDS[2] = new Background(cb, f);

		f = new BackgroundFill(g, rc, i);
		_BACKGROUNDS[3] = new Background(rb, f);

		s0 = new Stop(0, Graphics.GRAY_12);
		s1 = new Stop(1, Graphics.GRAY_15);
		g = new LinearGradient(0, 0, 0, 1.0, true, CycleMethod.NO_CYCLE, s0, s1);

		f = new BackgroundFill(g, fc, i);
		_BACKGROUNDS[4] = new Background(fb, f);

		f = new BackgroundFill(g, lc, i);
		_BACKGROUNDS[5] = new Background(lb, f);

		f = new BackgroundFill(g, cc, i);
		_BACKGROUNDS[6] = new Background(cb, f);

		f = new BackgroundFill(g, rc, i);
		_BACKGROUNDS[7] = new Background(rb, f);
	}


	/**
	 * Récupére la lettre majuscule.
	 */
	public final char getLetter() { return _letter; }


	public String getName() // debug
	{
		return _content instanceof Text ? ((Text) _content).getText() : null;
	}


	@Nullable
	public final HPos getPosition() { return _position; }


	public short getQuantity() { return _quantity; }


	public final boolean isToggled() { return _toggled; }


	public void quantify(short quantity)
	{
		if (quantity == _quantity) return;

		if (quantity == 0)
		{
			super.getChildren().remove(_counter);
			_counter = null;
			_quantity = 0;
		}
		else if (quantity > 0)
		{
			if (_counter == null)
			{
				_counter = Graphics.CREATE_TEXT_FIELD(Strings.PARENTHESISE(quantity), FontManager.DIGIT_FONT);
				if (_toggled) _counter.setEffect(Graphics.FULL_LIGHT_EFFECT);
				super.getChildren().add(_counter);
			}
			else
			{
				_counter.setText(Strings.PARENTHESISE(quantity));
				if (_counter.getParent() == null) super.getChildren().add(_counter);
			}
		}
		else // indéterminée mais > 0
		{
			super.getChildren().remove(_counter);
			_counter = null;
			if (_quantity < 0) return;
			_quantity = UNQUANTIFIED;
		}

		layoutChildren();
		super.fireEvent(new UIEvent(UIEvent.QUANTIFIED));
	}


	void setPosition(@Nullable HPos position)
	{
		if (position == _position) return;
		_position = position;
		_updateBackground();
	}


	final void toggle() { toggle(!_toggled); }


	final void toggle(boolean toggle) // publiquement par FilterGroup::setCurrent
	{
		if (toggle == _toggled) return;
		_content.setEffect(toggle ? Graphics.FULL_LIGHT_EFFECT : null);
		if (_counter != null) _counter.setEffect(toggle ? Graphics.FULL_LIGHT_EFFECT : null);
		_toggled = toggle;
		_updateBackground();
	}


	@Override
	protected void layoutChildren()
	{
		super.layoutChildren();
		Bounds b = _content.getBoundsInParent();
		double w = b.getWidth();
		super.setMinWidth(w);

		if (_counter != null)
		{
			w += _PADDING + _counter.getBoundsInLocal().getWidth();
			_content.relocate((super.getWidth() - w) / 2, (super.getHeight() - b.getHeight()) / 2);
			_counter.relocate(_content.getBoundsInParent().getMaxX() + _PADDING,
							  _content.getLayoutY() + _content.getBaselineOffset() - _counter.getBaselineOffset());
		}
		else
		{
			_content.relocate((super.getWidth() - w) / 2, (super.getHeight() - b.getHeight()) / 2);
		}
	}


	private void _updateBackground()
	{
		super.setBackground(_BACKGROUNDS[(_toggled ? 4 : 0) + (_position == null ? 0 : 1 + _position.ordinal())]);
	}
}