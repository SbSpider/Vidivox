package application.gui.screens.components;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

import application.gui.util.FXMLFilenameConstants;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.Slider;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;

public class VideoPlayer extends BorderPane implements Initializable {

	@FXML
	MediaView mediaView;

	@FXML
	ProgressBar progressBar;

	@FXML
	Slider progressSlider;

	@FXML
	GridPane bottomGridPane;

	public VideoPlayer() {
		FXMLLoader loader = new FXMLLoader(getClass().getResource(FXMLFilenameConstants.VIDEO_PLAYER_FXML_FQ));
		loader.setRoot(this);
		loader.setController(this);

		try {
			loader.load();
		} catch (IOException e) {
			System.out.println("Failed");
			throw new RuntimeException("Could not load from file.", e);
		}

		// this.getStylesheets().add("/css/VideoPlayer_Base.css");
		this.setStyle("-fx-background-color : white");
		this.setMaxSize(800, 800);

		// this.setMaxSize(200, 200);

		// http://stackoverflow.com/questions/14157161/hybrid-of-slider-with-progress-bar-javafx
		// used for progress bar / slider hybrid

		// Bind the value property of the slider to the progress property, so
		// both are updated.

		System.out.println("Ready to view");

	}

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		if (mediaView == null) {
			System.out.println("not ready");
		}

		mediaView.setMediaPlayer(
				new MediaPlayer(new Media("http://download.oracle.com/otndocs/products/javafx/oow2010-2.flv")));
		mediaView.getMediaPlayer().setAutoPlay(true);
		mediaView.getMediaPlayer().play();

		mediaView.getMediaPlayer().setOnReady(new Runnable() {

			@Override
			public void run() {

				progressSlider.valueProperty().set(0);
				progressBar.progressProperty().bind(progressSlider.valueProperty());
			}
		});

	}
}
