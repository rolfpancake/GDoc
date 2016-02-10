package ui.classes;

import java.util.ArrayList;
import data.Argument;
import data.Parameterized;


/**
 * Conteneurs des méthodes et signaux. Il ajoute une liste d'arguments décrits.
 */
class DocParameterized<D extends Parameterized> extends DocPrototyped<D>
{
	static protected final byte V_GAP = 5;
	static protected final byte H_GAP = 10;
	private ArrayList<DocNamed<Argument>> _children;


	DocParameterized(D parameterized)
	{
		super(parameterized); // parameterized != null
		if (parameterized.getSize() == 0) return;
		_children = new ArrayList<DocNamed<Argument>>(parameterized.getSize());
		for (Argument i : parameterized.getArguments()) if (i.description != null)
			_children.add(new DocNamed<Argument>(i));
		for (DocNamed<Argument> i : _children) super.getChildren().addAll(i.getName(), i);
		if (_children.size() == 0) _children = null;
	}


	final Docable[] getArguments()
	{
		return _children != null ? _children.toArray(new Docable[_children.size()]) : null;
	}


	@Override
	public void setHSize(short size)
	{
		super.setHSize(size);
		if (_children == null) return;
		double m = 0;
		double w;

		for (DocNamed<Argument> i : _children)
		{
			w = i.getName().getBoundsInLocal().getWidth();
			if (w > m) m = w;
		}

		m += H_GAP;
		size -= m;

		for (DocNamed<Argument> i : _children)
		{
			super.vSize += V_GAP;
			i.getName().setLayoutY(super.vSize + (i.getBaselineOffset() - i.getName().getBaselineOffset()));
			i.relocate(m, super.vSize);
			i.setHSize(size);
			super.vSize += i.getVSize();
		}
	}


	protected final double getHAlign()
	{
		return _children != null ? _children.get(_children.size() - 1).getLayoutX() : 0;
	}


	protected void setHAlign(double x)
	{
		if (_children != null) for (DocNamed<Argument> i : _children) i.setLayoutX(x);
	}
}