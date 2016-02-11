package ui.content;

import data.Documentation;
import data.Type;
import main.Launcher;
import ui.header.MenuType;


final class GlobalStack extends StackFrame
{
	private UIClass _gdScript;
	private UIClass _globalScope;
	private ClassSummary _summary;


	GlobalStack() { super(); }


	void setCurrent(MenuType type)
	{
		if (type == MenuType.LIST)
		{
			if (_summary == null)
			{
				_summary = new ClassSummary();
				Launcher.INSTANCE.updateMemoryUsage();
			}

			super.setContent(_summary);
		}
		else if (type == MenuType.SCRIPT)
		{
			if (_gdScript == null)
			{
				_gdScript = new UIClass(Type.GD_SCRIPT, Documentation.INSTANCE.getTrunk(Type.GD_SCRIPT));
				Launcher.INSTANCE.updateMemoryUsage();
			}

			super.setContent(_gdScript);
		}
		else if (type == MenuType.GLOBAL)
		{
			if (_globalScope == null)
			{
				_globalScope = new UIClass(Type.GLOBAL_SCOPE, Documentation.INSTANCE.getTrunk(Type.GLOBAL_SCOPE));
				Launcher.INSTANCE.updateMemoryUsage();
			}

			super.setContent(_globalScope);
		}
	}
}