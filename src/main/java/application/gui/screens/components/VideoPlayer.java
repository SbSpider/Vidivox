package application.gui.screens.components;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.concurrent.TimeUnit;

import application.gui.Window;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.Slider;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.RowConstraints;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import javafx.stage.FileChooser;
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
	 * Button used to merge tts.
	 */
	Button mergeTTSButton;

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

	public VideoPlayer() {

		mediaView = new MediaView();

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

		playPauseButton = new Button(">");
		stepForwardButton = new Button("|>");
		stepBackwardButton = new Button("<|");
		fastForwardButton = new Button(">>");
		reverseVideoButton = new Button("<<");

		buttonBar.getChildren().addAll(reverseVideoButton, stepBackwardButton, playPauseButton, stepForwardButton,
				fastForwardButton);

		bottomGridPane.add(buttonBar, 1, 1);

		mergeTTSButton = new Button("Merge TTS");

		bottomGridPane.add(mergeTTSButton, 2, 2);

		/*
		 * <ButtonBar prefHeight="40.0" prefWidth="200.0"> <buttons> <Button
		 * mnemonicParsing="false" text="&lt;&lt;" /> <Button
		 * mnemonicParsing="false" text="&lt;|" /> <Button
		 * mnemonicParsing="false" text="&gt;" /> <Button
		 * mnemonicParsing="false" text="|&gt;" /> <Button
		 * mnemonicParsing="false" text="&gt;&gt;" /> </buttons> </ButtonBar>
		 */

		setCenter(mediaView);
		setBottom(bottomGridPane);

		// this.getStylesheets().add("/css/VideoPlayer_Base.css");
		this.setStyle("-fx-background-color : white");
		this.setMaxSize(800, 800);

		// http://stackoverflow.com/questions/14157161/hybrid-of-slider-with-progress-bar-javafx
		// used for progress bar / slider hybrid

		// Bind the value property of the slider to the progress property, so
		// both are updated.

		// Start the media.
		startMedia();

		// Register the button event handlers.
		playPauseButton.setOnAction(event -> {
			stopTimeline();

			if (playPauseButton.getText().equals(">")) {
				pauseVideo();
			} else if (playPauseButton.getText().equals("||")) {
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

		mergeTTSButton.setOnAction(event -> {
			// Pause the video so that the location is preserved.
			pauseVideo();

			FileChooser chooser = new FileChooser();
			chooser.setInitialDirectory(new File(System.getProperty("user.home")));
			chooser.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("Wav", "*.wav"),
					new FileChooser.ExtensionFilter("All Files", "*"));

			File mp3File = chooser.showOpenDialog(Window.getPrimaryStage());

			// If the file is null then exit out.
			if (mp3File == null) {
				System.out.println("no mp3 file chosen");
				return;
			}

			chooser.getExtensionFilters().clear();
			chooser.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("MP4", "*.mp4"),
					new FileChooser.ExtensionFilter("All Files", "*"));

			File mp4File = chooser.showOpenDialog(Window.getPrimaryStage());

			if (mp4File == null) {
				System.out.println("No mp4 file chosen");
				return;
			}

			// http://stackoverflow.com/questions/9027317/how-to-convert-milliseconds-to-hhmmss-format
			// was used for time conversion.
			Duration currentTime = mediaView.getMediaPlayer().getCurrentTime();
			long millis = (long) currentTime.toMillis();
			String output = String.format("%02d:%02d:%02d.%d", TimeUnit.MILLISECONDS.toHours(millis),
					TimeUnit.MILLISECONDS.toMinutes(millis)
							- TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(millis)),
					TimeUnit.MILLISECONDS.toSeconds(millis)
							- TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(millis)),
					TimeUnit.MILLISECONDS.toMillis(millis)
							- TimeUnit.SECONDS.toMillis(TimeUnit.MILLISECONDS.toSeconds(millis)));

			String ffmpegCommand = "ffmpeg -y -i " + mp4File.getAbsolutePath() + " ~/share/sand/stripped.wav && ";
			ffmpegCommand += "sox -m -v0 ~/share/sand/stripped.wav \"| sox " + mp3File.getAbsolutePath()
					+ " -c 2 -p pad 2 \" ~/share/sand/final.wav && ";
			ffmpegCommand += "ffmpeg -y -i " + mp4File.getAbsolutePath()
					+ " -i ~/share/sand/final.wav -filter_complex \"[1:a]asplit=2[sc][mix];[0:a][sc]sidechaincompress[compr];[compr][mix]amerge\" "
					+ "-acodec aac -strict -2 -preset ultrafast ~/share/sand/superfinal.mp4";

			System.out.println("Command:\n" + ffmpegCommand);

			ProcessBuilder procBuilder = null;

			String os = System.getProperty("os.name").toLowerCase();
			if (os.contains("win")) {
				procBuilder = new ProcessBuilder("cmd", "/c", ffmpegCommand);
			} else {
				procBuilder = new ProcessBuilder("/bin/bash", "-c", ffmpegCommand);
			}

			procBuilder.redirectErrorStream(true);
			try {
				Process process = procBuilder.start();

				InputStream inputStream = process.getInputStream();
				OutputStream outputStream = process.getOutputStream();

				BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));

				String line = null;

				while ((line = reader.readLine()) != null) {
					System.out.println("Process: " + line);
				}

			} catch (Exception e) {
				System.out.println("Failed process");
				e.printStackTrace();
			}

			System.out.println("Current time: " + output);

			playVideo();
		});

	}

	private void playVideo() {
		playPauseButton.setText(">");
		mediaView.getMediaPlayer().play();
	}

	private void pauseVideo() {
		playPauseButton.setText("||");
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

	public void startMedia() {

		mediaView.setMediaPlayer(
				new MediaPlayer(new Media("http://download.oracle.com/otndocs/products/javafx/oow2010-2.flv")));
		mediaView.getMediaPlayer().setAutoPlay(true);
		mediaView.getMediaPlayer().play();

		MediaPlayer mp = mediaView.getMediaPlayer();

		// https://docs.oracle.com/javase/8/javafx/media-tutorial/playercontrol.htm
		// was used.
		mp.currentTimeProperty().addListener(new InvalidationListener() {
			public void invalidated(Observable ov) {
				updateValues();
			}
		});

		mp.setOnPlaying(new Runnable() {
			public void run() {
			}
		});

		mp.setOnPaused(new Runnable() {
			public void run() {
			}
		});

		mp.setOnReady(new Runnable() {
			public void run() {
				duration = mp.getMedia().getDuration();
				updateValues();

			}
		});

		mp.setOnEndOfMedia(new Runnable() {
			public void run() {
			}
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
}
