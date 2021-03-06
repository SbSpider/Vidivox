package application.gui.screens.controllers;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.URL;
import java.util.ResourceBundle;

import org.apache.commons.io.FileUtils;

import application.gui.Window;
import application.gui.screens.components.VideoPlayer;
import framework.ScratchDir;
import framework.component.EditingOptions;
import framework.component.PrefFileChooser;
import framework.component.TrackHolder;
import framework.component.TreeViewDirectoryViewer;
import framework.media.conversion.FFMPEGConverterTask;
import framework.savefunction.SaveFileDO;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
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
	 * Application wide save file.
	 */
	SaveFileDO projectFile;
	@FXML
	MenuItem saveProjectAsMenuItem;
	TreeViewDirectoryViewer dirTreeView;
	private TrackHolder trackHolder;
	EditingOptions editingOptions;

	/**
	 * Initializes the screen.
	 */
	@Override
	public void initialize(URL location, ResourceBundle resources) {
		
		// Run core prep work first
		try {
			FFMPEGConverterTask.prepare();
		} catch (IOException e) {
			System.out.println("Unable to prepare core tools, exiting application.");
			e.printStackTrace();
			return;
		}
		
		

		// Setup default scratch dir
		File dir = new File(System.getProperty("user.home") + "/.Vidivox/scratch");
		// Clcear existing data in the node, so we don't persist last location.
		dir.delete();
		// Make the node.
		dir.mkdirs();
		ScratchDir.setScratchDir(dir);

		videoPlayer = new VideoPlayer();
		dirTreeView = new TreeViewDirectoryViewer();

		// Add acellerators
		openVideoMenuItem.setAccelerator(new KeyCodeCombination(KeyCode.O, KeyCombination.CONTROL_DOWN));
		// Until saveProejct is enabled, acceleator for saveproject as is Ctrl +
		// s
		saveProjectAsMenuItem.setAccelerator(new KeyCodeCombination(KeyCode.S, KeyCombination.CONTROL_DOWN));
		openProjectMenuItem.setAccelerator(
				new KeyCodeCombination(KeyCode.O, KeyCombination.CONTROL_DOWN, KeyCombination.SHIFT_DOWN));
		// Deactivate normal save until a project exists
		saveProjectMenuItem.setDisable(true);

		// Set locations
		// mainScreen_Root.setCenter(videoPlayer);
		mainScreen_Root.setLeft(dirTreeView);

		// ClipTrack clipTrack = new
		// ClipTrack(videoPlayer.getMediaView().getMediaPlayer().getMedia());

		mainScreen_Root.setCenter(videoPlayer);

		trackHolder = new TrackHolder();

		mainScreen_Root.setBottom(trackHolder);

		editingOptions = new EditingOptions();

		mainScreen_Root.setRight(editingOptions);

		// Hide the track holder until there is a video present
		trackHolder.setVisible(false);

		editingOptions.setMaxWidth(dirTreeView.getMaxWidth());
		editingOptions.setMaxHeight(dirTreeView.getMaxHeight());
		editingOptions.setPrefWidth(dirTreeView.getPrefWidth());
		editingOptions.setPrefHeight(dirTreeView.getPrefHeight());

		videoPlayer.getAudioVolumeProperty().bind(editingOptions.getAudioProperty());
		videoPlayer.getVideoVolumeProperty().bind(editingOptions.getVideoProperty());
		videoPlayer.getMasterVolumeProperty().bind(editingOptions.getMasterProperty());

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

		updateVideoSources();

	}

	private void updateVideoSources() {
		Runnable onReady = videoPlayer.getMediaView().getMediaPlayer().getOnReady();

		videoPlayer.getMediaView().getMediaPlayer().setOnReady(() -> {
			trackHolder.setVideoSource(videoPlayer);

			// Run onready
			onReady.run();
		});

		// Make the holder visible.
		trackHolder.setVisible(true);
	}

	private String sanitiseFileName(String absolutePath) {
		absolutePath = absolutePath.replace("\\", "/");
		absolutePath = absolutePath.replace(" ", "%20");
		return absolutePath;
	}

	/**
	 * Saves the project, assuming that the project already has a save location.
	 * 
	 * @param event
	 * @throws IOException
	 */
	@FXML
	public void onSaveProject(ActionEvent event) throws IOException {

		if (projectFile == null) {
			Alert warning = new Alert(AlertType.ERROR, "Please use save as first.");
			warning.showAndWait();
			return;
		}

		File saveFile = new File(projectFile.getSaveFilename());

		SaveFileDO saveFileDO = new SaveFileDO();
		saveFileDO.setSaveFilename(saveFile.getAbsolutePath());

		saveFileDO = getSaveObjects(saveFileDO);

		saveProjectToFile(saveFile, saveFileDO);

	}

	private SaveFileDO getSaveObjects(SaveFileDO saveFileDO) {
		saveFileDO = videoPlayer.generateSaveObjects(saveFileDO);
		return saveFileDO;
	}

	private void saveProjectToFile(File saveFile, SaveFileDO saveFileDO) throws IOException {
		FileOutputStream fs = new FileOutputStream(saveFile);
		ObjectOutputStream oos = new ObjectOutputStream(fs);

		oos.writeObject(saveFileDO);

		oos.close();
		fs.close();

		System.out.println("Saved to file");
	}

	@FXML
	public void onOpenProject(ActionEvent event) throws IOException, ClassNotFoundException {
		PrefFileChooser chooser = new PrefFileChooser();
		chooser.setExtensionFilters(new ExtensionFilter("Vidivox Project", "*.vvoxproj"));

		File saveFile = chooser.showOpenDialog(Window.getPrimaryStage());

		FileInputStream fs = new FileInputStream(saveFile);
		ObjectInputStream ois = new ObjectInputStream(fs);
		SaveFileDO saveFileDO = (SaveFileDO) ois.readObject();

		ScratchDir.setScratchDir(saveFile.getParentFile());

		videoPlayer.useSaveFile(saveFileDO);

		initTreeview(saveFile);

		updateVideoSources();
	}

	/**
	 * When saving, you want the dir.
	 * 
	 * @param event
	 * @throws IOException
	 */
	@FXML
	public void onSaveProjectAsAction(ActionEvent event) throws IOException {
		// PrefFileChooser chooser = new PrefFileChooser();
		// chooser.setExtensionFilters(new ExtensionFilter("Vidivox Project
		// File", "*.vvoxproj"));
		//
		// File saveFile = chooser.showSaveDialog(Window.getPrimaryStage());
		PrefFileChooser projectDirectoryChooser = new PrefFileChooser();
		projectDirectoryChooser.setExtensionFilters(new ExtensionFilter("Vidivox Project", "*.vvoxproj"));

		projectDirectoryChooser
				.setTitle("Please select project save location. Directory of save file also used for temp files.");
		File saveFile = projectDirectoryChooser.showSaveDialog(Window.getPrimaryStage());

		if (saveFile == null) {
			return;
		}

		if (!saveFile.getAbsolutePath().endsWith(".vvoxproj")) {
			saveFile = new File(saveFile.getAbsolutePath() + ".vvoxproj");
		}

		projectFile = new SaveFileDO();
		// Sets the save file location
		projectFile.setSaveFilename(saveFile.getAbsolutePath());
		projectFile = getSaveObjects(projectFile);

		saveProjectToFile(saveFile, projectFile);

		// remove accelerator for save project as
		saveProjectAsMenuItem.setAccelerator(null);

		// Enable the save project button
		saveProjectMenuItem.setDisable(false);
		saveProjectMenuItem.setAccelerator(new KeyCodeCombination(KeyCode.S, KeyCombination.CONTROL_DOWN));

		initTreeview(saveFile);

		// Copy the scratchdir to the new location
		File scratchDir = ScratchDir.getScratchDir();

		FileUtils.copyDirectory(scratchDir, saveFile.getParentFile());

		// Setup scratch location.
		ScratchDir.setScratchDir(saveFile.getParentFile());
	}

	private void initTreeview(File saveFile) throws IOException {
		dirTreeView = new TreeViewDirectoryViewer(saveFile.getParentFile());
		dirTreeView.setupTreeView();

		dirTreeView.runBackground();

		mainScreen_Root.setLeft(dirTreeView);
	}

}
