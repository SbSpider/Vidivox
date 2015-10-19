package framework.component;

import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;

public class ClipTrack extends BorderPane {

	ImageView imageView;

	public ClipTrack() {

		imageView = new ImageView("/waveform.png");
		imageView.setFitHeight(50);

		imageView.setFitWidth(50);
		// imageView.fitWidthProperty().bind(widthProperty());

		setMaxHeight(50);
		setCenter(imageView);
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
