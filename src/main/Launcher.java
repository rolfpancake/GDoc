package main;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.jetbrains.annotations.NotNull;
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
import settings.ApplicationSettings;
import settings.WindowSection;
import ui.Graphics;
import ui.UI;
import window.ErrorWindow;


//TODO fenêtre de messages

public final class Launcher
{
	static private final short _MIN_WIDTH = 450;
	static private final short _MIN_HEIGHT = 300;

	static private final String _TITLE_1 = "GDoc " + Main.VERSION + " | ";
	static private final String _TITLE_2 = " KB | ";
	static private final String _TITLE_3 = " KB (";
	static private final String _TITLE_4 = "%)";
	static private final String _TITLE_ERROR = "Error";
	static private final short _GC_THRESHOLD = (short) 35000; // KB

	@NotNull
	static public final ApplicationSettings SETTINGS = new ApplicationSettings();

	@NotNull
	static public final KeyboardManager KEYBOARD = new KeyboardManager();

	@NotNull
	static public final Launcher INSTANCE = new Launcher();

	private Stage _window;
	private Logger _logger;
	private Runtime _runtime;


	private Launcher()
	{
		Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler()
		{
			@Override
			public void uncaughtException(Thread t, Throwable e) { raise(e); }
		});

		_runtime = Runtime.getRuntime();
	}


	public void log(String message)
	{
		if (message == null || message.isEmpty()) return;
		if (_logger == null) _logger = new Logger();
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
			exception.printStackTrace();
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
		_initHandlers();
		KEYBOARD.setWindow(stage);

		WindowSection e = SETTINGS.window();
		e.width().setValue((short) Math.max(_MIN_WIDTH, e.width().toShort()));
		e.height().setValue((short) Math.max(_MIN_HEIGHT, e.height().toShort()));

		short w = e.width().toShort();
		short h = e.height().toShort();

		String s = Paths.IMAGES + "logo/";
		_window.getIcons().addAll(new Image(s + "16.png"), new Image(s + "32.png"), new Image(s + "48.png"));
		_window.setMinWidth(_MIN_WIDTH);
		_window.setMinHeight(_MIN_HEIGHT);
		_window.setScene(new Scene(new UI(), w, h, Graphics.BACKGROUND_COLOR));
		updateMemoryUsage();

		if (e.maximized().toBoolean())
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


	private void _onMaximize(boolean v)
	{
		// les dimensions de la fenêtre sont déjà modifiées
		SETTINGS.window().maximized().setValue(v);
		SETTINGS.save();
	}


	private void _onQuit()
	{
		if (!_window.isMaximized())
		{
			WindowSection e = SETTINGS.window();
			e.width().setValue((short) Math.round(_window.getWidth()));
			e.height().setValue((short) Math.round(_window.getHeight()));
		}

		SETTINGS.save();
	}

	/*private void _write(String message)
	{
		Path p = Paths.USER_DIR.resolve("error.txt");
		if (Files.isDirectory(p)) return;
		FileWriter w = null;

		try
		{
			w = new FileWriter(p.toFile());
			w.write(message);
		}
		catch (IOException e)
		{
			Launcher.INSTANCE.log(e);
		}

		if (w != null)
		{
			try
			{
				w.close();
			}
			catch (IOException e)
			{
				Launcher.INSTANCE.log(e);
			}
		}
	}*/
}