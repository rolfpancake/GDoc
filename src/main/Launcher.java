package main;

import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import events.KeyboardManager;
import exceptions.ErrorMessage;
import settings.Property;
import settings.Section;
import settings.Settings;
import ui.Graphics;
import ui.UI;
import window.ErrorWindow;


public final class Launcher
{
	static private final short _MIN_WIDTH = 450;
	static private final short _MIN_HEIGHT = 300;
	static private final short _DEFAULT_WIDTH = 900;
	static private final short _DEFAULT_HEIGHT = 550;

	static private final String _TITLE_1 = "GDoc " + Main.VERSION + " | ";
	static private final String _TITLE_2 = " KB | ";
	static private final String _TITLE_3 = " KB (";
	static private final String _TITLE_4 = "%)";
	static private final String _TITLE_ERROR = "Error";
	static private final short _GC_THRESHOLD = (short) 35000; // KB

	static public final Launcher INSTANCE = new Launcher();

	private Stage _window;
	private Logger _logger;
	private Runtime _runtime;


	private Launcher()
	{
		Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler()
		{
			@Override
			public void uncaughtException(Thread t, Throwable e)
			{
				raise(e);
			}
		});

		_runtime = Runtime.getRuntime();
	}


	public void log(String message)
	{
		if (message == null || message.isEmpty()) return;

		if (_logger == null)
		{
			_logger = new Logger();
			KeyboardManager.GET_MANAGER(_logger);
		}

		_logger.append(message);
	}


	public void log(ErrorMessage message)
	{
		if (message != null) log(message.getMessage());
	}


	public void log(Throwable exception)
	{
		log(new ErrorMessage(exception, false));
	}


	/**
	 * Affiche un message d'erreur et ferme l'application.
	 */
	public void raise(Throwable exception)
	{
		if (Platform.isFxApplicationThread())
		{
			raise(new ErrorMessage(exception, true));
		}
		else
		{
			System.out.println("UncaughtException, current thread: " + Thread.currentThread().getName());
			exception.printStackTrace(); // runLater() ne fonctionne pas
			System.exit(1);
		}
	}


	/**
	 * Affiche un message d'erreur et ferme l'application.
	 */
	public void raise(ErrorMessage message)
	{
		if (message == null) return;

		ErrorWindow w = new ErrorWindow(_window, Modality.APPLICATION_MODAL, (short) 350, (short) 200,
										_TITLE_ERROR, message.getMessage());
		KeyboardManager.GET_MANAGER(w);

		w.addEventHandler(WindowEvent.WINDOW_HIDDEN, new EventHandler<WindowEvent>()
		{
			@Override
			public void handle(WindowEvent e) { System.exit(1); }
		});

		w.showAndWait();
	}


	public void updateMemoryUsage()
	{
		int t = Math.round(_runtime.totalMemory() / 1024);
		int f = Math.round(_runtime.freeMemory() / 1024);
		double m = t - f;
		if (m > _GC_THRESHOLD) System.gc();
		_window.setTitle(_TITLE_1 + t + _TITLE_2 + Math.round(m) + _TITLE_3 + Math.round(m / t * 100) + _TITLE_4);
	}


	void start(Stage stage)
	{
		if (_window != null) return;
		_window = stage;
		_initSettings();
		_initHandlers();
		KeyboardManager.GET_MANAGER(_window); // initialisation

		Section e = Settings.INSTANCE.getSection(Strings.WINDOW);
		short w = Short.valueOf(e.getProperty(Strings.WIDTH).getValue());
		short h = Short.valueOf(e.getProperty(Strings.HEIGHT).getValue());
		boolean m = e.getProperty(Strings.MAXIMIZED).isTrue();

		String s = Paths.IMAGES + "logo/";
		_window.getIcons().addAll(new Image(s + "16.png"), new Image(s + "32.png"), new Image(s + "48.png"));
		_window.setMinWidth(_MIN_WIDTH);
		_window.setMinHeight(_MIN_HEIGHT);
		_window.setScene(new Scene(new UI(), w, h, Graphics.BACKGROUND_COLOR));
		updateMemoryUsage();

		if (m)
		{
			stage.setMaximized(true);
			_window.show();
		}
		else
		{
			_window.centerOnScreen();
			_window.show();
			_window.setWidth(w); // sinon w > 2px et h > 25px ??
			_window.setHeight(h);
		}
	}


	private void _initHandlers()
	{
		_window.setOnHiding(new EventHandler<WindowEvent>()
		{
			@Override
			public void handle(WindowEvent event) { _onQuit(); }
		});

		_window.maximizedProperty().addListener(new ChangeListener<Boolean>()
		{
			@Override
			public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue)
			{
				_onMaximize(newValue);
			}
		});

		_window.addEventHandler(WindowEvent.WINDOW_HIDDEN, new EventHandler<WindowEvent>()
		{
			@Override
			public void handle(WindowEvent event) { System.exit(0); }
		});
	}


	private void _initSettings()
	{
		Section s = Settings.INSTANCE.getSection(Strings.WINDOW);
		Property p = s.getProperty(Strings.WIDTH);
		p.setDefault(_DEFAULT_WIDTH, true);
		p.setValue((short) Math.max(_MIN_WIDTH, Short.valueOf(p.getValue())));

		p = s.getProperty(Strings.HEIGHT);
		p.setDefault(_DEFAULT_HEIGHT, true);
		p.setValue(Math.max(_MIN_HEIGHT, Short.valueOf(p.getValue())));

		p = s.getProperty(Strings.MAXIMIZED);
		p.setDefault(false);
	}


	private void _onMaximize(boolean v)
	{
		Property p = Settings.INSTANCE.getSection(Strings.WINDOW).getProperty(Strings.MAXIMIZED);
		p.setValue(v);
		Settings.INSTANCE.save();
	}


	private void _onQuit()
	{
		if (!_window.isMaximized())
		{
			Section e = Settings.INSTANCE.getSection(Strings.WINDOW);
			e.getProperty(Strings.WIDTH).setValue((int) Math.round(_window.getWidth()));
			e.getProperty(Strings.HEIGHT).setValue((int) Math.round(_window.getHeight()));
		}

		Settings.INSTANCE.save();
	}
}