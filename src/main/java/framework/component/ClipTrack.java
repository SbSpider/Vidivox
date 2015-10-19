package framework.component;

import java.io.File;

import framework.media.conversion.FFMPEGConverterTask;
import framework.media.conversion.FFMPEGGenerateWaveform;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.media.Media;

public class ClipTrack extends BorderPane {

	ImageView imageView;

	public ClipTrack() {

		imageView = new ImageView();
		imageView.setFitHeight(50);

		imageView.setFitWidth(50);

		setMaxHeight(50);
		setCenter(imageView);
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
