package ui.descriptions;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import javafx.scene.Node;
import data.Type;


/**
 * Un paragraphe d'une seule ligne sans largeur maximale.
 */
public final class Line extends Paragraph
{
	public Line(@Nullable Type host, @NotNull String text)
	{
		super(host, text);
	}


	@Override
	protected short computeHeight()
	{
		return super.isEmpty() ? 0 : Paragraph.REGULAR_HEIGHT;
	}


	@Override
	protected void initLayout()
	{
		if (super.isEmpty()) return;

		double x = 0;
		Node p = null;

		for (Node i : super.blocks)
		{
			if (i == null)
			{
				x += H_GAP;
				p = null;
				continue;
			}

			if (x > 0)
			{
				if (p != null) x += Paragraph.STICKING_PADDING;
				if (i instanceof CodeBlock)
					i.relocate(x, (Paragraph.REGULAR_HEIGHT - i.getBoundsInLocal().getHeight()) / 2);
				else i.relocate(x, 0);
			}

			x += i.getBoundsInLocal().getWidth();
			p = i;
		}

		super.initLayout();
	}
}