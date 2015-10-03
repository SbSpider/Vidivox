package application.gui.screens.components;

import java.io.IOException;

import application.gui.util.FXMLFilenameConstants;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.layout.BorderPane;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;

public class VideoPlayer extends BorderPane {

	@FXML
	MediaView mediaView;

	public VideoPlayer() {
		FXMLLoader loader = new FXMLLoader(getClass().getResource(FXMLFilenameConstants.VIDEO_PLAYER_FXML_FQ));
		loader.setRoot(this);
		loader.setController(this);

		try {
			loader.load();
		} catch (IOException e) {
			System.out.println("FAiled");
			throw new RuntimeException("Could not load from file.", e);
		}

		// this.getStylesheets().add("/css/VideoPlayer_Base.css");
		this.setStyle("-fx-background-color : white");
		this.setMaxSize(800, 800);

		// this.setMaxSize(200, 200);

		System.out.println("Ready to view");

	}
}
