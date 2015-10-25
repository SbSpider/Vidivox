package framework.component;

import java.io.File;

import framework.media.conversion.FFMPEGConverterTask;
import framework.media.conversion.FFMPEGGenerateWaveform;
import javafx.beans.property.DoubleProperty;
import javafx.geometry.HPos;
import javafx.scene.control.ProgressBar;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.RowConstraints;
import javafx.scene.media.Media;

public class ClipTrack extends BorderPane {

	ImageView imageView;
	ProgressBar bar;

	public ClipTrack() {

		imageView = new ImageView();
//		imageView.setFitHeight(50);

//		imageView.setFitWidth(50);

		bar = new ProgressBar();
		bar.setId("clip-progress-bar");

		GridPane center = new GridPane();

		ColumnConstraints col1 = new ColumnConstraints();
		col1.setMinWidth(10);
		col1.setPrefWidth(100);
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
	 */
	public ClipTrack(Media media) {

		this();

		setMedia(media);

	}

	public void setMedia(Media media) {
		String source = media.getSource();
		source = source.substring("file:///".length());

		FFMPEGGenerateWaveform generateTask = new FFMPEGGenerateWaveform(new File(source));
		Thread thread = new Thread(generateTask);
		thread.setDaemon(false);
		thread.start();

		generateTask.setOnSucceeded(event -> {
			// Set the image once generated.
			imageView.setImage(new Image("file:///" + generateTask.getValue().getAbsolutePath()));
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

}
