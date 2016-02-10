package ui;

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

	private Rectangle _hScrollBar;
	private Rectangle _vScrollBar;
	private Region _content;
	private double _x, _y; // coordonnée maximale d'une barre
	private double _w, _h; // déplacement maximal du contenu
	private double _d;
	private boolean _wheel = false; // éviter un layout pendant le scrolling par molette
	private boolean _scrolling = false; // éviter un layout pendant le scrolling par barre
	private boolean _resizing = false; // éviter un layout en dehors des redimensionnements
	private Rectangle _clip;
	private Insets _margins;

	private ChangeListener<Object> _changeHandler = new ChangeListener<Object>()
	{
		@Override
		public void changed(ObservableValue<?> observable, Object oldValue, Object newValue) { _resizing = true; }
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
			_wheel = true;
			double d = (event.isShiftDown() ? 2 : 1) * event.getDeltaY();

			if (_w > 0 && (event.isControlDown() || _h == 0))
			{
				_content.setLayoutX(Math.max(-_w, Math.min(_content.getLayoutX() + d, _margins.getLeft())));
				_updateHBar();
			}
			else if (_h > 0)
			{
				_content.setLayoutY(Math.max(-_h, Math.min(_content.getLayoutY() + d, _margins.getTop())));
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

		_clip = new Rectangle(100, 100, Color.BLACK);
		super.setClip(_clip);
		super.getChildren().addAll(_hScrollBar, _vScrollBar);

		super.widthProperty().addListener(_changeHandler);
		super.heightProperty().addListener(_changeHandler);

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
		if (child == null || _content == null) return;

		double h = super.getHeight();

		if (h == 0 && super.getParent() != null) super.getParent().layout(); // cadrage après construction

		if (_content.getHeight() == 0)
		{
			super.layoutChildren();
			_content.layout();
		}

		h = super.getHeight();

		double y0 = child.getLayoutY(); // y de child dans _content
		Parent p = child.getParent();

		while (p != null && p != _content)
		{
			y0 += p.getLayoutY();
			p = p.getParent();
			if (p == this) break;
		}

		if (p != _content) return;

		double ch = child.getBoundsInLocal().getHeight();
		double y1 = y0 + ch; // y basse de child dans _content
		double d = (h - ch) / 2;

		_content.setLayoutX(_margins.getLeft());
		_updateHBar();

		if (y0 + _content.getLayoutY() < 0) // y0 dans this
		{
			_content.setLayoutY(Math.max(_margins.getTop(), Math.min(_FRAMING_MARGIN, d)) - y0);
			_updateVBar();
		}
		else if (y1 + _content.getLayoutY() > h) // y1 dans this
		{
			_content.setLayoutY(Math.max(_margins.getTop(), h - Math.min(_FRAMING_MARGIN, d) - ch) - y0);
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
			_content.setLayoutX(_margins.getLeft());
			_hScrollBar.setLayoutX(0);
		}

		if (vertical)
		{
			_content.setLayoutY(_margins.getTop());
			_hScrollBar.setLayoutY(0);
		}
	}


	public final void setContent(Region content)
	{
		if (content == _content)
		{
			if (content.getParent() != this) super.getChildren().add(content);
			return;
		}

		if (_content != null)
		{
			super.getChildren().remove(_content);
			_content.widthProperty().removeListener(_changeHandler);
			_content.heightProperty().removeListener(_changeHandler);
		}

		if (content != null)
		{
			content.relocate(_margins.getLeft(), _margins.getTop());
			super.getChildren().add(0, content);
			content.widthProperty().addListener(_changeHandler);
			content.heightProperty().addListener(_changeHandler);
			//content.layout();
		}

		_content = content;
		_vScrollBar.setLayoutY(0);
		_hScrollBar.setLayoutX(0);
		_resizing = true;
	}


	@Override
	protected void layoutChildren()
	{
		if (_wheel)
		{
			_wheel = false;
			return;
		}

		if (_scrolling || !_resizing) return;

		if (_content == null)
		{
			_resizing = false;
			_x = _y = _w = _h = 0;
			_vScrollBar.setVisible(false);
			_hScrollBar.setVisible(false);
			super.layoutChildren();
			return;
		}

		double w = super.getWidth();
		double h = super.getHeight();
		double fw = w - _margins.getLeft() - _margins.getRight();
		double fh = h - _margins.getTop() - _margins.getBottom();
		_clip.setWidth(w);
		_clip.setHeight(h);
		_content.setPrefSize(fw, fh); // appelle _changeHandler qui modifie _resizing
		super.layoutChildren();

		_content.layout(); // fiabilité des limites
		double cw = _content.getBoundsInLocal().getWidth();
		double ch = _content.getBoundsInLocal().getHeight();
		boolean vs = ch > fh;
		boolean hs = cw > fw;

		if (vs)
		{
			fw -= _SCROLL_BAR_THICKNESS;
			_content.setPrefWidth(fw);

			if (hs) // confirmé
			{
				fh -= _SCROLL_BAR_THICKNESS;
				_content.setPrefHeight(fh);
				super.layoutChildren();
				_content.layout();
				cw = _content.getBoundsInLocal().getWidth();
				ch = _content.getBoundsInLocal().getHeight();
			}
			else
			{
				hs = cw > fw;

				if (hs) // potentiel
				{
					fh -= _SCROLL_BAR_THICKNESS;
					_content.setPrefHeight(fh);
					super.layoutChildren();
					_content.layout();
					cw = _content.getBoundsInLocal().getWidth();
					ch = _content.getBoundsInLocal().getHeight();

					if (cw <= fw) // annulé car le contenu peut être réduit en largeur
					{
						fh += _SCROLL_BAR_THICKNESS;
						_content.setPrefHeight(fh);
						super.layoutChildren();
						_content.layout();
						cw = _content.getBoundsInLocal().getWidth();
						ch = _content.getBoundsInLocal().getHeight();
						hs = false;
					}
				}
				else // infirmé
				{
					super.layoutChildren();
					_content.layout();
					cw = _content.getBoundsInLocal().getWidth();
				}
			}
		}
		else if (hs)
		{
			fh -= _SCROLL_BAR_THICKNESS;
			_content.setPrefHeight(fh);
			vs = ch > fh;

			if (vs) // potentiel
			{
				fw -= _SCROLL_BAR_THICKNESS;
				_content.setPrefWidth(fw);
				super.layoutChildren();
				_content.layout();
				cw = _content.getBoundsInLocal().getWidth();
				ch = _content.getBoundsInLocal().getHeight();

				if (cw <= fw) // annulé car le contenu peut être réduit en hauteur
				{
					fw += _SCROLL_BAR_THICKNESS;
					_content.setPrefWidth(fw);
					super.layoutChildren();
					_content.layout();
					cw = _content.getBoundsInLocal().getWidth();
					ch = _content.getBoundsInLocal().getHeight();
					vs = false;
				}
			}
			else // infirmé
			{
				super.layoutChildren();
				_content.layout();
				ch = _content.getBoundsInLocal().getHeight();
			}
		}

		_resizing = false;

		if (vs)
		{
			_vScrollBar.setHeight(fh / ch * h);
			_y = h - (hs ? _SCROLL_BAR_THICKNESS : 0) - _vScrollBar.getHeight();
			_h = -(_margins.getTop() + fh - ch);
			_content.setLayoutY(Math.max(-_h, Math.min(_content.getLayoutY(), _margins.getTop())));
			_vScrollBar.relocate(w - _SCROLL_BAR_THICKNESS, -_content.getLayoutY() / _h * _y);
			_vScrollBar.setVisible(true);
		}
		else
		{
			_h = _margins.getTop();
			_y = 0;
			_vScrollBar.setVisible(false);
			_updateVContent();
		}

		if (hs)
		{
			_hScrollBar.setWidth(fw / cw * w);
			_x = w - (vs ? _SCROLL_BAR_THICKNESS : 0) - _hScrollBar.getWidth();
			_w = -(_margins.getLeft() + fw - cw);
			_content.setLayoutX(Math.max(-_w, Math.min(_content.getLayoutX(), _margins.getLeft())));
			_hScrollBar.relocate(-_content.getLayoutX() / _w * _x, h - _SCROLL_BAR_THICKNESS);
			_hScrollBar.setVisible(true);
		}
		else
		{
			_w = _margins.getLeft();
			_x = 0;
			_hScrollBar.setVisible(false);
			_updateHContent();
		}
	}


	private void _updateHBar()
	{
		_hScrollBar.setLayoutX(_w > 0 ? -_content.getLayoutX() / _w * _x : 0);
	}


	private void _updateHContent()
	{
		_content.setLayoutX(_x > 0 ? _margins.getLeft() - _hScrollBar.getLayoutX() / _x * _w : _margins.getLeft());
	}


	private void _updateVBar()
	{
		_vScrollBar.setLayoutY(_h > 0 ? -_content.getLayoutY() / _h * _y : 0);
	}


	private void _updateVContent()
	{
		_content.setLayoutY(_y > 0 ? _margins.getTop() - _vScrollBar.getLayoutY() / _y * _h : _margins.getTop());
	}
}