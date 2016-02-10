package ui.types;

import org.jetbrains.annotations.NotNull;
import javafx.scene.Group;
import javafx.scene.image.ImageView;
import javafx.scene.paint.Color;
import javafx.scene.shape.ArcTo;
import javafx.scene.shape.LineTo;
import javafx.scene.shape.MoveTo;
import javafx.scene.shape.Path;
import ui.Graphics;
import ui.Tab;


final class ListTab extends Tab
{
	static private final byte _INTER_ARC_WIDTH = 15;
	static private final byte _INTER_ARC_HEIGHT = 5;
	static private final byte _HEAD_ARC_MARGIN = 9;

	static public final byte TAB_HEIGHT = 2 * (FOOT_ARC_MARGIN + FOOT_ARC_LENGTH + _INTER_ARC_HEIGHT +
											   HEAD_ARC_LENGTH + _HEAD_ARC_MARGIN);
	static public final byte LEFT_MARGIN = 29;
	static public final byte RIGHT_MARGIN = 7;
	static public final byte TAB_WIDTH = LEFT_MARGIN + BORDER_THICKNESS + RIGHT_MARGIN;

	static private final byte _TAB_WIDE_WIDTH = TAB_WIDTH - MARGIN - BORDER_THICKNESS;
	static private final byte _TAB_SMALL_WIDTH = LEFT_MARGIN - MARGIN - BORDER_THICKNESS;

	private ImageView _icon;
	private ListType _type;
	private Group _tab;
	private boolean _layout;


	ListTab(@NotNull ListType type)
	{
		super();
		_type = type;
		_tab = _GET_TAB();

		byte c = 0;
		byte r = 0;

		if (type == ListType.BUILT_IN) c = 1;
		else if (type == ListType.ROOT) c = 2;
		else if (type == ListType.RESOURCE) c = 3;
		else if (type != ListType.ALL)
		{
			r = 1;
			if (type == ListType.NODE2D) c = 1;
			else if (type == ListType.SPATIAL) c = 2;
		}

		_icon = Graphics.GET_ICON_24(c, r);
		_icon.setMouseTransparent(true);

		super.getChildren().addAll(_tab, _icon);
		_fit();
	}


	static private LineTo _DRAW_BORDER(Path p, double x, double y)
	{
		LineTo l;
		ArcTo a;

		y += FOOT_ARC_MARGIN;
		l = new LineTo(x, y);
		x -= FOOT_ARC_THICKNESS;
		y += FOOT_ARC_LENGTH;
		a = new ArcTo();
		a.setX(x);
		a.setY(y);
		a.setRadiusX(FOOT_ARC_THICKNESS);
		a.setRadiusY(FOOT_ARC_LENGTH);
		a.setSweepFlag(true);
		p.getElements().addAll(l, a);

		x -= _INTER_ARC_WIDTH;
		y += _INTER_ARC_HEIGHT;
		l = new LineTo(x, y);
		x -= HEAD_ARC_THICKNESS;
		y += HEAD_ARC_LENGTH;
		a = new ArcTo();
		a.setX(x);
		a.setY(y);
		a.setRadiusX(HEAD_ARC_THICKNESS);
		a.setRadiusY(HEAD_ARC_LENGTH);
		p.getElements().addAll(l, a);

		y += 2 * _HEAD_ARC_MARGIN;
		l = new LineTo(x, y);
		x += HEAD_ARC_THICKNESS;
		y += HEAD_ARC_LENGTH;
		a = new ArcTo();
		a.setX(x);
		a.setY(y);
		a.setRadiusX(HEAD_ARC_THICKNESS);
		a.setRadiusY(HEAD_ARC_LENGTH);
		x += _INTER_ARC_WIDTH;
		p.getElements().addAll(l, a);

		y += _INTER_ARC_HEIGHT;
		l = new LineTo(x, y);
		x += FOOT_ARC_THICKNESS;
		y += FOOT_ARC_LENGTH;
		a = new ArcTo();
		a.setX(x);
		a.setY(y);
		a.setRadiusX(FOOT_ARC_THICKNESS);
		a.setRadiusY(FOOT_ARC_LENGTH);
		a.setSweepFlag(true);
		p.getElements().addAll(l, a);
		l = new LineTo(x, y + FOOT_ARC_MARGIN);
		p.getElements().add(l);

		return l;
	}


	static private Group _GET_TAB()
	{
		Group t = new Group();
		double x = LEFT_MARGIN + (float) BORDER_THICKNESS / 2;
		double y = 0f;

		Path p1 = new Path(new MoveTo(x, y)); // fond
		LineTo l = _DRAW_BORDER(p1, x, y);
		x = l.getX();
		y = l.getY();
		p1.getElements().addAll(new LineTo(x + RIGHT_MARGIN, y), new LineTo(x + RIGHT_MARGIN, 0), new LineTo(x, 0));
		p1.setFill(Graphics.BACKGROUND_COLOR);
		p1.setStroke(null); // non nul par défaut pour un Path
		t.getChildren().add(p1);

		Path p2 = new Path(new MoveTo(x, 0)); // bordure
		p2.setStrokeWidth(BORDER_THICKNESS);
		p2.setStroke(Color.gray(50d / 255));
		p2.setFill(null); // non nul par défaut pour un Path
		_DRAW_BORDER(p2, x, 0);
		t.getChildren().add(p2);

		return t;
	}


	@Override
	public String toString() { return _type.toString(); }


	@NotNull
	ListType getType() { return _type; }


	@Override
	public void select(boolean select)
	{
		super.select(select);
		_fit();
	}


	@Override
	protected void layoutChildren()
	{
		if (!_layout) return;
		_layout = false;
		byte w = super.isSelected() ? _TAB_WIDE_WIDTH : _TAB_SMALL_WIDTH;
		_icon.setLayoutX(MARGIN + BORDER_THICKNESS + (w - _icon.getFitWidth()) / 2);
		_icon.setLayoutY((TAB_HEIGHT - _icon.getFitHeight()) / 2);
	}


	private void _fit()
	{
		byte s;

		if (super.isSelected())
		{
			super.setEffect(null);
			s = Graphics.ICON_SIZE_24;
		}
		else
		{
			s = _TAB_SMALL_WIDTH - 2 * MARGIN;
			super.setEffect(UNSELECTED_EFFECT);
		}

		_icon.setFitWidth(s);
		_icon.setFitHeight(s);
		_layout = true;
	}
}