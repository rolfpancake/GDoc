package ui.descriptions;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import javafx.geometry.Bounds;
import javafx.scene.shape.Shape;
import data.Type;
import ui.Graphics;


/**
 * Une description de méthode, membre, constante, signal ou thème. Elle est ellipsée.
 */
public final class MemberDescription extends FullDescription
{
	private Shape _ellipsis;


	public MemberDescription(@Nullable Type host, @NotNull String description)
	{
		super(host, description);
	}


	@Override
	protected short computeHeight()
	{
		Stub s = super.getStub();
		if (s == null) return 0;

		if (super.isFolded() && s.isTruncated()) // ellipse visible
		{
			if (_ellipsis.getParent() == null) super.getChildren().add(_ellipsis);
			Bounds b = _ellipsis.getBoundsInLocal();

			// la largeur du talon n'est modifiée que si l'ellipse dépasse
			if (s.getXOffset() + Graphics.PADDING + b.getWidth() > super.getWidth())
				s.setWidth((short) (super.getWidth() - Graphics.PADDING - b.getWidth()));

			_ellipsis.relocate(s.getXOffset() + Graphics.PADDING, s.getYOffset() - b.getHeight() + Graphics.THIN);
			return s.getHeight();
		}
		else // ellipse non visible
		{
			super.getChildren().remove(_ellipsis);
		}

		return super.computeHeight();
	}


	@Override
	protected void initDisplay()
	{
		super.initDisplay();
		_ellipsis = Graphics.DRAW_ELLIPSIS();
	}
}