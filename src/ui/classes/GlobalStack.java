package ui.classes;

import data.Documentation;
import data.Type;
import main.Launcher;
import ui.header.MenuType;


final class GlobalStack extends StackFrame
{
	private UIClass _gdScript;
	private UIClass _globalScope;
	private TypeBriefs _briefs;


	GlobalStack() { super(); }


	void setCurrent(MenuType type)
	{
		if (type == MenuType.LIST)
		{
			if (_briefs == null)
			{
				_briefs = new TypeBriefs();
				Launcher.INSTANCE.updateMemoryUsage();
			}

			super.setContent(_briefs);
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