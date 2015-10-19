package framework.component;

import java.io.IOException;
import java.io.InputStream;

import javafx.scene.control.ProgressBar;
import javafx.scene.image.Image;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.BackgroundImage;
import javafx.scene.layout.BackgroundPosition;
import javafx.scene.layout.BackgroundRepeat;
import javafx.scene.layout.BorderPane;

public class ClipTrack extends BorderPane {

	private ProgressBar trackPositionBar;

	public ClipTrack() {
		trackPositionBar = new ProgressBar();
		trackPositionBar.setProgress(0);

		BackgroundImage image = null;
		
		try {
		InputStream resourceAsStream = getClass().getResourceAsStream("waveform.png");
		image = new BackgroundImage(new Image(resourceAsStream), BackgroundRepeat.NO_REPEAT,
				BackgroundRepeat.NO_REPEAT, BackgroundPosition.CENTER, null);
		} catch (Exception e){
			System.out.println("Failed to find file");
			e.printStackTrace();
		}

		trackPositionBar.setBackground(new Background(image));

		setCenter(trackPositionBar);
	}

}
