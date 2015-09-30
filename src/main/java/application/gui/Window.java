package application.gui;

import javafx.application.Application;
import javafx.application.Preloader.ProgressNotification;
import javafx.application.Preloader.StateChangeNotification;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.concurrent.Task;
import javafx.stage.Stage;

public class Window extends Application {

	private static Stage primaryStage;

	public static Stage getPrimaryStage() {
		return primaryStage;
	}

	@Override
	public void start(Stage primaryStage) throws Exception {
		setPrimaryStage(primaryStage);
		primaryStage.setTitle("Vidivox");

		primaryStage.show();

	}

	public void begin(String[] args) {
		launch(args);
	}

	public static void setPrimaryStage(Stage primaryStage) {
		Window.primaryStage = primaryStage;
	}

}
