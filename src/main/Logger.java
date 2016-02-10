package main;

import org.jetbrains.annotations.Nullable;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.control.CheckBox;
import settings.Property;
import settings.Settings;
import window.ErrorWindow;


public final class Logger extends ErrorWindow
{
	static private final String _TITLE = "Log (";

	private short _lines = 0;
	private CheckBox _hide;


	Logger()
	{
		super(null, null, (short) 450, (short) 250, _TITLE + "0)", null);
		super.setResizable(true);
		_hide = new CheckBox("Not show again");
		super.setLeftAnchor(_hide);
		Property p = Settings.INSTANCE.getSection(Strings.LOG).getProperty(Strings.HIDE);
		p.setPersistent(true);
		p.setDefault(true);
		_hide.setSelected(p.isTrue());

		_hide.selectedProperty().addListener(new ChangeListener<Boolean>()
		{
			@Override
			public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue)
			{
				Settings.INSTANCE.getSection(Strings.LOG).getProperty(Strings.HIDE).setValue(newValue);
				Settings.INSTANCE.save();
			}
		});
	}


	/**
	 * Ajoute une ligne au journal de session et affiche la fenêtre.
	 */
	void append(@Nullable String message)
	{
		if (message == null || message.isEmpty()) return;

		String s = super.content.getText();
		if (s != null) s += s.isEmpty() ? "" : Strings.NEW_LINE;
		super.content.setText(s + Strings.DASH + Strings.SPACE + message);
		_lines += 1;
		super.setTitle(_TITLE + _lines + ")");

		if (!super.isShowing())
		{
			if ((_hide.isSelected() && _lines > 1) ||
				Settings.INSTANCE.getSection(Strings.LOG).getProperty(Strings.HIDE).isTrue())
				return;

			super.show();
		}

		super.frame.resetScrolling(true, false);
		super.toFront();
	}
}