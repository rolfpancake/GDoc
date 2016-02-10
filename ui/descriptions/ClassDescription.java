package ui.descriptions;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import javafx.event.EventHandler;
import javafx.geometry.Bounds;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import data.Type;
import events.UIEvent;
import main.Launcher;
import settings.BooleanProperty;
import ui.Graphics;


/**
 * Description de classe. Elle peut être pliée manuellement par un bouton, ou par le code.
 */
public final class ClassDescription extends FullDescription
{
	static private final BooleanProperty _FOLD_DESCRIPTION = Launcher.SETTINGS.ui().foldDescription();
	static private final boolean _GLOBAL_DESCRIPTION_FOLDING = Launcher.SETTINGS.ui().globalDescriptionFolding().toBoolean();

	private Folder _folder;

	private EventHandler<MouseEvent> _clickHandler = new EventHandler<MouseEvent>()
	{
		@Override
		public void handle(MouseEvent event) { _onClick(event); }
	};


	public ClassDescription(@Nullable Type host, @NotNull String description)
	{
		super(host, description);
	}


	@Override
	public void fold(boolean value)
	{
		_folder.toggle(value);
		Stub s = super.getStub();
		if (s == null) return;

		if (value) s.setWidth((short) (super.getWidth() - _folder.getBoundsInLocal().getWidth() - Graphics.PADDING));
		else s.setWidth(super.getWidth());

		super.fold(value);
	}


	@Override
	protected short computeHeight()
	{
		Stub s = super.getStub();
		if (s == null) return 0;

		Bounds b = _folder.getBoundsInLocal();
		short h;

		if (super.isFolded()) // pliée (positionnement au premier paragraphe)
		{
			if (s.isTruncated() || super.paragraphs != null) // premier paragraphe coupé
			{
				if (_folder.getParent() == null) super.getChildren().add(_folder);

				if (s.getXOffset() + Graphics.PADDING + b.getWidth() > super.getWidth())
					s.setWidth((short) Math.max(0, super.getWidth() - b.getWidth() - Graphics.PADDING));

				_folder.toggle(true); // si pliage basculé par une autre classe
				_folder.relocate(s.getXOffset() + Graphics.PADDING,
								 s.getLayoutY() + s.getYOffset() - _folder.getBaselineOffset());
			}
			else // premier paragraphe court
			{
				super.getChildren().remove(_folder);
			}

			return s.getHeight();
		}
		else if (super.paragraphs != null) // n paragraphes (positionnement au dernier paragraphe)
		{
			if (_folder.getParent() == null) super.getChildren().add(_folder);
			h = super.computeHeight();
			Paragraph p = super.paragraphs.get(super.paragraphs.size() - 1);

			/* icône sous le dernier paragraphe */
			if (p.getXOffset() + Graphics.PADDING + _folder.getBoundsInLocal().getWidth() > super.getWidth())
			{
				h += Graphics.PADDING;
				_folder.relocate(0, h);
				return (short) (h + b.getHeight());
			}

			_folder.relocate(p.getXOffset() + Graphics.PADDING,
							 p.getLayoutY() + p.getYOffset() - _folder.getBaselineOffset());

			return h;
		}
		else if (!s.isMonoLine()) // 1 paragraphe long (positionnement au premier paragraphe)
		{
			if (_folder.getParent() == null) super.getChildren().add(_folder);

			// icône sous le premier paragraphe
			if (s.getXOffset() + Graphics.PADDING + b.getWidth() > super.getWidth())
			{
				h = (short) (s.getHeight() + Graphics.PADDING);
				_folder.relocate(0, h);
				return (short) (h + b.getHeight());
			}

			_folder.relocate(s.getXOffset() + Graphics.PADDING,
							 s.getLayoutY() + s.getYOffset() - _folder.getBaselineOffset());

			return s.getHeight();
		}
		else // plieur non visible
		{
			super.getChildren().remove(_folder);
		}

		return super.computeHeight();
	}


	@Override
	protected void initDisplay()
	{
		super.initDisplay();
		_folder = new Folder();

		_folder.addEventHandler(MouseEvent.MOUSE_CLICKED, new EventHandler<MouseEvent>()
		{
			@Override
			public void handle(MouseEvent event) { _onClick(event); }
		});
	}


	private void _onClick(MouseEvent e) // clic sur le plieur
	{
		e.consume();
		if (e.getButton() != MouseButton.PRIMARY || !Launcher.KEYBOARD.isEmpty()) return;

		if (_GLOBAL_DESCRIPTION_FOLDING) _FOLD_DESCRIPTION.setValue(_folder.toggle());
		this.fold(_folder.isToggled()); // super ignore la redéfinition

		super.fireEvent(new UIEvent(UIEvent.FOLDING));
	}
}