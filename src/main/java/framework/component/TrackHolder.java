package framework.component;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import application.gui.screens.components.VideoPlayer;
import javafx.event.EventHandler;
import javafx.geometry.HPos;
import javafx.scene.control.ListView;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.Slider;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.RowConstraints;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;

public class TrackHolder extends BorderPane {

	// Top section is going to be a vbox with option of title and a
	// progressbarslider that is global.
	ProgressBar bar;
	Slider slider;

	List<ClipTrack> clips;
	ListView<ClipTrack> centreList;

	VideoPlayer vidPlayer;

	double maxWidth;
	double maxTime;

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
		// row1.setMinHeight(10);
		row1.setPrefHeight(30);
		row1.setVgrow(Priority.SOMETIMES);

		top.getColumnConstraints().addAll(col1);
		top.getRowConstraints().addAll(row1);

		top.add(slider, 0, 0);
		top.add(bar, 0, 0);

		slider.valueProperty().set(0);

		bar.progressProperty().bind(slider.valueProperty().divide(slider.getMax()));

		clips = new ArrayList<ClipTrack>();
		centreList = new ListView<ClipTrack>();

		setTop(top);
		setCenter(centreList);

		// Used
		// http://docs.oracle.com/javafx/2/drag_drop/jfxpub-drag_drop.htm

		TrackHolder holder = this;

		setOnDragOver(new EventHandler<DragEvent>() {
			public void handle(DragEvent event) {
				/* data is dragged over the target */
				/*
				 * accept it only if it is not dragged from the same node and if
				 * it has a string data
				 */
				if (event.getGestureSource() != holder && event.getDragboard().hasFiles()) {
					/*
					 * allow for both copying and moving, whatever user chooses
					 */
					event.acceptTransferModes(TransferMode.COPY_OR_MOVE);
				}

				event.consume();
			}
		});

		setOnDragDropped(new EventHandler<DragEvent>() {
			public void handle(DragEvent event) {
				// System.out.println("WAJKSDLAJSLDJALJSLDJ");
				/* data dropped */
				/* if there is a string data on dragboard, read it and use it */
				Dragboard db = event.getDragboard();
				boolean success = false;

				if (db.hasFiles()) {

					for (File file : db.getFiles()) {
						System.out.println("Adding file: " + file.getAbsolutePath());
						Media media = new Media("file:///" + file.getAbsolutePath());
						MediaPlayer player = new MediaPlayer(media);
						ClipTrack clipTrack = new ClipTrack(media);
						// clips.add(new ClipTrack(new Media("file:///" +
						// file.getAbsolutePath()), false));

						player.setOnReady(() -> {
							System.out.println("Media Duration: " + media.getDuration().toMillis());
							double trackWidth = (media.getDuration().toMillis() / maxTime) * maxWidth;

							System.out.println("Max Width: " + maxWidth);
							System.out.println("Max time: " + maxTime);
							System.out.println("Setting track width: " + trackWidth);

							clipTrack.setMaxWidth(trackWidth);
						});
						clips.add(clipTrack);

						clips.forEach(clip -> clip.setProgressProperty(vidPlayer.getProgressSliderProperty(),
								slider.getMax()));
						centreList.getItems().clear();
						centreList.getItems().addAll(clips);
					}
					success = true;
				}
				/*
				 * let the source know whether the string was successfully
				 * transferred and used
				 */
				event.setDropCompleted(success);

				event.consume();
			}
		});
	}

	public void setVideoSource(VideoPlayer vidPlayer) {

		this.vidPlayer = vidPlayer;

		Media media = vidPlayer.getMediaView().getMediaPlayer().getMedia();
		clips.clear();
		clips.add(new ClipTrack(media));

		centreList.getItems().clear();
		// Add items
		centreList.getItems().addAll(clips);

		// Bind the pogress bar
		slider.valueProperty().bind(vidPlayer.getProgressSliderProperty());

		clips.forEach(clip -> clip.setProgressProperty(vidPlayer.getProgressSliderProperty(), slider.getMax()));

		// maxWidth = centreList.getItems().get(0).getPrefWidth();
		maxWidth = centreList.getWidth();
		maxTime = vidPlayer.getMediaView().getMediaPlayer().getTotalDuration().toMillis();

	}

}
