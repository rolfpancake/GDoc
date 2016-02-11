package ui.content;

import org.jetbrains.annotations.NotNull;
import javafx.event.EventHandler;
import javafx.geometry.Bounds;
import javafx.geometry.VPos;
import javafx.scene.Group;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import data.Identifier;
import fonts.FontManager;
import fonts.FontWeight;
import main.Strings;
import ui.Graphics;


/**
 * Classe de base abstraite des conteneurs de méthodes, signaux, membres, constantes et thèmes.
 * Elle ajoute le prototype.
 */
abstract class DocPrototyped<D extends Identifier> extends Docable<D>
{
	static public final byte PROTOTYPE_ROW_HEIGHT = 25;

	static private final byte _V_GAP = 2;
	static private final Color _BACKGROUND_COLOR = Color.gray(248d / 255);

	protected Group suffix;
	private Group _prototype;
	private Text _name;
	private Rectangle _background;

	static private final EventHandler<MouseEvent> _MOUSE_HANDLER = new EventHandler<MouseEvent>()
	{
		@Override
		public void handle(MouseEvent event)
		{
			if (!(event.getSource() instanceof DocPrototyped)) return;
			DocPrototyped o = (DocPrototyped) event.getSource();

			if (event.getEventType() == MouseEvent.MOUSE_ENTERED)
				o._background.setOpacity(1);
			else if (!o.isOpened())
				o._background.setOpacity(0);
		}
	};


	protected DocPrototyped(@NotNull D identifier)
	{
		super(identifier);
	}


	public final void highlight(boolean value)
	{
		if (value) _name.setFill(Graphics.HIGHLIGHT);
		else _name.setFill(super.getData().nameIsValid() ? Graphics.TEXT_COLOR : Graphics.INVALID_ID);
	}


	@Override
	public void setWidth(short width)
	{
		_background.setWidth(width);
		super.setWidth(width);
	}


	@Override
	void open(boolean value)
	{
		_background.setOpacity(value ? 1 : 0);
		super.open(value);
	}


	void setLeftOffset(short offset)
	{
		super.setLeftOffset(offset);
		_prototype.setLayoutX(offset);
	}


	@Override
	protected short computeHeight()
	{
		short h = (short) (super.computeHeight() + PROTOTYPE_ROW_HEIGHT);
		return super.getDisplayMode() == DisplayMode.EMPTY && !super.isOpened() ? h : (short) (h + _V_GAP);
	}


	@NotNull
	protected Text getName() { return _name; }


	@Override
	protected void initDisplay()
	{
		_prototype = new Group();
		Identifier d = super.getData();
		_name = new Text(d.getName());
		_name.setTextOrigin(VPos.TOP);
		_name.setFill(d.nameIsValid() ? Graphics.TEXT_COLOR : Graphics.INVALID_ID);
		_name.setFont(FontManager.INSTANCE.getFont(FontWeight.Bold, Graphics.DOUBLE));
		_name.setMouseTransparent(true);
		_prototype.getChildren().add(_name);

		_background = new Rectangle(10, 10, _BACKGROUND_COLOR);
		_background.setOpacity(0);
		super.getChildren().addAll(_background, _prototype);
		super.initDisplay(); // description au-dessus

		super.addEventHandler(MouseEvent.MOUSE_ENTERED, _MOUSE_HANDLER);
		super.addEventHandler(MouseEvent.MOUSE_EXITED, _MOUSE_HANDLER);
	}


	@Override
	protected void initLayout()
	{
		super.initLayout();
		_name.setLayoutY((PROTOTYPE_ROW_HEIGHT - _name.getBoundsInLocal().getHeight()) / 2);

		Bounds b = _name.getBoundsInParent();
		double x = b.getMaxX() + Graphics.DOUBLE;
		Text p;

		if (this instanceof DocParameterized)
		{
			p = Graphics.CREATE_TEXT_FIELD(Strings.LEFT_PARENTHESIS);
			_prototype.getChildren().add(p);
			p.relocate(x, b.getMinY() + (b.getHeight() - p.getBoundsInLocal().getHeight()) / 2);
			x += p.getBoundsInLocal().getWidth() + Graphics.THIN;
		}

		if (suffix != null)
		{
			_prototype.getChildren().add(suffix);
			suffix.setLayoutX(x);
			// relocate() ajoute parfois 1 px à l'ordonnée avec la même valeur ??
			suffix.setLayoutY(_name.getLayoutY() + _name.getBaselineOffset() - suffix.getBaselineOffset());
			x += suffix.getBoundsInLocal().getWidth() + Graphics.THIN;
		}

		if (this instanceof DocParameterized)
		{
			p = Graphics.CREATE_TEXT_FIELD(Strings.RIGHT_PARENTHESIS);
			_prototype.getChildren().add(p);
			p.relocate(x, b.getMinY() + (b.getHeight() - p.getBoundsInLocal().getHeight()) / 2);
		}

		super.getDescription().setLayoutY(PROTOTYPE_ROW_HEIGHT + _V_GAP);
	}


	@Override
	protected void onHeightComputed()
	{
		_background.setHeight(super.getHeight());
	}
}