package application.gui.screens.components;

import javafx.application.Platform;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.Slider;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.RowConstraints;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import javafx.util.Duration;

public class VideoPlayer extends BorderPane {

	MediaView mediaView;
	GridPane bottomGridPane;
	ProgressBar progressBar;
	Slider progressSlider;
	Duration totalDuration;
	protected Duration duration;
	Label playTime = new Label();

	public VideoPlayer() {

		mediaView = new MediaView();

		bottomGridPane = new GridPane();

		bottomGridPane.getColumnConstraints().clear();
		ColumnConstraints col1 = new ColumnConstraints();
		col1.setMinWidth(10);
		col1.setPrefWidth(100);
		col1.setHgrow(Priority.SOMETIMES);

		ColumnConstraints col2 = new ColumnConstraints();
		col2.setMinWidth(10);
		col2.setPrefWidth(100);
		col2.setHgrow(Priority.ALWAYS);

		ColumnConstraints col3 = new ColumnConstraints();
		col3.setMinWidth(10);
		col3.setPrefWidth(100);
		col3.setHgrow(Priority.SOMETIMES);

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

		progressBar = new ProgressBar();
		// Clamp to max
		progressBar.setMaxWidth(Double.MAX_VALUE);

		progressSlider = new Slider();
		progressSlider.setMaxWidth(Double.MAX_VALUE);

		progressSlider.valueProperty().set(0);
		progressSlider.valueProperty().addListener(new ChangeListener<Number>() {

			@Override
			public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
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
			}
		});

		bottomGridPane.add(progressBar, 1, 0);
		bottomGridPane.add(progressSlider, 1, 0);

		setCenter(mediaView);
		setBottom(bottomGridPane);

		// this.getStylesheets().add("/css/VideoPlayer_Base.css");
		this.setStyle("-fx-background-color : white");
		this.setMaxSize(800, 800);

		// http://stackoverflow.com/questions/14157161/hybrid-of-slider-with-progress-bar-javafx
		// used for progress bar / slider hybrid

		// Bind the value property of the slider to the progress property, so
		// both are updated.

		startMedia();

		System.out.println("Ready to view");

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
				System.out.println("onPaused");
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

					Duration totalDuration = mediaView.getMediaPlayer().getTotalDuration();

					MediaPlayer mediaPlayer = mediaView.getMediaPlayer();
					if (mediaPlayer != null) {
						double currentTime = mediaPlayer.getCurrentTime().toSeconds();
						double sliderTime = totalDuration.multiply(progressSlider.getValue() / 100.0).toSeconds();

						if (Math.abs(currentTime - sliderTime) > 1) {
							mediaPlayer.seek(totalDuration.multiply(progressSlider.getValue() / 100.0));
						}
					}
				}
			}
		});

	}

	protected void updateValues() {
		if (playTime != null && progressSlider != null) {
			Platform.runLater(new Runnable() {
				public void run() {
					System.out.println("Updating");
					Duration currentTime = mediaView.getMediaPlayer().getCurrentTime();
					playTime.setText(formatTime(currentTime, duration));

					if (!progressSlider.isDisabled() && duration.greaterThan(Duration.ZERO)
							&& !progressSlider.isValueChanging()) {
						progressSlider.setValue(currentTime.divide(duration).toMillis() * 100.0);
					}
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
