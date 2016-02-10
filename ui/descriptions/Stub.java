package ui.descriptions;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import javafx.geometry.Bounds;
import javafx.scene.Node;
import javafx.scene.text.Text;
import data.Type;


/**
 * Premier paragraphe d'une description. Il peut être tronqué.
 */
final class Stub extends Paragraph
{
	private boolean _truncable;
	private short _last; // indice du dernier bloc visible (-1 si vide sinon >= 0)
	private boolean _monoLine = true;


	Stub(@Nullable Type host, @NotNull String text)
	{
		super(host, text);
	}


	/**
	 * Récupére l'abscisse droite du dernier mot visible.
	 */
	@Override
	short getXOffset()
	{
		if (super.isEmpty() || (_last == 0 && super.blocks[0] == null)) return 0;
		return (short) super.blocks[_last].getBoundsInParent().getMaxX();
	}


	/**
	 * Récupére l'ordonnée de la ligne de base du dernier mot visible.
	 */
	@Override
	short getYOffset()
	{
		if (super.isEmpty()) return 0;
		Node b = super.blocks[_last];
		return (short) (b.getLayoutY() + b.getBaselineOffset());
	}


	/**
	 * Détermine si tous les mots visibles tiennent sur une seule ligne.
	 */
	@Override
	boolean isMonoLine()
	{
		if (_truncable || _last < 1) return true;
		Node b = super.blocks[_last];
		if (b instanceof Text) return b.getLayoutY() == 0;
		return Math.abs(b.getLayoutY()) -
			   Math.abs((Paragraph.REGULAR_HEIGHT - b.getBoundsInLocal().getHeight()) / 2) < 2;
	}


	/**
	 * Détermine si au moins un mot n'est pas affiché.
	 */
	boolean isTruncated() { return !super.isEmpty() && _last < super.blocks.length - 1; }


	/**
	 * Détermine si le texte peut être tronqué. Un texte tronqué ne contient qu'une seule ligne et au minimum un
	 * mot. La troncation opére par mot entier.
	 */
	void truncate(boolean value)
	{
		if (super.isEmpty() || value == _truncable) return;

		if (!value)
		{
			short n = (short) super.blocks.length;
			Node b;

			if (_last < n - 1)
			{
				for (short j = (short) (_last + 1); j < n; ++j)
				{
					b = super.blocks[j];
					if (b != null && b.getParent() == null) super.getChildren().add(b);
				}
			}

			_last = (short) (n - 1);
		}

		_truncable = value;
		super.requestHeightComputing();
	}


	@Override
	protected short computeHeight()
	{
		if (!_truncable) return super.computeHeight();

		double x = 0;
		short m = (short) Math.max(0, super.getWidth());
		Node p = null;
		short n = 0;
		double r, w;
		Bounds b;
		_last = -1;

		for (Node i : super.blocks)
		{
			_last += 1;

			if (i == null)
			{
				x += Paragraph.H_GAP;
				p = null;
				continue;
			}

			b = i.getBoundsInLocal();
			w = b.getWidth();

			if (p != null) x += Paragraph.STICKING_PADDING;
			r = x + w;

			if (r > m)
			{
				if (n == 0)
				{
					super.getChildren().removeAll(super.blocks);
				}
				else
				{
					_last -= 1;
					while (_last > 0 && super.blocks[_last] == null) _last -= 1; // espaces

					for (short k = (short) (_last + 1); k < super.blocks.length; ++k)
						super.getChildren().remove(super.blocks[k]);
				}

				break;
			}

			if (i.getParent() == null) super.getChildren().add(i);
			if (i instanceof CodeBlock) i.relocate(x, (REGULAR_HEIGHT - b.getHeight()) / 2);
			else i.relocate(x, 0);

			x = r;
			n += 1;
			p = i;
		}

		return (short) super.blocks[_last].getBoundsInParent().getMaxY();
	}


	@Override
	protected void initLayout()
	{
		super.initLayout();
		_last = super.isEmpty() ? -1 : (short) (super.blocks.length - 1);
	}
}