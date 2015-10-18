package application.gui.screens.controllers;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ResourceBundle;

import application.gui.Window;
import application.gui.screens.components.VideoPlayer;
import framework.component.PrefFileChooser;
import framework.function.savefunction.JSONConverter;
import framework.function.savefunction.SaveFileDO;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.layout.BorderPane;
import javafx.scene.media.Media;
import javafx.stage.FileChooser.ExtensionFilter;

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
	@FXML
	MenuItem openVideoMenuItem;
	@FXML
	MenuItem saveProjectMenuItem;
	@FXML
	MenuItem openProjectMenuItem;
	@FXML
	MenuItem closeWindowMenuItem;
	@FXML
	MenuItem ttsMenuItem;

	/**
	 * Initializes the screen.
	 */
	@Override
	public void initialize(URL location, ResourceBundle resources) {
		videoPlayer = new VideoPlayer();

		mainScreen_Root.setCenter(videoPlayer);

		// Add acellerators
		openVideoMenuItem.setAccelerator(new KeyCodeCombination(KeyCode.O, KeyCombination.CONTROL_DOWN));
		saveProjectMenuItem.setAccelerator(new KeyCodeCombination(KeyCode.S, KeyCombination.CONTROL_DOWN));

	}

	@FXML
	public void closeButton(ActionEvent event) {
		// Close the application.
		Window.getPrimaryStage().hide();
	}

	// Note that during the beta phase, to aid rapid development by exposing
	// issues earlier,
	// file error handling has not been performed (e.g. where the user presses
	// cancel instead of entering a value. This will be included in final
	// submission

	@FXML
	public void onOpenVideo(ActionEvent event) {
		PrefFileChooser chooser = new PrefFileChooser();
		chooser.setExtensionFilters(new ExtensionFilter("Mp4's", "*.mp4"));

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

	@FXML
	public void onSaveProject(ActionEvent event) throws IOException {
		PrefFileChooser chooser = new PrefFileChooser();
		chooser.setExtensionFilters(new ExtensionFilter("Vidivox Project File", "*.vvoxproj"));

		File saveFile = chooser.showSaveDialog(Window.getPrimaryStage());

		FileOutputStream fs = new FileOutputStream(saveFile);
		ObjectOutputStream oos = new ObjectOutputStream(fs);
		oos.writeObject(videoPlayer.generateSaveFile());

		oos.close();
		fs.close();

		// String jsonSaveData =
		// JSONConverter.convertToJson(videoPlayer.generateSaveFile());

		// Files.write(Paths.get(saveFile.toURI()), jsonSaveData.getBytes());
	}

	@FXML
	public void onOpenProject(ActionEvent event) throws IOException, ClassNotFoundException {
		PrefFileChooser chooser = new PrefFileChooser();
		chooser.setExtensionFilters(new ExtensionFilter("Vidivox Project File", "*.vvoxproj"));

		File saveFile = chooser.showOpenDialog(Window.getPrimaryStage());

		// String jsonSaveData = String.join("\n",
		// Files.readAllLines(Paths.get(saveFile.toURI())));

		// System.out.println(jsonSaveData);
		// SaveFileDO saveFileDO = JSONConverter.convertToDO(jsonSaveData);

		FileInputStream fs = new FileInputStream(saveFile);
		ObjectInputStream ois = new ObjectInputStream(fs);
		SaveFileDO saveFileDO = (SaveFileDO) ois.readObject();
		
		videoPlayer.useSaveFile(saveFileDO);
	}

}
