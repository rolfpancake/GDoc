package ui.filters;

import org.jetbrains.annotations.Nullable;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.Region;
import ui.Graphics;

//TODO pourcentages

public final class FilterBar extends Region
{
	static private Background _BACKGROUND = new Background(new BackgroundFill(Graphics.GRAY_22,
																			  new CornerRadii(FilterButton.CORNER + Graphics.DOUBLE),
																			  Insets.EMPTY));

	private FilterGroup _left;
	private FilterGroup _center;
	private FilterGroup _right;


	public FilterBar(@Nullable FilterGroup left, @Nullable FilterGroup center, @Nullable FilterGroup right)
	{
		super();
		super.setMinHeight(FilterGroup.MIN_HEIGHT);
		super.setBackground(_BACKGROUND);

		_left = left;
		_center = center;
		_right = right;

		if (left != null)
		{
			left.setPosition(center != null ? HPos.LEFT : null);
			super.getChildren().add(left);
		}

		if (right != null)
		{
			right.setPosition(center != null ? HPos.RIGHT : null);
			super.getChildren().add(right);
		}

		if (center != null)
		{
			if (_left != null) center.setPosition(right != null ? HPos.CENTER : HPos.RIGHT);
			else center.setPosition(right != null ? HPos.LEFT : null);
			super.getChildren().add(center);
			center.toBack();
		}
	}


	@Nullable
	public FilterGroup getCenter() { return _center; }


	@Nullable
	public FilterGroup getLeft() { return _left; }


	@Nullable
	public FilterGroup getRight() { return _right; }


	@Override
	protected void layoutChildren()
	{
		super.layoutChildren();
		byte t = 2 * Graphics.DOUBLE + Graphics.THIN;

		if (_left != null)
		{
			_left.setPrefHeight(super.getHeight());

			if (_right != null)
			{
				_right.setLayoutX(super.getWidth() - _right.getWidth());
				_right.setPrefHeight(super.getHeight());

				if (_center != null)
				{
					_center.setLayoutX(_left.getWidth() - t);
					_center.setPrefSize(_right.getLayoutX() - _center.getLayoutX() + t, super.getHeight());
				}
			}
			else if (_center != null)
			{
				_center.setLayoutX(_left.getWidth() - t);
				_center.setPrefSize(super.getWidth() - _center.getLayoutX(), super.getHeight());
			}
		}
		else if (_right != null)
		{
			_right.setLayoutX(super.getWidth() - _right.getWidth());
			_right.setPrefHeight(super.getHeight());
			if (_center != null) _center.setPrefSize(_right.getLayoutX() + t, super.getHeight());
		}
		else if (_center != null)
		{
			_center.setPrefSize(super.getWidth(), super.getHeight());
		}

		super.layoutChildren();
	}
}