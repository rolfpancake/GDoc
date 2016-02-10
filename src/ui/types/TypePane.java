package ui.types;

import java.util.ArrayList;
import java.util.Arrays;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import data.Documentation;
import data.Type;
import events.ChangeEvent;
import events.SelectionEvent;
import ui.ConstraintFactory;
import ui.Frame;


public final class TypePane extends GridPane implements EventHandler<SelectionEvent>
{
	private ArrayList<SubList> _lists;
	private Frame _frame;
	private SubList _current;
	private ListTabBar _tabBar;
	private boolean _layout;



	public TypePane()
	{
		super();

		super.setMinWidth(60);
		_lists = new ArrayList<SubList>(ListType.values().length);

		ArrayList<Type> l = new ArrayList<Type>(Arrays.asList(Documentation.INSTANCE.getTypes()));
		_lists.add(new SubList(ListType.ALL, l)); // l non modifiée
		_lists.add(new SubList(ListType.BUILT_IN, l)); // l modifiée
		_lists.add(new SubList(ListType.RESOURCE, l));
		_lists.add(new SubList(ListType.CONTROL, l));
		_lists.add(new SubList(ListType.NODE2D, l));
		_lists.add(new SubList(ListType.SPATIAL, l));
		_lists.add(2, new SubList(ListType.ROOT, l)); // l non modifiée

		_frame = new Frame(new Insets(10, 4, 30, 8));
		_tabBar = new ListTabBar();

		super.setHgap(-ListTab.RIGHT_MARGIN);
		ColumnConstraints c0 = ConstraintFactory.GET_FIXED_COLUMN(ListTab.TAB_WIDTH);
		ColumnConstraints c1 = ConstraintFactory.GET_AUTO_GROWING_COLUMN();
		super.getColumnConstraints().addAll(c0, c1);
		super.add(_tabBar, 0, 0);
		super.add(_frame, 1, 0);

		ChangeListener<Number> h = new ChangeListener<Number>()
		{
			@Override
			public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue)
			{
				_layout = true;
			}
		};

		super.widthProperty().addListener(h);
		super.heightProperty().addListener(h);
		_tabBar.addEventHandler(SelectionEvent.SELECTED, this); // sur l'occurrence l'événement n'est pas consommé
		_current = _lists.get(0);
		_frame.setContent(_current);

		super.addEventHandler(ChangeEvent.CHANGE, new EventHandler<ChangeEvent>() // filtrage
		{
			@Override
			public void handle(ChangeEvent event)
			{
				_frame.resetScrolling(true, true);
			}
		});
	}


	@Override
	public void handle(SelectionEvent event)
	{
		if (!(event.getTarget() instanceof ListTab)) return;
		event.consume();
		ListType t = ((ListTab) event.getTarget()).getType();
		if (t == _current.getType()) return;

		_current = getList(t);
		_current.reset();
		_layout = true;
		_frame.setContent(_current);
	}


	public void fitWidth()
	{
		_layout = true;
		_frame.fitWidth();
	}


	public SubList getCurrent() { return _current; }


	public ListTabBar getTabBar() { return _tabBar; }


	SubList getList(ListType type)
	{
		if (type == null) return null;
		for (SubList i : _lists) if (i.getType() == type) return i;
		return null;
	}


	@Override
	protected void layoutChildren()
	{
		if (!_layout) return;
		_layout = false;
		_tabBar.setPrefHeight(super.getHeight());
		_frame.setLayoutX(ListTab.TAB_WIDTH - ListTab.RIGHT_MARGIN);
		_frame.setPrefWidth(super.getWidth() - _frame.getLayoutX());
		_frame.setPrefHeight(super.getHeight());
		super.layoutChildren();
	}
}