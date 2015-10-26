package framework.component;

import java.io.File;

import framework.media.conversion.FFMPEGGenerateWaveform;
import javafx.beans.property.DoubleProperty;
import javafx.geometry.HPos;
import javafx.scene.control.ProgressBar;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.RowConstraints;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;

public class ClipTrack extends BorderPane {

	ImageView imageView;
	ProgressBar bar;
	private HBox hBox;
	private MediaPlayer player;

	boolean primary;
	private Media media;
	private GridPane center;

	public ClipTrack(boolean primary) {
		this.primary = primary;

		imageView = new ImageView();
		// imageView.setFitHeight(50);

		// imageView.setFitWidth(50);

		bar = new ProgressBar();
		bar.setId("clip-progress-bar");

		center = new GridPane();

		ColumnConstraints col1 = new ColumnConstraints();
		col1.setMinWidth(getMaxWidth());
		col1.setPrefWidth(getPrefWidth());
		col1.setHgrow(Priority.ALWAYS);
		col1.setHalignment(HPos.CENTER);

		RowConstraints row1 = new RowConstraints();
		// row1.setMinHeight(10);
		// row1.setPrefHeight(30);
		row1.setVgrow(Priority.SOMETIMES);

		center.getColumnConstraints().addAll(col1);
		center.getRowConstraints().addAll(row1);

		center.add(imageView, 0, 0);
		center.add(bar, 0, 0);

		// setMaxHeight(50);
		setCenter(center);

		bar.prefWidthProperty().bind(widthProperty());
		bar.prefHeightProperty().bind(heightProperty());

		// bar.setVisible(false);

	}

	/**
	 * Generate a ClipTrack from media
	 * 
	 * @param media
	 * @param isPrimary
	 */
	public ClipTrack(Media media, boolean primary) {
		this(primary);
		setMedia(media);
	}

	public boolean getPrimary() {
		return primary;
	}

	public void setMedia(Media media) {
		String source = media.getSource();
		source = source.substring("file:///".length());

		FFMPEGGenerateWaveform generateTask = new FFMPEGGenerateWaveform(new File(source));
		Thread thread = new Thread(generateTask);
		thread.setDaemon(false);
		thread.start();

		this.media = media;

		generateTask.setOnSucceeded(event -> {
			// Set the image once generated.
			imageView.setImage(new Image("file:///" + generateTask.getValue().getAbsolutePath()));
		});

	}

	public void setClipWidth(double maxTime, double maxWidth) {

		if (primary) {
			setClipMax(maxWidth);
		} else {

			player = new MediaPlayer(media);

			player.setOnReady(() -> {

				Media media = player.getMedia();

				System.out.println("Media Duration: " + media.getDuration().toMillis());
				double trackWidth = (media.getDuration().toMillis() / maxTime) * maxWidth;

				System.out.println("Max Width: " + maxWidth);
				System.out.println("Max time: " + maxTime);
				System.out.println("Setting track width: " + trackWidth);

				// ((GridPane) (getCenter())).setMaxWidth(trackWidth);
				center.setMaxWidth(trackWidth);
				bar.setMaxWidth(trackWidth);

				center.getColumnConstraints().forEach(constraint -> {
					constraint.setMaxWidth(trackWidth);
					constraint.setPrefWidth(trackWidth);
				});
			});

		}
	}

	public void setClipMax(double maxTrackWidth) {
		center.setMaxWidth(maxTrackWidth);
		bar.setMaxWidth(maxTrackWidth);

		center.getColumnConstraints().forEach(constraint -> {
			constraint.setMaxWidth(maxTrackWidth);
			constraint.setPrefWidth(maxTrackWidth);
		});
	}

	public void setProgressProperty(DoubleProperty prop, double max) {
		bar.setVisible(true);
		bar.progressProperty().bind(prop.divide(max));
	}

	@Override
	protected void setHeight(double value) {
		super.setHeight(value);
		imageView.setFitHeight(value);
	}

	@Override
	protected void setWidth(double value) {
		super.setWidth(value);
		imageView.setFitWidth(value);
	}

	// Utilized
	// http://stackoverflow.com/questions/30316917/javafx-draggable-node-label-horizontally-only-not-vertically

	class DragContext {
		double x;
		double y;
	}

	DragContext dragContext = new DragContext();

	public DragContext getDragContext() {
		return dragContext;
	}

}
