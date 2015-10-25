package framework.component;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import application.gui.screens.components.VideoPlayer;
import javafx.event.EventHandler;
import javafx.geometry.HPos;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.Slider;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.RowConstraints;
import javafx.scene.media.Media;

public class TrackHolder extends BorderPane {

	// Top section is going to be a vbox with option of title and a
	// progressbarslider that is global.
	ProgressBar bar;
	Slider slider;

	List<ClipTrack> clips;
	ListView<HBox> centreList;

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
		centreList = new ListView<HBox>();

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
						ClipTrack clipTrack = new ClipTrack(media, false);
						// clips.add(new ClipTrack(new Media("file:///" +
						// file.getAbsolutePath()), false));

						clips.add(clipTrack);

						clips.forEach(clip -> clip.setProgressProperty(vidPlayer.getProgressSliderProperty(),
								slider.getMax()));
						centreList.getItems().clear();

						for (ClipTrack clipTrack2 : clips) {
							HBox box = getHBoxWithClip(clipTrack2);
							centreList.getItems().add(box);
						}

						clipTrack.setClipWidth(maxTime, maxWidth);

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

		ClipTrack clipTrack = new ClipTrack(media, true);
		clips.add(clipTrack);

		centreList.getItems().clear();
		// Add items

		HBox box = getHBoxWithClip(clips.get(0));
		box.setMaxWidth(getMaxWidth());
		System.out.println("Box max width: " + getMaxWidth());
		centreList.getItems().addAll(box);

		// Bind the pogress bar
		slider.valueProperty().bind(vidPlayer.getProgressSliderProperty());

		clips.forEach(clip -> clip.setProgressProperty(vidPlayer.getProgressSliderProperty(), slider.getMax()));

		// maxWidth = centreList.getItems().get(0).getPrefWidth();

		maxWidth = centreList.getWidth();

		maxTime = vidPlayer.getMediaView().getMediaPlayer().getTotalDuration().toMillis();

		// Remove the scroll buttons for the primary
		box.getChildren().remove(0);
		box.getChildren().remove(1);

		clipTrack.setClipMax(maxWidth);

	}

	private HBox getHBoxWithClip(ClipTrack clip) {
		HBox box = new HBox();
		Button leftStepButton = new Button("<");
		Button rightStepButton = new Button(">");

		Pane pane = new Pane();
		pane.getChildren().add(clip);

		box.getChildren().addAll(leftStepButton, pane, rightStepButton);

		return box;
	}

}
