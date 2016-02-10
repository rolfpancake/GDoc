package main;

import javafx.application.Application;
import javafx.stage.Stage;


public final class Main extends Application
{
	static public final float VERSION = 0.44f;


	public static void main(String[] args)
	{
		launch(args);
	}


	@Override
	public void start(Stage stage) throws Exception
	{
		Launcher.INSTANCE.start(stage);
	}
}