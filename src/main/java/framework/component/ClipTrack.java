package framework.component;

import java.io.IOException;
import java.io.InputStream;

import javafx.scene.control.ProgressBar;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.BackgroundImage;
import javafx.scene.layout.BackgroundPosition;
import javafx.scene.layout.BackgroundRepeat;
import javafx.scene.layout.BorderPane;

public class ClipTrack extends BorderPane {

	ImageView imageView;

	public ClipTrack() {

		imageView = new ImageView("/waveform.png");
		imageView.setFitHeight(50);

		imageView.setFitWidth(50);
//		imageView.fitWidthProperty().bind(widthProperty());

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
