package main;

import org.jetbrains.annotations.Nullable;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.control.CheckBox;
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
		_hide.setSelected(Launcher.SETTINGS.log().hide().toBoolean());

		_hide.selectedProperty().addListener(new ChangeListener<Boolean>()
		{
			@Override
			public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue)
			{
				Launcher.SETTINGS.log().hide().setValue(newValue);
				Launcher.SETTINGS.save();
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
			if ((_hide.isSelected() && _lines > 1) || Launcher.SETTINGS.log().hide().toBoolean())
				return;

			super.show();
		}

		super.frame.resetScrolling(true, false);
		super.toFront();
	}
}