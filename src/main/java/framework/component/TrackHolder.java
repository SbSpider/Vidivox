package framework.component;

import java.util.ArrayList;
import java.util.List;

import application.gui.screens.components.VideoPlayer;
import javafx.geometry.HPos;
import javafx.scene.control.ListView;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.Slider;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.RowConstraints;
import javafx.scene.media.Media;

public class TrackHolder extends BorderPane {

	// Top section is going to be a vbox with option of title and a
	// progressbarslider that is global.
	ProgressBar bar;
	Slider slider;

	List<ClipTrack> clips;
	ListView<ClipTrack> centreList;

	VideoPlayer vidPlayer;

	public TrackHolder() {
		slider = new Slider();
		bar = new ProgressBar();
		
		slider.setMaxWidth(Double.MAX_VALUE);
		bar.setMaxWidth(Double.MAX_VALUE);

		GridPane top = new GridPane();

		ColumnConstraints col1 = new ColumnConstraints();
		col1.setMinWidth(10);
		col1.setPrefWidth(100);
		col1.setHgrow(Priority.ALWAYS);
		col1.setHalignment(HPos.CENTER);

		RowConstraints row1 = new RowConstraints();
		row1.setMinHeight(10);
		row1.setPrefHeight(30);
		row1.setVgrow(Priority.SOMETIMES);

		top.getColumnConstraints().addAll(col1);
		top.getRowConstraints().addAll(row1);

		top.add(slider, 0, 0);
		top.add(bar, 0, 0);

		slider.valueProperty().set(0);
		slider.valueProperty().addListener((observable, oldValue, newValue) -> {

			double value = (Double) newValue / slider.getMax();

			// This little block just adjusts the progress so that it
			// appears under the slider object rather than not.
			if (value < 0.25) {
				value += 0.02;
			} else if (value < 0.5) {
				value += 0.001;
			} else if (value < 0.75) {
				value += 0.0001;
			}
			bar.setProgress(value);
		});

		clips = new ArrayList<ClipTrack>();
		centreList = new ListView<ClipTrack>();

		setTop(top);
		setCenter(centreList);
	}

	public void setVideoSource(VideoPlayer vidPlayer) {

		this.vidPlayer = vidPlayer;

		Media media = vidPlayer.getMediaView().getMediaPlayer().getMedia();
		clips.add(new ClipTrack(media));

		centreList.getItems().clear();
		// Add items
		centreList.getItems().addAll(clips);

		// Bind the pogress bar
		slider.valueProperty().bind(vidPlayer.getProgressSliderProperty());
		
		clips.forEach(clip -> clip.setProgressProperty(vidPlayer.getProgressSliderProperty(), slider.getMax()));
	}

}
