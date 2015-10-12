package application.gui.screens.controllers;

import java.io.File;
import java.net.URL;
import java.util.ResourceBundle;

import application.gui.Window;
import application.gui.screens.components.VideoPlayer;
import javafx.fxml.Initializable;
import javafx.fxml.FXML;
import javafx.scene.control.MenuBar;
import javafx.scene.layout.BorderPane;
import javafx.scene.media.Media;
import javafx.stage.FileChooser;
import javafx.event.ActionEvent;

/**
 * THe controller for the main screen.
 * 
 * @author sraj144
 */
public class MainScreenController implements Initializable {

	@FXML
	MenuBar menuBar;
	@FXML
	BorderPane mainScreen_Root;

	VideoPlayer videoPlayer;

	/**
	 * Initializes the screen.
	 */
	@Override
	public void initialize(URL location, ResourceBundle resources) {
		videoPlayer = new VideoPlayer();

		mainScreen_Root.setCenter(videoPlayer);

		// player.init();
	}

	@FXML
	public void closeButton(ActionEvent event) {
		// Close the application.
		Window.getPrimaryStage().hide();
	}

	@FXML
	public void onOpenVideo(ActionEvent event) {
		FileChooser chooser = new FileChooser();
		chooser.setInitialDirectory(new File(System.getProperty("user.home")));
		chooser.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("Mp3's", "*.mp4"));

		File videoFile = chooser.showOpenDialog(Window.getPrimaryStage());

		String absolutePath = videoFile.getAbsolutePath();

		absolutePath = sanitiseFileName(absolutePath);

		Media media = new Media("file:///" + absolutePath);

		videoPlayer.setNewVideoToPlay(media);
	}

	private String sanitiseFileName(String absolutePath) {
		absolutePath = absolutePath.replace("\\", "/");
		absolutePath = absolutePath.replace(" ", "%20");
		return absolutePath;
	}

}
