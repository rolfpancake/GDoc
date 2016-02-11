package ui.descriptions;

import org.jetbrains.annotations.Nullable;
import main.Strings;
import ui.VContainer;


/**
 * Classe de base abstraite des paragraphes et des descriptions.
 */
abstract public class TextArea extends VContainer
{
	static private final byte _PREVIEW = 20;
	private String _text;


	protected TextArea(@Nullable String text)
	{
		super();
		_text = text == null ? Strings.EMPTY : text;
		super.init();
	}


	@Override
	public final String toString()
	{
		return _text.length() <= _PREVIEW ? _text : _text.substring(0, _PREVIEW) + Strings.ELLIPSIS;
	}


	abstract public boolean isEmpty();


	@Override
	protected boolean isInitialized() { return _text != null; }
}