package ui.content;

import org.jetbrains.annotations.NotNull;
import data.Described;
import data.Documentation;
import ui.VContainer;
import ui.descriptions.EmptyDescription;
import ui.descriptions.MemberDescription;
import ui.descriptions.TextArea;


/**
 * Classe de base abstraite des conteneurs de documentation.
 */
abstract class Docable<D extends Described> extends VContainer
{
	private D _data;
	private DisplayMode _displayMode;
	private TextArea _description;
	private boolean _opened = false;


	protected Docable(@NotNull D described)
	{
		super();
		_data = described;
		_displayMode = DisplayMode.MULTILINE;
		super.init();
	}


	@Override
	public void setWidth(short width)
	{
		_description.setWidth((short) (width - _description.getLayoutX()));
		super.setWidth(width);
	}


	@NotNull
	final D getData() { return _data; }


	@NotNull
	final DisplayMode getDisplayMode() { return _displayMode; }


	short getMinLeftOffset()
	{
		return 0;
	}


	/**
	 * Affiche toute la documentation quelque soit le mode d'affichage.
	 */
	void open(boolean value)
	{
		if (value == _opened) return;
		_opened = value;

		if (_displayMode != DisplayMode.MULTILINE)
		{
			if (value)
			{
				if (_displayMode == DisplayMode.EMPTY && _description.getParent() == null)
					super.getChildren().add(_description);

				if (_description instanceof MemberDescription) ((MemberDescription) _description).fold(false);
			}
			else if (_displayMode == DisplayMode.EMPTY)
			{
				super.getChildren().remove(_description);
			}
			else if (_description instanceof MemberDescription)
			{
				((MemberDescription) _description).fold(_displayMode == DisplayMode.MONOLINE);
			}
		}

		super.requestHeightComputing();
	}


	final boolean isOpened() { return _opened; }


	void setDisplayMode(@NotNull DisplayMode mode)
	{
		if (mode == _displayMode) return;
		_displayMode = mode;
		if (_opened) return;

		if (mode == DisplayMode.EMPTY)
		{
			super.getChildren().remove(_description);
		}
		else
		{
			if (_description.getParent() == null) super.getChildren().add(_description);

			if (_description instanceof MemberDescription)
				((MemberDescription) _description).fold(mode == DisplayMode.MONOLINE);
		}

		super.requestHeightComputing();
	}


	void setLeftOffset(short offset)
	{
		_description.setLayoutX(offset);
	}


	@Override
	protected short computeHeight()
	{
		return _displayMode == DisplayMode.EMPTY && !_opened ? 0 : _description.getHeight();
	}


	@NotNull
	protected final TextArea getDescription() { return _description; }


	@Override
	protected final boolean isInitialized() { return _data != null; }


	@Override
	protected void initDisplay()
	{
		if (_data.description != null)
		{
			_description = new MemberDescription(Documentation.INSTANCE.getDataType(_data), _data.description);
			if (_description.isEmpty()) _description = new EmptyDescription();
		}
		else
		{
			_description = new EmptyDescription();
		}

		super.getChildren().add(_description);
	}


	@Override
	protected void layoutChildren()
	{
		super.layoutChildren();
		_description.layout();
	}
}