package application.gui.screens.components;

import java.io.File;
import java.util.HashMap;

import org.apache.commons.io.FilenameUtils;

import application.gui.Window;
import application.gui.screens.controllers.TTSMenuController;
import framework.ScratchDir;
import framework.component.PrefFileChooser;
import framework.media.conversion.FFMPEGConverterTask;
import framework.media.conversion.SoxConverterTask;
import framework.savefunction.SaveFileDO;
import framework.savefunction.saveableobjects.DoubleSaveableObject;
import framework.savefunction.saveableobjects.SaveableObject;
import framework.savefunction.saveableobjects.StringSaveableObject;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.Slider;
import javafx.scene.control.TitledPane;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.RowConstraints;
import javafx.scene.layout.VBox;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import javafx.scene.text.Text;
import javafx.stage.FileChooser.ExtensionFilter;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Duration;

public class VideoPlayer extends BorderPane {

	/**
	 * The media view;
	 */
	MediaView mediaView;
	/**
	 * The gridpane at the bottom.
	 */
	GridPane bottomGridPane;
	/**
	 * The progress bar.
	 */
	ProgressBar progressBar;
	/**
	 * The progress slider.
	 */
	Slider progressSlider;

	/**
	 * Current duration of the video.
	 */
	Duration duration;

	/**
	 * The current time label.
	 */
	Label currentTimeLabel;

	/**
	 * Bar of buttons, for control of the video.
	 */
	HBox buttonBar;

	/**
	 * Used to play and pause the video.
	 */
	Button playPauseButton;
	/**
	 * Steps a frame forward in the video.
	 */
	Button stepForwardButton;
	/**
	 * Steps a frame backward in the video.
	 */
	Button stepBackwardButton;
	/**
	 * Reverses the video.
	 */
	Button reverseVideoButton;
	/**
	 * Fast forwards the video.
	 */
	Button fastForwardButton;

	/**
	 * Button used to merge generic audio.
	 */
	Button mergeAudioButton;

	/**
	 * Button to merge tts.
	 */
	Button mergeTTSButton;

	ProgressBar conversionProgressBar;

	/*
	 * =====================================================================
	 * Below are objects that aren't GUI objects.
	 * ===================================================================
	 */

	/**
	 * The timeline system used for seeking.
	 */
	Timeline seekingTimeline;

	/**
	 * The rate at which to seek when fast forwarding or rewinding, in seconds
	 * changed per seek. Default is 1.
	 */
	Integer seekRate = 1;

	static final Integer MIN_SEEK_RATE = 2;

	// Assume a 24 fps video, which is standard. Therefore, 1 frame is
	// 1/24th of a second. To step forward, we then increment it by that
	// much.
	static final double FRAME_MILLIS = 1000.0 / 24.0;
	private FlowPane bottomMergingButtonFlowPane;
	private Text titleText;

	public VideoPlayer() {

		setId("videoPlayer");

		mediaView = new MediaView();
		mediaView.setId("mediaView");

		bottomGridPane = new GridPane();

		bottomGridPane.getColumnConstraints().clear();
		ColumnConstraints col1 = new ColumnConstraints();
		col1.setMinWidth(10);
		col1.setPrefWidth(100);
		col1.setHgrow(Priority.SOMETIMES);
		col1.setHalignment(HPos.CENTER);

		ColumnConstraints col2 = new ColumnConstraints();
		col2.setMinWidth(10);
		col2.setPrefWidth(100);
		col2.setHgrow(Priority.ALWAYS);
		col2.setHalignment(HPos.CENTER);

		ColumnConstraints col3 = new ColumnConstraints();
		col3.setMinWidth(10);
		col3.setPrefWidth(100);
		col3.setHgrow(Priority.SOMETIMES);
		col3.setHalignment(HPos.CENTER);

		bottomGridPane.getColumnConstraints().addAll(col1, col2, col3);

		bottomGridPane.getRowConstraints().clear();

		RowConstraints row1 = new RowConstraints();
		row1.setMinHeight(10);
		row1.setPrefHeight(30);
		row1.setVgrow(Priority.SOMETIMES);

		RowConstraints row2 = new RowConstraints();
		row2.setMinHeight(10);
		row2.setPrefHeight(30);
		row2.setVgrow(Priority.SOMETIMES);

		bottomGridPane.getRowConstraints().addAll(row1, row2);

		bottomGridPane.setVgap(10);
		bottomGridPane.setPadding(new Insets(5));

		progressBar = new ProgressBar();
		// Clamp to max
		progressBar.setMaxWidth(Double.MAX_VALUE);

		progressSlider = new Slider();
		progressSlider.setMaxWidth(Double.MAX_VALUE);

		progressSlider.valueProperty().set(0);
		progressSlider.valueProperty().addListener((observable, oldValue, newValue) -> {

			double value = (Double) newValue / progressSlider.getMax();

			// This little block just adjusts the progress so that it
			// appears under the slider object rather than not.
			if (value < 0.25) {
				value += 0.02;
			} else if (value < 0.5) {
				value += 0.001;
			} else if (value < 0.75) {
				value += 0.0001;
			}
			progressBar.setProgress(value);
		});

		bottomGridPane.add(progressBar, 1, 0);
		bottomGridPane.add(progressSlider, 1, 0);

		currentTimeLabel = new Label();
		currentTimeLabel.setAlignment(Pos.CENTER);

		bottomGridPane.add(currentTimeLabel, 2, 0);

		buttonBar = new HBox();
		buttonBar.setAlignment(Pos.CENTER);

		playPauseButton = new Button("||");
		stepForwardButton = new Button("|>");
		stepBackwardButton = new Button("<|");
		fastForwardButton = new Button(">>");
		reverseVideoButton = new Button("<<");

		buttonBar.getChildren().addAll(reverseVideoButton, stepBackwardButton, playPauseButton, stepForwardButton,
				fastForwardButton);

		bottomGridPane.add(buttonBar, 1, 1);

		mergeAudioButton = new Button("Merge Audio");
		mergeTTSButton = new Button("Merge TTS");

		bottomMergingButtonFlowPane = new FlowPane();
		bottomMergingButtonFlowPane.setOrientation(Orientation.HORIZONTAL);

		bottomMergingButtonFlowPane.getChildren().addAll(mergeAudioButton, mergeTTSButton);

		// bottomGridPane.add(mergeAudioButton, 2, 2);
		// bottomGridPane.add(mergeTTSButton, 2, 2);
		bottomGridPane.add(bottomMergingButtonFlowPane, 2, 2);

		conversionProgressBar = new ProgressBar();
		conversionProgressBar.setProgress(0);

		bottomGridPane.add(conversionProgressBar, 0, 2);

		/*
		 * <ButtonBar prefHeight="40.0" prefWidth="200.0"> <buttons> <Button
		 * mnemonicParsing="false" text="&lt;&lt;" /> <Button
		 * mnemonicParsing="false" text="&lt;|" /> <Button
		 * mnemonicParsing="false" text="&gt;" /> <Button
		 * mnemonicParsing="false" text="|&gt;" /> <Button
		 * mnemonicParsing="false" text="&gt;&gt;" /> </buttons> </ButtonBar>
		 */

		// this.getStylesheets().add("/css/VideoPlayer_Base.css");
		this.setStyle("-fx-background-color : white");
		this.setMaxSize(600, 600);

		// http://stackoverflow.com/questions/14157161/hybrid-of-slider-with-progress-bar-javafx
		// used for progress bar / slider hybrid

		// Bind the value property of the slider to the progress property, so
		// both are updated.

		// Start the media.
		// startMedia();

		// Register the button event handlers.
		playPauseButton.setOnAction(event -> {
			stopTimeline();

			if (playPauseButton.getText().equals("||")) {
				pauseVideo();
			} else if (playPauseButton.getText().equals(">")) {
				playVideo();
			}
		});

		stepForwardButton.setOnAction(event -> {
			stopTimeline();
			pauseVideo();

			MediaPlayer mediaPlayer = mediaView.getMediaPlayer();
			Duration currentTime = mediaPlayer.getCurrentTime();

			Duration newTime = currentTime.add(new Duration(FRAME_MILLIS));

			mediaPlayer.seek(newTime);

		});

		stepBackwardButton.setOnAction(event -> {
			stopTimeline();
			pauseVideo();

			MediaPlayer mediaPlayer = mediaView.getMediaPlayer();
			Duration currentTime = mediaPlayer.getCurrentTime();

			Duration newTime = currentTime.subtract(new Duration(FRAME_MILLIS));

			mediaPlayer.seek(newTime);

		});

		fastForwardButton.setOnAction(event -> {

			pauseVideo();

			if (seekRate < MIN_SEEK_RATE) {
				seekRate = MIN_SEEK_RATE;
			} else {
				seekRate *= 2;
			}

			// Pause the video
			mediaView.getMediaPlayer().pause();
			startTimeline();
		});

		reverseVideoButton.setOnAction(event -> {
			pauseVideo();

			if (seekRate > -MIN_SEEK_RATE) {
				seekRate = -MIN_SEEK_RATE;
			} else {
				seekRate *= 2;
			}

			mediaView.getMediaPlayer().pause();
			startTimeline();
		});

		mergeAudioButton.setOnAction(event -> {
			// Pause the video so that the location is preserved.
			pauseVideo();

			PrefFileChooser chooser = new PrefFileChooser();
			chooser.setExtensionFilters(new ExtensionFilter("Wav", "*.wav"), new ExtensionFilter("All Files", "*"));

			File mp3File = chooser.showOpenDialog(Window.getPrimaryStage());

			// If the file is null then exit out.
			if (mp3File == null) {
				System.out.println("no mp3 file chosen");
				return;
			}

			File saveFile = getOutputSaveFile();

			if (saveFile != null) {
				// Handle the cases where for some reason, the extension is not
				// appended. Check the three main endings for mp4's - .mp4, m4v
				// and
				// .m4a
				if (!saveFile.getAbsolutePath().endsWith(".mp4") && !saveFile.getAbsolutePath().endsWith(".m4a")
						&& !saveFile.getAbsolutePath().endsWith(".m4v")) {
					saveFile = new File(saveFile.getAbsolutePath() + ".mp4");
				}

				String outputName = saveFile.getAbsolutePath();
				mergeWithAudioAtLocation(mp3File.getAbsolutePath(), outputName);

				startMedia(new Media("file:///" + saveFile.getAbsolutePath().replaceAll("\\", "/")));
			} else {
				if (playPauseButton.getText().equals(">")) {
					playPauseButton.fire();
				}
			}
		});

		mergeTTSButton.setOnAction(event -> {
			String filename = null;
			if (mediaView.getMediaPlayer() != null && mediaView.getMediaPlayer().getMedia() != null) {
				filename = mediaView.getMediaPlayer().getMedia().getSource();
			} else {
				Alert alert = new Alert(AlertType.ERROR,
						"Please open a video first, before attempting to add text to speech.", ButtonType.OK);
				alert.setWidth(400);
				alert.setHeight(300);
				alert.showAndWait();
				return;
			}

			FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/TTSMenu.fxml"));
			Parent ttsRoot = null;
			try {
				ttsRoot = loader.load();
			} catch (Exception e) {
				System.out.println("Fialed to load fxml");
				e.printStackTrace();
			}
			Scene scene = new Scene(ttsRoot);

			// Pause the video
			if (playPauseButton.getText().equals("||")) {
				playPauseButton.fire();
			}

			final Stage dialog = new Stage();
			dialog.initModality(Modality.APPLICATION_MODAL);
			dialog.initOwner(Window.getPrimaryStage());

			dialog.setScene(scene);
			dialog.showAndWait();

			TTSMenuController controller = loader.<TTSMenuController> getController();
			String ttsFilename = controller.getTTSFilename();

			// Quit if the returned file is null i.e. they closed the box.
			if (ttsFilename == null) {
				if (playPauseButton.getText().equals(">")) {
					playPauseButton.fire();
				}
				return;
			}

			// FileChooser chooser = getChooserDialog("Please select location to
			// save combined output file to");
			File saveFile = getOutputSaveFile();

			if (saveFile != null) {
				// Handle the cases where for some reason, the extension is not
				// appended. Check the three main endings for mp4's - .mp4, m4v
				// and
				// .m4a
				if (!saveFile.getAbsolutePath().endsWith(".mp4") && !saveFile.getAbsolutePath().endsWith(".m4a")
						&& !saveFile.getAbsolutePath().endsWith(".m4v")) {
					saveFile = new File(saveFile.getAbsolutePath() + ".mp4");
				}

				String outputName = saveFile.getAbsolutePath();
				mergeWithAudioAtLocation(ttsFilename, outputName);

				startMedia(new Media("file:///" + sanitiseFileName(saveFile.getAbsolutePath())));
			} else {
				if (playPauseButton.getText().equals(">")) {
					playPauseButton.fire();
				}
			}
		});

		VBox titleBox = new VBox();

		// Used below link
		// https://docs.oracle.com/javafx/2/events/DraggablePanelsExample.java.htm

		// The code for that was adopted and changed to use a a title pane
		// instead of one the whole node - this is because we don't want the
		// whole node to be dragable all the time.

		TitledPane titlePane = new TitledPane();

		titleText = new Text();
		titleText.setId("titleText");
		titleText.setText("Please select a video to begin");

		titleBox.getChildren().add(titlePane);
		titleBox.getChildren().add(titleText);

		// Dragability setup:

		final BooleanProperty dragModeActiveProperty = new SimpleBooleanProperty(this, "dragModeActive", true);
		final DragContext dragContext = new DragContext();

		titlePane.addEventFilter(MouseEvent.ANY, new EventHandler<MouseEvent>() {
			public void handle(final MouseEvent mouseEvent) {
				if (dragModeActiveProperty.get()) {
					// disable mouse events for all children
					mouseEvent.consume();
				}
			}
		});

		titlePane.addEventFilter(MouseEvent.MOUSE_PRESSED, new EventHandler<MouseEvent>() {
			public void handle(final MouseEvent mouseEvent) {
				if (dragModeActiveProperty.get()) {
					// remember initial mouse cursor coordinates
					// and node position
					dragContext.mouseAnchorX = mouseEvent.getX();
					dragContext.mouseAnchorY = mouseEvent.getY();
					dragContext.initialTranslateX = getTranslateX();
					dragContext.initialTranslateY = getTranslateY();
				}
			}
		});

		titlePane.addEventFilter(MouseEvent.MOUSE_DRAGGED, new EventHandler<MouseEvent>() {
			public void handle(final MouseEvent mouseEvent) {
				if (dragModeActiveProperty.get()) {
					// shift node from its initial position by delta
					// calculated from mouse cursor movement
					setTranslateX(dragContext.initialTranslateX + mouseEvent.getX() - dragContext.mouseAnchorX);
					setTranslateY(dragContext.initialTranslateY + mouseEvent.getY() - dragContext.mouseAnchorY);
				}
			}
		});

		setTop(titleBox);
		setCenter(mediaView);
		setBottom(bottomGridPane);

		// Set the top to be centred.
		setAlignment(getTop(), Pos.CENTER);

	}

	private static final class DragContext {
		public double mouseAnchorX;
		public double mouseAnchorY;
		public double initialTranslateX;
		public double initialTranslateY;
	}

	public DoubleProperty getProgressSliderProperty() {
		return progressSlider.valueProperty();
	}

	private File getOutputSaveFile() {
		PrefFileChooser chooser = new PrefFileChooser();

		chooser.setTitle("Please select location to save output to");
		chooser.setExtensionFilters(new ExtensionFilter("Mp4", "*.mp4"));
		chooser.setInitialFileName("FinalOutput");

		File saveFile = chooser.showSaveDialog(Window.getPrimaryStage());
		return saveFile;
	}

	private void mergeWithAudioAtLocation(String mp3File, String outputFile) {
		// FileChooser chooser = new FileChooser();
		// chooser.setInitialDirectory(new
		// File(System.getProperty("user.home")));
		// chooser.getExtensionFilters().addAll(new
		// FileChooser.ExtensionFilter("MP4", "*.mp4"),
		// new FileChooser.ExtensionFilter("All Files", "*"));
		//
		// File mp4File = chooser.showOpenDialog(Window.getPrimaryStage());
		//
		// if (mp4File == null) {
		// System.out.println("No mp4 file chosen");
		// return;
		// }
		File mp4File = getMp4FileFromCurrentlyPlayingMedia();

		// http://stackoverflow.com/questions/9027317/how-to-convert-milliseconds-to-hhmmss-format
		// was used for time conversion.
		Duration currentTime = mediaView.getMediaPlayer().getCurrentTime();
		long millis = (long) currentTime.toMillis();
		double seconds = millis / 1000.0;

		System.out.println();
		System.out.println();
		System.out.println(ScratchDir.getScratchDir().getAbsolutePath());
		System.out.println();
		System.out.println();

		File tempAudioFile = new File(ScratchDir.getScratchDir().getAbsolutePath() + "/temp.wav");
		FFMPEGConverterTask stripTask = new FFMPEGConverterTask(mp4File.getAbsolutePath(),
				tempAudioFile.getAbsolutePath());

		Thread thread = new Thread(stripTask);
		thread.setDaemon(false);
		thread.start();

		// Audio strip does not take time.
		try {
			thread.join();
		} catch (InterruptedException e) {
			System.out.println("Interupted");
			e.printStackTrace();
		}

		File tempFinalAudioFile = new File(ScratchDir.getScratchDir().getAbsolutePath() + "/final.wav");
		SoxConverterTask silenceTask = new SoxConverterTask(tempAudioFile.getAbsolutePath(), mp3File, seconds,
				tempFinalAudioFile.getAbsolutePath());

		thread = new Thread(silenceTask);
		thread.setDaemon(false);
		thread.start();

		// Audio silence does not take time.
		try {
			thread.join();
		} catch (InterruptedException e) {
			System.out.println("Interuptted");
			e.printStackTrace();
		}

		// Delete the audio file being used temporarily.
		tempAudioFile.delete();

		System.out.println("Starting sidechain");

		FFMPEGConverterTask sideChainMergeTask = new FFMPEGConverterTask(tempFinalAudioFile.getAbsolutePath(),
				mp4File.getAbsolutePath(), outputFile);

		// Set the duration of the video.
		sideChainMergeTask.setDuration(duration);

		thread = new Thread(sideChainMergeTask);
		thread.setDaemon(false);
		thread.start();

		conversionProgressBar.progressProperty().bind(sideChainMergeTask.progressProperty());
		System.out.println();
		System.out.println();
		System.out.println("Showing");
		System.out.println();
		System.out.println();
		//
		// try {
		// thread.join();
		// } catch (InterruptedException e) {
		// System.out.println("Interupted");
		// e.printStackTrace();
		// }

		sideChainMergeTask.setOnSucceeded(event -> {
			System.out.println("Succeeded");
			conversionProgressBar.progressProperty().unbind();
			conversionProgressBar.setProgress(0);

			tempFinalAudioFile.delete();

			startMedia(new Media("file:///" + sanitiseFileName(outputFile)));
		});
		sideChainMergeTask.setOnCancelled(event -> {
			System.out.println("Cancelled.");
			conversionProgressBar.progressProperty().unbind();
			conversionProgressBar.setProgress(0);

			tempFinalAudioFile.delete();
		});
		sideChainMergeTask.setOnFailed(event -> {
			System.out.println("Failed");
			conversionProgressBar.progressProperty().unbind();
			conversionProgressBar.setProgress(0);

			tempFinalAudioFile.delete();
		});

		// String ffmpegCommand = "ffmpeg -y -i " + mp4File.getAbsolutePath() +
		// " ~/stripped.wav && ";
		// ffmpegCommand += "sox -m -v0 ~/stripped.wav \"| sox " + mp3File + "
		// -c 2 -p pad " + seconds
		// + " \" ~/final.wav && ";
		// ffmpegCommand += "ffmpeg -y -i " + mp4File.getAbsolutePath()
		// + " -i ~/final.wav -filter_complex
		// \"[1:a]asplit=2[sc][mix];[0:a][sc]sidechaincompress[compr];[compr][mix]amerge\"
		// "
		// + "-acodec aac -strict -2 -preset ultrafast " + outputFile + " && " +
		// "rm ~/stripped.wav ~/final.wav";
		//
		// System.out.println("Command:\n" + ffmpegCommand);
		//
		// ProcessBuilder procBuilder = null;
		//
		// String os = System.getProperty("os.name").toLowerCase();
		// if (os.contains("win")) {
		// procBuilder = new ProcessBuilder("cmd", "/c", ffmpegCommand);
		// } else {
		// procBuilder = new ProcessBuilder("/bin/bash", "-c", ffmpegCommand);
		// }
		//
		// Alert convertAlert = new Alert(AlertType.INFORMATION, "Merging audio
		// file into video", ButtonType.OK);
		// convertAlert.setWidth(400);
		// convertAlert.setHeight(300);
		// convertAlert.initModality(Modality.APPLICATION_MODAL);
		// convertAlert.initOwner(Window.getPrimaryStage());
		//
		// convertAlert.show();
		//
		// procBuilder.redirectErrorStream(true);
		// try {
		// Process process = procBuilder.start();
		//
		// InputStream inputStream = process.getInputStream();
		// OutputStream outputStream = process.getOutputStream();
		//
		// BufferedReader reader = new BufferedReader(new
		// InputStreamReader(inputStream));
		//
		// String line = null;
		//
		// while ((line = reader.readLine()) != null) {
		// System.out.println("Process: " + line);
		// }
		//
		// } catch (Exception e) {
		// System.out.println("Failed process");
		// e.printStackTrace();
		// }
		//
		// convertAlert.close();
		//
		// Alert convertedAlert = new Alert(AlertType.INFORMATION,
		// "Video has been converted. Please open the saved location using the
		// open video command to view.",
		// ButtonType.OK);
		// convertedAlert.setWidth(400);
		// convertedAlert.setHeight(300);
		// convertedAlert.showAndWait();
	}

	/**
	 * Will get the mp4 file from the currently playing media, stripping of the
	 * file:/// specifier. Note, this requires a video to alreadu be running.
	 * 
	 * @return
	 */
	private File getMp4FileFromCurrentlyPlayingMedia() {
		MediaPlayer mediaPlayer = mediaView.getMediaPlayer();

		if (mediaPlayer == null || mediaPlayer.getMedia() == null) {
			return null;
		}
		String mp4FilePath = mediaPlayer.getMedia().getSource();
		mp4FilePath = mp4FilePath.substring("file:///".length());
		File mp4File = new File(mp4FilePath);
		return mp4File;
	}

	private void playVideo() {
		playPauseButton.setText("||");
		mediaView.getMediaPlayer().play();
	}

	private void pauseVideo() {
		playPauseButton.setText(">");
		mediaView.getMediaPlayer().pause();
	}

	public void startTimeline() {
		// To avoid double up issues, remove the previous instance of the
		// seeking timeline.
		if (seekingTimeline != null) {
			seekingTimeline.stop();
			// Mark for garbage collection.
			seekingTimeline = null;
		}

		seekingTimeline = new Timeline(new KeyFrame(Duration.seconds(1), event -> {
			MediaPlayer mediaPlayer = mediaView.getMediaPlayer();
			Duration currentTime = mediaPlayer.getCurrentTime();

			mediaPlayer.seek(currentTime.add(Duration.seconds(seekRate)));
		}));

		seekingTimeline.setCycleCount(Timeline.INDEFINITE);
		seekingTimeline.play();
	}

	public void stopTimeline() {
		// Only work on it the timeline is not null.
		if (seekingTimeline != null) {

			seekingTimeline.stop();
			seekRate = 1;

			seekingTimeline = null;
		}
	}

	public void startMedia(Media media) {

		MediaPlayer mediaPlayer = new MediaPlayer(media);
		mediaPlayer.setAutoPlay(true);

		mediaPlayer.seek(new Duration(0));

		// Unload existing player
		MediaPlayer existingMedia = mediaView.getMediaPlayer();
		if (existingMedia != null) {
			existingMedia.stop();
			existingMedia = null;
		}

		mediaView.setMediaPlayer(mediaPlayer);

		MediaPlayer mp = mediaView.getMediaPlayer();

		// https://docs.oracle.com/javase/8/javafx/media-tutorial/playercontrol.htm
		// was used.
		mp.currentTimeProperty().addListener(new InvalidationListener() {
			public void invalidated(Observable ov) {
				updateValues();
			}
		});

		mp.setOnReady(new Runnable() {
			public void run() {
				mediaOnReady(mp);

			}
		});

		mp.setOnEndOfMedia(() -> {
			// When the media is finished, we want to pause the video (i.e. have
			// the button set to pause).
			pauseVideo();
		});

		// Value changing property for the sliding action.
		progressSlider.valueProperty().addListener(new InvalidationListener() {
			public void invalidated(Observable ov) {
				if (progressSlider.isValueChanging()) {
					// multiply duration by percentage calculated by slider
					// position
					mp.seek(duration.multiply(progressSlider.getValue() / 100.0));
				}
			}
		});

		// Value changing property for the clicking action.
		progressSlider.valueProperty().addListener(new ChangeListener<Number>() {

			@Override
			public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
				if (!progressSlider.isValueChanging()) {

					MediaPlayer mediaPlayer = mediaView.getMediaPlayer();
					if (mediaPlayer != null) {
						double currentTime = mediaPlayer.getCurrentTime().toSeconds();
						double sliderTime = duration.multiply(progressSlider.getValue() / 100.0).toSeconds();

						if (Math.abs(currentTime - sliderTime) > 1) {
							mediaPlayer.seek(duration.multiply(progressSlider.getValue() / 100.0));
						}
					}
				}
			}
		});

		titleText.setText("Playing : " + FilenameUtils.getBaseName(media.getSource()));

	}

	protected void updateValues() {
		if (currentTimeLabel != null && progressSlider != null) {
			Platform.runLater(() -> {
				MediaPlayer mediaPlayer = mediaView.getMediaPlayer();
				Duration currentTime = mediaPlayer.getCurrentTime();
				currentTimeLabel.setText(formatTime(currentTime, duration));

				if (!progressSlider.isDisabled() && duration.greaterThan(Duration.ZERO)
						&& !progressSlider.isValueChanging()) {
					progressSlider.setValue(currentTime.divide(duration).toMillis() * 100.0);
				}

			});
		}
	}

	private void mediaOnReady(MediaPlayer mp) {
		duration = mp.getMedia().getDuration();
		updateValues();
	}

	private static String formatTime(Duration elapsed, Duration duration) {
		int intElapsed = (int) Math.floor(elapsed.toSeconds());
		int elapsedHours = intElapsed / (60 * 60);
		if (elapsedHours > 0) {
			intElapsed -= elapsedHours * 60 * 60;
		}
		int elapsedMinutes = intElapsed / 60;
		int elapsedSeconds = intElapsed - elapsedHours * 60 * 60 - elapsedMinutes * 60;

		if (duration.greaterThan(Duration.ZERO)) {
			int intDuration = (int) Math.floor(duration.toSeconds());
			int durationHours = intDuration / (60 * 60);
			if (durationHours > 0) {
				intDuration -= durationHours * 60 * 60;
			}
			int durationMinutes = intDuration / 60;
			int durationSeconds = intDuration - durationHours * 60 * 60 - durationMinutes * 60;
			if (durationHours > 0) {
				return String.format("%d:%02d:%02d/%d:%02d:%02d", elapsedHours, elapsedMinutes, elapsedSeconds,
						durationHours, durationMinutes, durationSeconds);
			} else {
				return String.format("%02d:%02d/%02d:%02d", elapsedMinutes, elapsedSeconds, durationMinutes,
						durationSeconds);
			}
		} else {
			if (elapsedHours > 0) {
				return String.format("%d:%02d:%02d", elapsedHours, elapsedMinutes, elapsedSeconds);
			} else {
				return String.format("%02d:%02d", elapsedMinutes, elapsedSeconds);
			}
		}
	}

	public MediaView getMediaView() {
		return mediaView;
	}

	public double getMaxSliderValue() {
		return progressSlider.getMax();
	}

	public void setNewVideoToPlay(Media media) {

		startMedia(media);

	}

	public void useSaveFile(SaveFileDO saveFile) {
		HashMap<String, SaveableObject> saveableObjects = saveFile.getSaveableObjects();

		// Will load the video specified in the save file as well, if not null
		if (saveableObjects.get("videoMediaSource") != null) {
			StringSaveableObject stringSave = (StringSaveableObject) saveableObjects.get("videoMediaSource");
			String path = (String) stringSave.getValue();
			File mediaFile = new File(path);
			startMedia(new Media("file:///" + sanitiseFileName(mediaFile.getAbsolutePath())));

			// Once the media is prepped, set the values.
			mediaView.getMediaPlayer().setOnReady(() -> {
				mediaOnReady(mediaView.getMediaPlayer());
				setValuesFromFile(saveFile);
				pauseVideo();
			});

		} else {
			// If there is no pre set media, then just set values
			setValuesFromFile(saveFile);
		}

	}

	private void setValuesFromFile(SaveFileDO saveFile) {

		HashMap<String, SaveableObject> saveableObjects = saveFile.getSaveableObjects();

		progressSlider.setValue((double) saveableObjects.get("progressSlider").getValue());
	}

	private String sanitiseFileName(String absolutePath) {
		absolutePath = absolutePath.replace("\\", "/");
		absolutePath = absolutePath.replace(" ", "%20");
		return absolutePath;
	}

	/**
	 * Generates a save file.
	 * 
	 * @return
	 */
	public SaveFileDO generateSaveObjects(SaveFileDO saveFile) {

		if (saveFile == null) {
			saveFile = new SaveFileDO();
		}

		HashMap<String, SaveableObject> saveableObjects = saveFile.getSaveableObjects();

		saveableObjects.put("progressSlider", new DoubleSaveableObject(progressSlider.getValue()));
		saveableObjects.put("videoMediaSource",
				new StringSaveableObject(getMp4FileFromCurrentlyPlayingMedia().getAbsolutePath()));

		return saveFile;
	}

	public SaveFileDO generateSaveObjects() {
		return generateSaveObjects(null);
	}

}
