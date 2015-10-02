package application.gui;

import java.io.IOException;

import application.gui.screens.FXMLFilenameConstants;
import framework.utils.FXMLLoadingUtil;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Window extends Application {

	private static Stage primaryStage;

	public static Stage getPrimaryStage() {
		return primaryStage;
	}

	@Override
	public void start(Stage primaryStage) {

		Parent root = null;
		try {
			// Preload the FXML. All FXML must be preloaded.
			FXMLLoadingUtil.registerFXML(FXMLFilenameConstants.MAIN_SCREEN_FXML);

		} catch (IOException e) {
			System.out.println("IO Exception when loading fxml, likely that the FXML could not be found.");
			e.printStackTrace();
			return;
		} catch (Exception e) {
			System.out.println("Error occured in FXML preload.");
			throw e;
		}

		System.out.println("Preloaded fxml");

		setPrimaryStage(primaryStage);
		primaryStage.setTitle("Vidivox");

		Scene scene = new Scene(FXMLLoadingUtil.getFXMLRoot(FXMLFilenameConstants.MAIN_SCREEN_FXML));

		primaryStage.setScene(scene);

		primaryStage.show();

	}

	public void begin(String[] args) {
		launch(args);
	}

	public static void setPrimaryStage(Stage primaryStage) {
		Window.primaryStage = primaryStage;
	}

}
