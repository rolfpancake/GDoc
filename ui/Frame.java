package ui;

import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;


/**
 * Classe de base des contenus fenêtrés.
 */
public class Frame extends Region
{
	static private final byte _SCROLL_BAR_THICKNESS = 7;
	static private final byte _SCROLL_BAR_CORNER = 4;
	static private final Color _SCROLL_BAR_COLOR = Color.gray(0.8);
	static private final byte _FRAMING_MARGIN = 30;

	private VBox _container; // pour occuper toute la place sans toucher au contenu
	private Rectangle _hScrollBar;
	private Rectangle _vScrollBar;
	private Region _content;
	private double _x, _y; // coordonnée maximale d'une barre
	private double _w, _h; // déplacement maximal du contenu
	private double _d;
	private boolean _scrolling = false; // éviter un layout pendant le scrolling
	private boolean _resizing = false; // éviter un layout en dehors des redimensionnements
	private Rectangle _clip;
	private Insets _margins;

	private ChangeListener<Object> _changeListener = new ChangeListener<Object>()
	{
		@Override
		public void changed(ObservableValue<?> observable, Object oldValue, Object newValue)
		{
			/* l'écouteur est placé sur l'occurrence elle-même ainsi que sur le contenu, dans le premier cas il
		       faut juste permettre le layout, et dans le second le déclencher */

			if (_content != null && !_resizing &&
				(observable == _content.widthProperty() || observable == _content.heightProperty()))
			{
				Platform.runLater(new Runnable()
				{
					@Override
					public void run() { _layoutLater(); }
				});
			}

			_resizing = true;
		}
	};

	private EventHandler<MouseEvent> _mouseHandler = new EventHandler<MouseEvent>()
	{
		@Override
		public void handle(MouseEvent event)
		{
			if (event.getEventType() == MouseEvent.MOUSE_DRAGGED)
			{
				if (event.getSource() == _vScrollBar)
				{
					_vScrollBar.setLayoutY(Math.max(0, Math.min(_vScrollBar.getLayoutY() + event.getY() - _d, _y)));
					_updateVContent();
				}
				else if (event.getSource() == _hScrollBar)
				{
					_hScrollBar.setLayoutX(Math.max(0, Math.min(_hScrollBar.getLayoutX() + event.getX() - _d, _x)));
					_updateHContent();
				}
			}
			else if (event.getEventType() == MouseEvent.MOUSE_RELEASED)
			{
				_scrolling = false;
			}
			else if (event.getEventType() == MouseEvent.MOUSE_PRESSED)
			{
				_d = event.getSource() == _vScrollBar ? event.getY() : event.getX();
				_scrolling = true;
			}

			event.consume();

		}
	};


	private EventHandler<ScrollEvent> _scrollHandler = new EventHandler<ScrollEvent>()
	{
		@Override
		public void handle(ScrollEvent event)
		{
			double d = (event.isShiftDown() ? 2 : 1) * event.getDeltaY();

			if (_w > 0 && (event.isControlDown() || _h == 0))
			{
				_container.setLayoutX(Math.max(-_w, Math.min(_container.getLayoutX() + d, 0)));
				_updateHBar();
			}
			else if (_h > 0)
			{
				_container.setLayoutY(Math.max(-_h, Math.min(_container.getLayoutY() + d, 0)));
				_updateVBar();
			}

			event.consume();
		}
	};


	public Frame(Insets margins)
	{
		super();

		_margins = margins == null ? new Insets(1) : margins;
		super.setBackground(new Background(new BackgroundFill(Color.TRANSPARENT, null, null)));

		_vScrollBar = new Rectangle(_SCROLL_BAR_THICKNESS, _SCROLL_BAR_THICKNESS);
		_vScrollBar.setArcWidth(_SCROLL_BAR_CORNER);
		_vScrollBar.setArcHeight(_SCROLL_BAR_CORNER);
		_vScrollBar.setFill(_SCROLL_BAR_COLOR);

		_hScrollBar = new Rectangle(_SCROLL_BAR_THICKNESS, _SCROLL_BAR_THICKNESS);
		_hScrollBar.setArcWidth(_SCROLL_BAR_CORNER);
		_hScrollBar.setArcHeight(_SCROLL_BAR_CORNER);
		_hScrollBar.setFill(_SCROLL_BAR_COLOR);

		_container = new VBox();
		_container.setPadding(_margins);
		_container.setFillWidth(true);

		_clip = new Rectangle(100, 100, Color.BLACK);
		super.setClip(_clip);
		super.getChildren().addAll(_container, _hScrollBar, _vScrollBar);

		super.widthProperty().addListener(_changeListener);
		super.heightProperty().addListener(_changeListener);

		super.addEventHandler(ScrollEvent.SCROLL, _scrollHandler);
		_vScrollBar.addEventHandler(MouseEvent.MOUSE_PRESSED, _mouseHandler);
		_vScrollBar.addEventHandler(MouseEvent.MOUSE_DRAGGED, _mouseHandler);
		_vScrollBar.addEventHandler(MouseEvent.MOUSE_RELEASED, _mouseHandler);
		_hScrollBar.addEventHandler(MouseEvent.MOUSE_PRESSED, _mouseHandler);
		_hScrollBar.addEventHandler(MouseEvent.MOUSE_DRAGGED, _mouseHandler);
		_hScrollBar.addEventHandler(MouseEvent.MOUSE_RELEASED, _mouseHandler);
	}


	public final void fitWidth()
	{
		super.setPrefWidth(_content.getBoundsInLocal().getWidth() + _margins.getLeft() + _margins.getRight() +
								   (_vScrollBar.isVisible() ? _SCROLL_BAR_THICKNESS : 0));
	}


	public final void frame(Node child)
	{
		if (child == null) return;

		double y0 = child.getLayoutY(); // y de child dans _container
		Parent p = child.getParent();

		while (p != null)
		{
			y0 += p.getLayoutY();
			p = p.getParent();
			if (p == _container) break;
		}

		if (p != _container) return;

		double h = super.getHeight();
		double ch = child.getBoundsInParent().getHeight();
		double y1 = y0 + ch; // y basse de child dans _container
		double y2 = y0 + _container.getLayoutY(); // y de child dans l'occurrence
		double y3 = y1 + _container.getLayoutY(); // y basse de child dans l'occurrence
		double d = (h - ch) / 2;

		_container.setLayoutX(0);
		_updateHBar();

		if (y2 < 0)
		{
			_container.setLayoutY(Math.max(0, Math.min(_FRAMING_MARGIN, d)) - y0);
			_updateVBar();
		}
		else if (y3 > h)
		{
			_container.setLayoutY(Math.max(0, h - Math.min(_FRAMING_MARGIN, d) - ch) - y0);
			_updateVBar();
		}
	}


	public final boolean hasContent() { return _content != null; }


	@Override
	public void requestLayout() // quand le contenu n'utilise pas un layout pane
	{
		_resizing = true;
		super.requestLayout();
	}


	public void resetScrolling(boolean horizontal, boolean vertical)
	{
		if (horizontal)
		{
			_container.setLayoutX(0);
			_hScrollBar.setLayoutX(0);
		}

		if (vertical)
		{
			_container.setLayoutY(0);
			_hScrollBar.setLayoutY(0);
		}
	}


	public final void setContent(Region content)
	{
		if (content == _content)
		{
			if (content.getParent() != _container) _container.getChildren().add(content);
			return;
		}

		if (_content != null)
		{
			_container.getChildren().remove(_content);
			_content.widthProperty().removeListener(_changeListener);
			_content.heightProperty().removeListener(_changeListener);
		}

		if (content != null)
		{
			_container.setLayoutX(0);
			_container.setLayoutY(0);
			_container.getChildren().add(content);
			content.widthProperty().addListener(_changeListener);
			content.heightProperty().addListener(_changeListener);
			_container.layout();
		}

		_content = content;
		_vScrollBar.setLayoutY(0);
		_hScrollBar.setLayoutX(0);
		_resizing = true;
	}


	@Override
	protected void layoutChildren()
	{
		if (_scrolling || !_resizing) return;
		_resizing = false;

		if (_content == null)
		{
			_h = _w = 0;
			_vScrollBar.setVisible(false);
			_hScrollBar.setVisible(false);
			super.layoutChildren();
			return;
		}

		double w = super.getWidth();
		double h = super.getHeight();
		_clip.setWidth(w);
		_clip.setHeight(h);
		_container.setPrefSize(w, h);
		super.layoutChildren();

		double fw = w;
		double fh = h;
		/* si les marges horizontales sont ajoutées ici la barre horizontale est toujours visible */
		//TODO l'exclusion des marges empêche la visibilité du type le plus long dans TypePane
		double cw = _content.getBoundsInLocal().getWidth();
		double ch = _content.getBoundsInLocal().getHeight() + _margins.getTop() + _margins.getBottom();
		boolean vs = ch > h;
		boolean hs = cw > w;

		if (vs)
		{
			fw -= _SCROLL_BAR_THICKNESS;
			hs = hs || cw > fw;
			if (hs) fh -= _SCROLL_BAR_THICKNESS;
			_container.setPrefSize(fw, fh);
			super.layoutChildren();
			_container.layout();
		}
		else if (hs)
		{
			fh -= _SCROLL_BAR_THICKNESS;
			vs = ch > fh;
			if (vs) fw -= _SCROLL_BAR_THICKNESS;
			_container.setPrefSize(fw, fh);
			super.layoutChildren();
			_container.layout();
		}

		if (vs)
		{
			_vScrollBar.setHeight(fh / ch * fh);
			_y = h - (h - fh) - _vScrollBar.getHeight();
			_h = -(h - (h - fh) - ch);
			_container.setLayoutY(Math.max(-_h, Math.min(_container.getLayoutY(), 0)));
			_vScrollBar.setLayoutX(fw);
			_vScrollBar.setLayoutY(-_container.getLayoutY() / _h * _y);
			_vScrollBar.setVisible(true);
		}
		else
		{
			_h = 0;
			_y = 0;
			_vScrollBar.setVisible(false);
			_updateVContent();
		}

		if (hs)
		{
			cw += _margins.getLeft() + _margins.getRight();
			_hScrollBar.setWidth(fw / cw * fw);
			_x = w - (w - fw) - _hScrollBar.getWidth();
			_w = -(w - (w - fw) - cw);
			_container.setLayoutX(Math.max(-_w, Math.min(_container.getLayoutX(), 0)));
			_hScrollBar.setLayoutY(fh);
			_hScrollBar.setLayoutX(-_container.getLayoutX() / _w * _x);
			_hScrollBar.setVisible(true);
		}
		else
		{
			_w = 0;
			_x = 0;
			_hScrollBar.setVisible(false);
			_updateHContent();
		}
	}

	private void _layoutLater()
	{
		if (_resizing) super.requestLayout();
	}

	private void _updateHBar()
	{
		_hScrollBar.setLayoutX(_w > 0 ? -_container.getLayoutX() / _w * _x : 0);
	}


	private void _updateHContent()
	{
		_container.setLayoutX(_x > 0 ? -_hScrollBar.getLayoutX() / _x * _w : 0);
	}


	private void _updateVBar()
	{
		_vScrollBar.setLayoutY(_h > 0 ? -_container.getLayoutY() / _h * _y : 0);
	}


	private void _updateVContent()
	{
		_container.setLayoutY(_y > 0 ? -_vScrollBar.getLayoutY() / _y * _h : 0);
	}
}