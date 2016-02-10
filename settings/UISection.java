package settings;

import org.jetbrains.annotations.NotNull;
import main.Strings;


public final class UISection extends Section
{
	private ShortProperty _currentTab;
	private BooleanProperty _foldDescription;
	private ShortProperty _foldMembers;
	private BooleanProperty _hideUndocumentedArgument;
	private BooleanProperty _hideUndocumentedReturn;
	private BooleanProperty _globalDescriptionFolding;
	private BooleanProperty _globalMemberFolding;
	private Property _openedTabs;


	UISection()
	{
		super("ui");

		_openedTabs = new Property(false, "openedTabs", Strings.EMPTY);
		_currentTab = new ShortProperty(false, "currentTab", true, (short) 0);
		_foldDescription = new BooleanProperty(false, "foldDescription", false);
		_foldMembers = new ShortProperty(false, "foldMembers", true, (short) 2);
		_hideUndocumentedArgument = new BooleanProperty(true, "hideUndocumentedArgument", false);
		_hideUndocumentedReturn = new BooleanProperty(true, "hideUndocumentedReturn", true);
		_globalDescriptionFolding = new BooleanProperty(true, "globalDescriptionFolding", true);
		_globalMemberFolding = new BooleanProperty(true, "globalMemberFolding", true);

		super.addProperty(_openedTabs, _currentTab, _foldDescription, _foldMembers, _hideUndocumentedArgument,
						  _hideUndocumentedReturn, _globalDescriptionFolding, _globalMemberFolding);
	}

	
	@NotNull
	public ShortProperty currentTab() { return _currentTab; }


	@NotNull
	public BooleanProperty foldDescription() { return _foldDescription; }


	@NotNull
	public ShortProperty foldMembers() { return _foldMembers; }


	@NotNull
	public BooleanProperty globalDescriptionFolding() { return _globalDescriptionFolding; }


	@NotNull
	public BooleanProperty globalMemberFolding() { return _globalMemberFolding; }


	@NotNull
	public BooleanProperty hideUndocumentedArgument() { return _hideUndocumentedArgument; }


	@NotNull
	public BooleanProperty hideUndocumentedReturn() { return _hideUndocumentedReturn; }


	@NotNull
	public Property openedTabs() { return _openedTabs; }


	@Override
	protected void validate()
	{
		_foldMembers.setValue((short) Math.max(0, Math.min(_currentTab.toShort(), 2)));
	}
}