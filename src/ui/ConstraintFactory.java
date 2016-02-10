package ui;


import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.RowConstraints;


abstract public class ConstraintFactory
{
	static public ColumnConstraints GET_AUTO_FIXED_COLUMN()
	{
		return new ColumnConstraints(Region.USE_COMPUTED_SIZE,
									 Region.USE_COMPUTED_SIZE,
									 Region.USE_COMPUTED_SIZE,
									 Priority.NEVER, null, true);
	}


	static public RowConstraints GET_AUTO_FIXED_ROW()
	{
		return new RowConstraints(Region.USE_COMPUTED_SIZE,
								  Region.USE_COMPUTED_SIZE,
								  Region.USE_COMPUTED_SIZE,
								  Priority.NEVER, null, true);
	}


	static public ColumnConstraints GET_AUTO_GROWING_COLUMN()
	{
		return new ColumnConstraints(Region.USE_COMPUTED_SIZE,
									 Region.USE_COMPUTED_SIZE,
									 Region.USE_COMPUTED_SIZE,
									 Priority.ALWAYS, null, true);
	}


	static public RowConstraints GET_AUTO_GROWING_ROW()
	{
		return new RowConstraints(Region.USE_COMPUTED_SIZE,
								  Region.USE_COMPUTED_SIZE,
								  Region.USE_COMPUTED_SIZE,
								  Priority.ALWAYS, null, true);
	}


	static public ColumnConstraints GET_FIXED_COLUMN(short width)
	{
		return new ColumnConstraints(width, width, width, Priority.NEVER, null, false);
	}


	static public RowConstraints GET_FIXED_ROW(short height)
	{
		return new RowConstraints(height, height, height, Priority.NEVER, null, false);
	}
}