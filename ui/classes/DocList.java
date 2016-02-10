package ui.classes;

import java.util.ArrayList;
import org.jetbrains.annotations.Nullable;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import data.Constant;
import data.Group;
import data.GroupType;
import data.Identifier;
import data.Member;
import data.Method;
import data.Signal;
import data.ThemeItem;
import ui.UIType;
import ui.Verticable;


final class DocList extends Verticable
{
	static private final byte _H_GAP = 10;
	static private final byte _V_GAP = 8;

	private ArrayList<DocPrototyped> _docables;
	private GroupType _type;

	/* le prototype et le retour/type doivent être centrés, et la description et le prototype doivent être dans le
	   même conteneur pour être cadrés, donc un GridPane ne peut pas être utilisé */

	DocList(Group group)
	{
		super();
		if (group == null || group.getSize() == 0) return;
		_type = group.getType();
		Identifier[] l = group.getIdentifiers();
		if (l == null) return;

		_docables = new ArrayList<DocPrototyped>(l.length);

		if (_type == GroupType.methods) _fromMethods(l);
		else if (_type == GroupType.signals) _fromSignals(l);
		else _fromTyped(l);

		layoutChildren();
	}


	@Override
	public String toString()
	{
		return super.getClass().getSimpleName() + ":" + _type.toString();
	}


	@Nullable
	DocPrototyped getPrototyped(String name)
	{
		String s;

		for (DocPrototyped i : _docables)
		{
			s = i.getData().getName();
			if (s != null && s.equals(name)) return i;
		}

		return null;
	}


	@Override
	public void setHSize(short size)
	{
		if (_docables == null) return;

		size -= _docables.get(0).getLayoutX();
		super.vSize = -_V_GAP;
		double y;
		UIType t;

		for (DocPrototyped i : _docables)
		{
			super.vSize += _V_GAP;
			i.setLayoutY(super.vSize);
			t = null;

			if (_type == GroupType.methods) t = ((DocMethod) i).getReturn();
			else if (_type != GroupType.signals) t = ((DocTyped) i).getType();

			if (t != null)
				t.setLayoutY(super.vSize +
							 (DocPrototyped.PROTOTYPE_ROW_HEIGHT - t.getBoundsInLocal().getHeight()) / 2);

			i.setHSize(size);
			super.vSize += i.getVSize();
		}
	}


	private void _fromMethods(Identifier[] l)
	{
		ObservableList<Node> c = super.getChildren();
		DocMethod o;
		UIType t;
		String n, s;
		double w;
		short k;
		double x = 0;

		for (Identifier i : l)
		{
			o = new DocMethod<Method>((Method) i);
			n = o.getData().getName();
			if (n == null) continue;
			k = 0;

			for (DocPrototyped j : _docables) // tri alphabétique
			{
				s = j.getData().getName();
				if (s != null && n.compareTo(s) < 0) break;
				k += 1;
			}

			t = o.getReturn();
			_docables.add(k, o);
			w = t.getBoundsInLocal().getWidth();
			if (w > x) x = (short) w;
			c.addAll(t, o);
		}

		x += _H_GAP;
		for (DocPrototyped j : _docables) j.setLayoutX(x);
	}


	private void _fromSignals(Identifier[] l)
	{
		ObservableList<Node> c = super.getChildren();
		DocParameterized o;
		String n, s;
		short k;

		for (Identifier i : l)
		{
			o = new DocParameterized<Signal>((Signal) i);
			n = o.getData().getName();
			if (n == null) continue;
			k = 0;

			for (DocPrototyped j : _docables) // tri alphabétique
			{
				s = j.getData().getName();
				if (s != null && n.compareTo(s) < 0) break;
				k += 1;
			}

			_docables.add(k, o);
			c.add(o);
		}
	}


	private void _fromTyped(Identifier[] l)
	{
		ObservableList<Node> c = super.getChildren();
		DocTyped o;
		UIType t;
		String n, s;
		double w;
		short k;
		double x = 0;

		for (Identifier i : l)
		{
			if (_type == GroupType.members) o = new DocTyped<Member>((Member) i);
			else if (_type == GroupType.constants) o = new DocTyped<Constant>((Constant) i);
			else if (_type == GroupType.theme_items) o = new DocTyped<ThemeItem>((ThemeItem) i);
			else continue;
			n = o.getData().getName();
			if (n == null) continue;
			k = 0;

			for (DocPrototyped j : _docables) // tri alphabétique
			{
				s = j.getData().getName();
				if (s != null && n.compareTo(s) < 0) break;
				k += 1;
			}

			_docables.add(k, o);
			t = o.getType();
			w = t.getBoundsInLocal().getWidth();
			if (w > x) x = (short) w;
			c.addAll(t, o);
		}

		x += _H_GAP;
		for (DocPrototyped j : _docables) j.setLayoutX(x);
	}
}