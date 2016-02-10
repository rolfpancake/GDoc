package ui.header;

import org.jetbrains.annotations.NotNull;
import javafx.event.EventHandler;
import javafx.geometry.Bounds;
import javafx.scene.Group;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.shape.ArcTo;
import javafx.scene.shape.LineTo;
import javafx.scene.shape.MoveTo;
import javafx.scene.shape.Path;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import data.Type;
import events.KeyboardManager;
import events.TabEvent;
import fonts.FontManager;
import fonts.FontWeight;
import main.Strings;
import ui.Graphics;
import ui.Tab;


public final class TypeTab extends Tab
{
	static final byte MIN_WIDTH = 80;
	static final short MAX_WIDTH = 300;
	static final byte MIN_HEIGHT = 20;
	static final byte MAX_HEIGHT = 40;
	static final byte OVERLAP = Graphics.DOUBLE;

	static private final double _ANGLE_TAN = Math.tan(70 * Math.PI / 180);
	static private final byte _ARC_LENGTH = 8;
	static private final byte _ARC_HEIGHT = 6;
	static private final byte _OFFSET = 2;
	static private final FontWeight _WEIGHT = FontWeight.SemiBold;

	private Text _title;
	private ImageView _closeButton;
	private Group _left;
	private Rectangle _centerBody;
	private Rectangle _centerBorder;
	private Group _right;
	private Type _type;
	private boolean _closable;
	private byte _drawnHeight;
	private double _sideWidth;

	private EventHandler<MouseEvent> _clickHandler = new EventHandler<MouseEvent>()
	{
		@Override
		public void handle(MouseEvent event) { _onClick(event); }
	};


	TypeTab(@NotNull Type type)
	{
		super();
		super.setMinHeight(MIN_HEIGHT);
		super.setMaxHeight(MAX_HEIGHT);

		_type = type;
		_closable = true;
		_drawnHeight = 0;

		_title = Graphics.CREATE_TEXT_FIELD(type.getName(), FontManager.INSTANCE.getFont(_WEIGHT, _OFFSET));
		_left = new Group();
		_right = new Group();
		_closeButton = Graphics.GET_ICON_16((byte) 0, (byte) 0);
		_closeButton.setPickOnBounds(true);

		_centerBorder = new Rectangle(2, BORDER_THICKNESS, BORDER_COLOR);
		_centerBody = new Rectangle(2, 2, Graphics.GRAY_23);

		super.getChildren().addAll(_centerBorder, _centerBody, _left, _right, _title, _closeButton);
		super.addEventHandler(MouseEvent.MOUSE_CLICKED, _clickHandler);
	}


	public void setType(@NotNull Type type)
	{
		if (type == _type) return;
		_type = type;
		_title.setText(type.getName());
	}


	@NotNull
	Type getType() { return _type; }


	boolean isClosable() { return _closable; }


	void setClosable(boolean value)
	{
		_closable = value;
		if (!value) super.getChildren().remove(_closeButton);
		else if (_closeButton.getParent() == null) super.getChildren().add(_closeButton);
	}


	@Override
	protected void layoutChildren()
	{
		if (super.getHeight() != _drawnHeight)
		{
			if (super.getHeight() < MIN_HEIGHT) return;
			_drawnHeight = (byte) Math.min(super.getHeight(), MAX_HEIGHT);
			_drawSide(_left);
			_drawSide(_right);
		}

		super.layoutChildren();
		_right.setLayoutX(super.getWidth() - _sideWidth);

		_centerBorder.setLayoutX(_sideWidth);
		_centerBody.setLayoutX(_centerBorder.getLayoutX());
		_centerBorder.setWidth(_right.getLayoutX() - _centerBorder.getLayoutX());
		_centerBody.setWidth(_centerBorder.getWidth());
		_centerBody.setLayoutY(BORDER_THICKNESS);
		_centerBody.setHeight(super.getHeight() - BORDER_THICKNESS);

		_title.setLayoutX(_sideWidth + Graphics.PADDING);
		_title.setLayoutY(Tab.BORDER_THICKNESS + (super.getHeight() - _title.getBoundsInLocal().getHeight()) / 2);
		String s = _type.getName();

		if (_closable)
		{
			Bounds b = _closeButton.getBoundsInLocal();
			_closeButton.setLayoutY(_title.getLayoutY() + (_title.getBoundsInLocal().getHeight() - b.getHeight()) / 2);
			_closeButton.setLayoutX(_right.getLayoutX() - b.getWidth());

			if (s != null)
				_title.setText(Strings.ELLIPSE(s, _title.getFont(),
											   _closeButton.getLayoutX() - _title.getLayoutX() - Graphics.PADDING));
		}
		else if (s != null)
		{
			_title.setText(Strings.ELLIPSE(s, _title.getFont(), _right.getLayoutX() - _title.getLayoutX()));
		}
	}


	private double _drawBorder(Path p, double x, double y, double w, double h, boolean r)
	{
		LineTo l;
		ArcTo a;
		byte s = (byte) (r ? -1 : 1);

		x += _ARC_LENGTH * s;
		y -= _ARC_HEIGHT;
		a = new ArcTo();
		a.setX(x);
		a.setY(y);
		a.setRadiusX(_ARC_LENGTH);
		a.setRadiusY(_ARC_HEIGHT);
		a.setSweepFlag(r);

		x += w * s;
		y -= h;
		l = new LineTo(x, y);
		p.getElements().addAll(a, l);

		x += _ARC_LENGTH * s;
		y -= _ARC_HEIGHT;
		a = new ArcTo();
		a.setX(x);
		a.setY(y);
		a.setRadiusX(_ARC_LENGTH);
		a.setRadiusY(_ARC_HEIGHT);
		a.setSweepFlag(!r);

		x += OVERLAP * s;
		l = new LineTo(x, y);
		p.getElements().addAll(a, l);

		return x;
	}


	private void _drawSide(Group s)
	{
		boolean r = s == _right;
		double h = _drawnHeight - OVERLAP - BORDER_THICKNESS - 2 * _ARC_HEIGHT;
		double w = h / _ANGLE_TAN;
		_sideWidth = w + 2 * _ARC_LENGTH;
		double a = (double) BORDER_THICKNESS / 2;
		double b = r ? _sideWidth - BORDER_THICKNESS : a;
		double x = b;
		double y = _drawnHeight - OVERLAP - a;
		s.getChildren().clear();

		Path p1 = new Path(new MoveTo(x, y)); // fond
		x = _drawBorder(p1, x, y, w, h, r);
		/* au moins 1 px supplémentaire est nécessaire */
		p1.getElements().addAll(new LineTo(x, y + OVERLAP), new LineTo(b, y + OVERLAP), new LineTo(b, y));
		p1.setFill(Graphics.GRAY_23);
		p1.setStroke(null); // non nul par défaut pour un Path
		s.getChildren().add(p1);

		Path p2 = new Path(new MoveTo(b, y)); // bordure
		p2.setStrokeWidth(BORDER_THICKNESS);
		p2.setStroke(BORDER_COLOR);
		p2.setFill(null); // non nul par défaut pour un Path
		_drawBorder(p2, b, y, w, h, r);
		s.getChildren().add(p2);
	}


	private void _onClick(MouseEvent e)
	{
		e.consume(); // l'événement est bien consommé
		if (e.getButton() != MouseButton.PRIMARY) return;
		KeyboardManager k = KeyboardManager.GET_MANAGER(this);

		if (e.getTarget() instanceof ImageView)
		{
			if (k.isEmpty()) super.fireEvent(new TabEvent(TabEvent.CLOSE));
			else if (k.isKey(KeyCode.SHIFT)) super.fireEvent(new TabEvent(TabEvent.CLOSE_OTHERS));
			else if (k.isKey(KeyCode.CONTROL)) super.fireEvent(new TabEvent(TabEvent.CLOSE_RIGHT));
			else if (k.isKey(KeyCode.ALT)) super.fireEvent(new TabEvent(TabEvent.CLOSE_LEFT));
		}
		else if (!super.isSelected())
		{
			super.fireEvent(new TabEvent(TabEvent.SELECT));
		}
	}
}